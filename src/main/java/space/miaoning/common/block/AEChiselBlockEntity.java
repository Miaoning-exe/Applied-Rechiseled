package space.miaoning.common.block;

import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import space.miaoning.common.pattern.ChiselPatternDetails;
import space.miaoning.common.registry.ModBlockEntityTypes;
import space.miaoning.common.registry.ModItems;
import space.miaoning.common.util.ChiselRecipeResolver;

import java.util.ArrayList;
import java.util.List;

public class AEChiselBlockEntity extends AENetworkBlockEntity implements ICraftingProvider, IGridTickable, InternalInventoryHost {
    private List<IPatternDetails> patterns = new ArrayList<>();
    private final IManagedGridNode mainNode = this.getMainNode();
    private final AppEngInternalInventory templateSlot = new AppEngInternalInventory(this, 1, 1);
    private static final String NBT_TEMPLATE_SLOT = "template_slot";

    public AEChiselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.AE_CHISEL.get(), pos, state);

        this.mainNode.setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(10.0)
                .setVisualRepresentation(AEItemKey.of(ModItems.AE_CHISEL.get()))
                .addService(ICraftingProvider.class, this)
                .addService(IGridTickable.class, this);
        this.rebuildPatterns();
    }


    private void rebuildPatterns() {
        ItemStack templateItemStack = this.templateSlot.getStackInSlot(0);
        this.patterns = ChiselRecipeResolver.getAllChiselPattern(level, templateItemStack);
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
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        templateSlot.writeToNBT(data, NBT_TEMPLATE_SLOT);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        templateSlot.readFromNBT(data, NBT_TEMPLATE_SLOT);
    }

    @Override
    public void onReady() {
        rebuildPatterns();
        super.onReady();
    }



}
