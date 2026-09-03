package space.miaoning.block;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import space.miaoning.registry.ModBlockEntityTypes;
import space.miaoning.registry.ModItems;
import space.miaoning.util.ChiselRecipeResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public class AEChiselBlockEntity extends AENetworkBlockEntity implements ICraftingProvider, IGridTickable, InternalInventoryHost {
    private List<IPatternDetails> patterns = new ArrayList<>();
    private final IManagedGridNode mainNode = this.getMainNode();
    private final AppEngInternalInventory templateSlot = new AppEngInternalInventory(this, 1, 1);
    private final List<GenericStack> pendingOutputList = new ArrayList<>();
    private final MachineSource actionSource = new MachineSource(this);
    private int parallel = 1;

    private static final String NBT_TEMPLATE_SLOT = "template_slot";
    private static final String NBT_PENDING_OUTPUT_LIST = "pending_output_list";
    private static final String NBT_PARALLEL = "parallel";
    private static final Set<AEChiselBlockEntity> INSTANCES =
            Collections.newSetFromMap(new WeakHashMap<>());

    public AEChiselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.AE_CHISEL.get(), pos, state);

        this.mainNode.setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(10.0)
                .setVisualRepresentation(AEItemKey.of(ModItems.AE_CHISEL.get()))
                .addService(ICraftingProvider.class, this)
                .addService(IGridTickable.class, this);
    }

    private void updatePatterns() {
        ItemStack templateItemStack = this.templateSlot.getStackInSlot(0);
        this.patterns = ChiselRecipeResolver.resolve(level, templateItemStack, parallel);
    }

    @Override
    public void onReady() {
        registerInstance(this);
        updatePatterns();
        super.onReady();
    }

    @Override
    public void onChunkUnloaded() {
        unregisterInstance(this);
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        unregisterInstance(this);
        super.setRemoved();
    }

    public static void refreshAllPatterns() {
        List<AEChiselBlockEntity> instances;
        synchronized (INSTANCES) {
            instances = List.copyOf(INSTANCES);
        }

        for (AEChiselBlockEntity instance : instances) {
            if (!instance.isRemoved() && instance.level != null) {
                instance.updatePatterns();
                if (!instance.level.isClientSide()) {
                    ICraftingProvider.requestUpdate(instance.mainNode);
                }
            }
        }
    }

    private static void registerInstance(AEChiselBlockEntity instance) {
        synchronized (INSTANCES) {
            INSTANCES.add(instance);
        }
    }

    private static void unregisterInstance(AEChiselBlockEntity instance) {
        synchronized (INSTANCES) {
            INSTANCES.remove(instance);
        }
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        updatePatterns();
        ICraftingProvider.requestUpdate(mainNode);
    }

    @Override
    public boolean isClientSide() {
        return level == null || level.isClientSide();
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return this.patterns;
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        writeToNBT(data);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        readFromNBT(data);
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!this.mainNode.isActive() || !this.patterns.contains(patternDetails)) {
            return false;
        }

        GenericStack output = patternDetails.getPrimaryOutput();

        // Here the pendingOutputList always has only one element, even though it's a list.
        addToPendingOutputList(output.what(), output.amount());
        this.saveChanges();
        return true;
    }

    private void addToPendingOutputList(AEKey what, long amount) {
        if (amount > 0) {
            this.pendingOutputList.add(new GenericStack(what, amount));

            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }
    }

    @Override
    public boolean isBusy() {
        return !pendingOutputList.isEmpty();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Interface, pendingOutputList.isEmpty(), true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.mainNode.isActive()) {
            return TickRateModulation.SLEEP;
        }

        var storage = node.getGrid().getStorageService().getInventory();
        boolean didSomething = false;

        for (var iterator = this.pendingOutputList.listIterator(); iterator.hasNext();) {
            var pendingOutput = iterator.next();

            long inserted = storage.insert(
                    pendingOutput.what(),
                    pendingOutput.amount(),
                    Actionable.MODULATE,
                    this.actionSource);

            if (inserted >= pendingOutput.amount()) {
                iterator.remove();
                didSomething = true;
            } else if (inserted > 0) {
                iterator.set(new GenericStack(
                        pendingOutput.what(),
                        pendingOutput.amount() - inserted));
                didSomething = true;
            }
        }

        if (didSomething) {
            this.saveChanges();
        }

        if (this.pendingOutputList.isEmpty()) {
            return TickRateModulation.SLEEP;
        }

        return didSomething
                ? TickRateModulation.URGENT
                : TickRateModulation.SLOWER;
    }


    private void writeToNBT(CompoundTag data) {
        this.templateSlot.writeToNBT(data, NBT_TEMPLATE_SLOT);
        data.putInt(NBT_PARALLEL, parallel);
        ListTag pendingOutputListTag = new ListTag();
        for (var output : pendingOutputList) {
            pendingOutputListTag.add(GenericStack.writeTag(output));
        }
        data.put(NBT_PENDING_OUTPUT_LIST, pendingOutputListTag);
    }

    private void readFromNBT(CompoundTag data) {
        templateSlot.readFromNBT(data, NBT_TEMPLATE_SLOT);
        parallel = Mth.clamp(data.getInt(NBT_PARALLEL), 1, ChiselRecipeResolver.MAX_PARALLEL);
        ListTag pendingOutputListTag = data.getList(NBT_PENDING_OUTPUT_LIST, Tag.TAG_COMPOUND);
        for (int i = 0; i < pendingOutputListTag.size(); ++i) {
            var stack = GenericStack.readTag(pendingOutputListTag.getCompound(i));
            if (stack != null) {
                this.addToPendingOutputList(stack.what(), stack.amount());
            }
        }
    }

    public int getParallel() {
        return parallel;
    }

    public void setParallel(int parallel) {
        int normalized = Mth.clamp(parallel, 1, ChiselRecipeResolver.MAX_PARALLEL);
        if (this.parallel == normalized) {
            return;
        }

        this.parallel = normalized;
        updatePatterns();
        ICraftingProvider.requestUpdate(mainNode);
        saveChanges();
    }
}
