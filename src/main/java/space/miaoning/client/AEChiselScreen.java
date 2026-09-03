package space.miaoning.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import space.miaoning.block.AEChiselBlockEntity;
import space.miaoning.menu.AEChiselMenu;

public final class AEChiselScreen extends AbstractContainerScreen<AEChiselMenu> {
    private static final ResourceLocation CHEST_TEXTURE = new ResourceLocation("ae2", "textures/guis/chest.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int TOP_SECTION_HEIGHT = 83;
    private static final int EXTRA_TOP_HEIGHT = 22;
    private static final int PLAYER_INVENTORY_TOP = TOP_SECTION_HEIGHT + EXTRA_TOP_HEIGHT;

    private ParallelSlider parallelSlider;

    public AEChiselScreen(AEChiselMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 188;
    }

    @Override
    protected void init() {
        super.init();
        parallelSlider = addRenderableWidget(new ParallelSlider(leftPos + 28, topPos + 61, menu.getParallel()));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!parallelSlider.isDragging()) {
            parallelSlider.setParallel(menu.getParallel());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(CHEST_TEXTURE, leftPos, topPos, 0, 0, imageWidth, TOP_SECTION_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        renderExpandedTopSection(graphics);
        graphics.blit(CHEST_TEXTURE, leftPos, topPos + PLAYER_INVENTORY_TOP, 0, TOP_SECTION_HEIGHT,
                imageWidth, TOP_SECTION_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, 95, 0x404040, false);
    }

    private void renderExpandedTopSection(GuiGraphics graphics) {
        int top = topPos + TOP_SECTION_HEIGHT;
        int bottom = top + EXTRA_TOP_HEIGHT;
        graphics.fill(leftPos, top, leftPos + imageWidth, bottom, 0xFFC6C6C6);
        graphics.fill(leftPos, top, leftPos + 1, bottom, 0xFF000000);
        graphics.fill(leftPos + 1, top, leftPos + 3, bottom, 0xFFFFFFFF);
        graphics.fill(leftPos + 173, top, leftPos + 175, bottom, 0xFF555555);
        graphics.fill(leftPos + 175, top, leftPos + imageWidth, bottom, 0xFF000000);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (parallelSlider.isMouseOver(mouseX, mouseY)) {
            return parallelSlider.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (parallelSlider.isDragging()) {
            return parallelSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (parallelSlider.isDragging()) {
            return parallelSlider.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private static double normalizedValue(int parallel) {
        int clamped = Mth.clamp(parallel, AEChiselBlockEntity.MIN_PARALLEL, AEChiselBlockEntity.MAX_PARALLEL);
        return (clamped - AEChiselBlockEntity.MIN_PARALLEL)
                / (double) (AEChiselBlockEntity.MAX_PARALLEL - AEChiselBlockEntity.MIN_PARALLEL);
    }

    private final class ParallelSlider extends AbstractSliderButton {
        private int lastSentParallel;
        private boolean dragging;

        private ParallelSlider(int x, int y, int parallel) {
            super(x, y, 120, 20, Component.empty(), normalizedValue(parallel));
            lastSentParallel = parallel;
            updateMessage();
        }

        private int parallel() {
            return Mth.clamp(
                    (int) Math.round(value * (AEChiselBlockEntity.MAX_PARALLEL - AEChiselBlockEntity.MIN_PARALLEL))
                            + AEChiselBlockEntity.MIN_PARALLEL,
                    AEChiselBlockEntity.MIN_PARALLEL,
                    AEChiselBlockEntity.MAX_PARALLEL
            );
        }

        private void setParallel(int parallel) {
            value = normalizedValue(parallel);
            lastSentParallel = parallel;
            updateMessage();
        }

        private boolean isDragging() {
            return dragging;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            dragging = true;
            super.onClick(mouseX, mouseY);
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            dragging = false;
            super.onRelease(mouseX, mouseY);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.applied_rechiseled.ae_chisel.parallel", parallel()));
        }

        @Override
        protected void applyValue() {
            int parallel = parallel();
            if (parallel != lastSentParallel && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, parallel);
                lastSentParallel = parallel;
            }
        }
    }
}
