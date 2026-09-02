package space.miaoning.common.block;

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
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import space.miaoning.common.pattern.ChiselPatternDetails;
import space.miaoning.common.registry.ModBlockEntityTypes;
import space.miaoning.common.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

public class AEChiselBlockEntity extends AENetworkBlockEntity implements ICraftingProvider, IGridTickable, InternalInventoryHost {
    private final List<IPatternDetails> patterns = new ArrayList<>();
    private final IManagedGridNode mainNode = this.getMainNode();
    private final AppEngInternalInventory chiselRecipeSlot = new AppEngInternalInventory(this, 1, 1);


    public AEChiselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.AE_CHISEL.get(), pos, state);

        mainNode.setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(10.0)
                .setVisualRepresentation(AEItemKey.of(ModItems.AE_CHISEL.get()))
                .addService(ICraftingProvider.class, this)
                .addService(IGridTickable.class, this);
    }


    private void rebuildPatterns() {
//        this.patterns.clear();
//
//        ItemStack stack = this.chiselRecipeSlot.getStackInSlot(0);
//        if (!stack.isEmpty()) {
//            var registry = CarvingUtils.getChiselRegistry();
//            if (registry != null) {
//                var input = GenericStack.fromItemStack(stack);
//                if (!ChiselPatternDetails.addChiselPatterns(input, registry.getItemsForChiseling(stack), this.patterns, this.parallel)) {
//                    this.chiselRecipeSlot.setItemDirect(0, ItemStack.EMPTY);
//                    return;
//                }
//            }
//        }
//
//        this.postPatternChange();
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        rebuildPatterns();
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
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!this.mainNode.isActive() || !this.patterns.contains(patternDetails)) {
            return false;
        }


    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return null;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return null;
    }

}
