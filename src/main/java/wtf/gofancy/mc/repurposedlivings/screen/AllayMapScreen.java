package wtf.gofancy.mc.repurposedlivings.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;
import wtf.gofancy.mc.repurposedlivings.container.AllayMapContainer;
import wtf.gofancy.mc.repurposedlivings.network.UpdateAllayMapTargetSide;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AllayMapScreen extends AbstractContainerScreen<AllayMapContainer> {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(RepurposedLivings.MODID, "textures/gui/allay_map.png");

    public AllayMapScreen(AllayMapContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 216;
    }

    @Override
    protected void init() {
        super.init();

        addTarget(this.leftPos + 10, this.topPos + 25 + this.font.lineHeight, UpdateAllayMapTargetSide.Target.SOURCE, this.menu::getSourceTarget, this.menu::setSourceTarget);
        addTarget(this.leftPos + 10, this.topPos + 95 + this.font.lineHeight, UpdateAllayMapTargetSide.Target.DESTINATION, this.menu::getDestinationTarget, this.menu::setDestinationTarget);
    }
    
    private void addTarget(int x, int y, UpdateAllayMapTargetSide.Target target, Supplier<ItemTarget> getter, Consumer<ItemTarget> setter) {
        addTargetSideButton(x + 152, y + 18, target, getter, setter);
    }
    
    private void addTargetSideButton(int x, int y, UpdateAllayMapTargetSide.Target target, Supplier<ItemTarget> getter, Consumer<ItemTarget> setter) {
        addRenderableWidget(new Button(x, y, 40, 20, getTranslationForSide(getter.get().side()), button -> {
            Direction next = this.menu.setTargetSide(target, getter, setter);
            button.setMessage(getTranslationForSide(next));
        }));
    }
    
    private static Component getTranslationForSide(Direction side) {
        return ModUtil.getItemTranslation(ModSetup.ALLAY_MAP.get(), "side." + side.getName());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderFg(poseStack);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, BACKGROUND);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        blit(poseStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }
    
    public void renderFg(PoseStack poseStack) {
        renderTarget("source_target", this.menu.getSourceTarget(), poseStack, this.leftPos + 10, this.topPos + 25);
        renderTarget("destination_target", this.menu.getDestinationTarget(), poseStack, this.leftPos + 10, this.topPos + 95);
    }
    
    private void renderTarget(String key, ItemTarget target, PoseStack poseStack, int x, int y) {
        BlockEntity be = this.minecraft.level.getBlockEntity(target.pos());
        Block block = be != null ? be.getBlockState().getBlock() : Blocks.CHEST;
        renderScaledItemIntoGui(new ItemStack(block), x, y + 10, 2);

        this.font.draw(poseStack, ModUtil.getContainerTranslation("allay_map", key), x + 40, y, 4210752);
        
        this.font.draw(poseStack, ModUtil.getContainerTranslation("allay_map", "pos"), x + 40, y + 13, 4210752);
        this.font.draw(poseStack, ModUtil.getContainerTranslation("allay_map", "pos.x", target.pos().getX()), x + 40, y + 15 + this.font.lineHeight, 4210752);
        this.font.draw(poseStack, ModUtil.getContainerTranslation("allay_map", "pos.y", target.pos().getY()), x + 40, y + 15 + this.font.lineHeight * 2, 4210752);
        this.font.draw(poseStack, ModUtil.getContainerTranslation("allay_map", "pos.z", target.pos().getZ()), x + 40, y + 15 + this.font.lineHeight * 3, 4210752);
        
        this.font.draw(poseStack, ModUtil.getItemTranslation(ModSetup.ALLAY_MAP.get(), "side"), x + 152, y + 13, 4210752);
    }
    
    @Override
    protected void renderLabels(PoseStack poseStack, int pMouseX, int pMouseY) {
        drawCenteredStringWithoutShadow(poseStack, this.title, this.imageWidth / 2, this.titleLabelY, 4210752);
    }
    
    public void drawCenteredStringWithoutShadow(PoseStack poseStack, Component text, int x, int y, int color) {         
        FormattedCharSequence sequence = text.getVisualOrderText();
        this.font.draw(poseStack, sequence, (float) (x - this.font.width(sequence) / 2), (float) y, color);
    }
    
    public void renderScaledItemIntoGui(ItemStack stack, int x, int y, float scale) {
        ItemRenderer renderer = this.minecraft.getItemRenderer();
        BakedModel model = renderer.getModel(stack, null, null, 0);

        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(x, y, 100 + renderer.blitOffset);
        poseStack.scale(scale, scale, 1);
        poseStack.translate(8, 8, 0);
        poseStack.scale(1, -1, 1);
        poseStack.scale(16, 16, 16);
        RenderSystem.applyModelViewMatrix();
        
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        boolean useFlatLight = !model.usesBlockLight();
        if (useFlatLight) Lighting.setupForFlatItems();
        renderer.render(stack, ItemTransforms.TransformType.GUI, false, new PoseStack(), bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if (useFlatLight) Lighting.setupFor3DItems();

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
