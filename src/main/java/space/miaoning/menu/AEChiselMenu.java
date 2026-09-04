package space.miaoning.menu;

import appeng.api.inventories.InternalInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import space.miaoning.block.AEChiselBlockEntity;
import space.miaoning.registry.ModBlocks;
import space.miaoning.registry.ModMenus;

public final class AEChiselMenu extends AbstractContainerMenu {
    private static final int TEMPLATE_SLOT = 0;
    private static final int PLAYER_INVENTORY_TOP = 106;
    private static final int HOTBAR_TOP = 164;

    private final AEChiselBlockEntity chisel;
    private final DataSlot parallelData;
    private int syncedParallel;

    public AEChiselMenu(int containerId, Inventory playerInventory, AEChiselBlockEntity chisel) {
        super(ModMenus.AE_CHISEL.get(), containerId);
        this.chisel = chisel;
        this.syncedParallel = chisel.getParallel();

        addSlot(new TemplateSlot(chisel.getTemplateSlot(), 0, 80, 37));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, PLAYER_INVENTORY_TOP + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, HOTBAR_TOP));
        }

        parallelData = addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return chisel.isClientSide() ? syncedParallel : chisel.getParallel();
            }

            @Override
            public void set(int value) {
                syncedParallel = Mth.clamp(value, AEChiselBlockEntity.MIN_PARALLEL, AEChiselBlockEntity.MAX_PARALLEL);
            }
        });
    }

    public static AEChiselMenu fromNetwork(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        Level level = playerInventory.player.level();
        if (level.getBlockEntity(pos) instanceof AEChiselBlockEntity chisel) {
            return new AEChiselMenu(containerId, playerInventory, chisel);
        }
        throw new IllegalStateException("Missing AE Chisel block entity at " + pos);
    }

    public int getParallel() {
        return parallelData.get();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < AEChiselBlockEntity.MIN_PARALLEL || id > AEChiselBlockEntity.MAX_PARALLEL) {
            return false;
        }

        chisel.setParallel(id);
        return true;
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId == TEMPLATE_SLOT) {
            if (clickType == ClickType.PICKUP && !player.level().isClientSide()) {
                chisel.setTemplate(getCarried());
                broadcastChanges();
            }
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean canDragTo(@NotNull Slot slot) {
        return slots.indexOf(slot) != TEMPLATE_SLOT && super.canDragTo(slot);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player.level().getBlockState(chisel.getBlockPos()).is(ModBlocks.AE_CHISEL.get())
                && player.distanceToSqr(
                chisel.getBlockPos().getX() + 0.5,
                chisel.getBlockPos().getY() + 0.5,
                chisel.getBlockPos().getZ() + 0.5
        ) <= 64.0;
    }

    private static final class TemplateSlot extends Slot {
        private TemplateSlot(InternalInventory inventory, int slot, int x, int y) {
            super(inventory.toContainer(), slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(@NotNull Player player) {
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
