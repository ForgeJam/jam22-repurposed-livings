package wtf.gofancy.mc.repurposedlivings.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;
import wtf.gofancy.mc.repurposedlivings.container.AllayMapContainer;
import wtf.gofancy.mc.repurposedlivings.network.Network;
import wtf.gofancy.mc.repurposedlivings.network.UpdateAllayMapTargetSide;

public class AllayMapScreen extends AbstractContainerScreen<AllayMapContainer> {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(RepurposedLivings.MODID, "textures/gui/allay_map.png");
    
    private EditBox sourcePos;

    public AllayMapScreen(AllayMapContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        
        BlockPos sourcePos = this.menu.getSourceTarget().pos();
        String sourcePosStr = String.format("X: %s, Y: %s, Z: %s", sourcePos.getX(), sourcePos.getY(), sourcePos.getZ());
        this.sourcePos = new EditBox(this.font, this.leftPos + 30, this.topPos + 55, 10 + this.font.width(sourcePosStr), 12, Component.literal("Pos"));
        this.sourcePos.setBordered(true);
        this.sourcePos.setEditable(false);
        this.sourcePos.setValue(sourcePosStr);
        addWidget(this.sourcePos);
        
        addRenderableWidget(new Button(this.leftPos + 70, this.topPos + 28, 40, 20, Component.literal("Change"), button -> {
            Direction side = this.menu.getSourceTarget().side();
            Direction next = Direction.values()[(side.ordinal() + 1) % Direction.values().length];
            Network.INSTANCE.sendToServer(new UpdateAllayMapTargetSide(this.menu.getHand(), UpdateAllayMapTargetSide.Target.SOURCE, next));
        }));
        
        addRenderableWidget(new Button(this.leftPos + 70, this.topPos + 100, 40, 20, Component.literal("Change"), button -> {
            Direction side = this.menu.getDestinationTarget().side();
            Direction next = Direction.values()[(side.ordinal() + 1) % Direction.values().length];
            Network.INSTANCE.sendToServer(new UpdateAllayMapTargetSide(this.menu.getHand(), UpdateAllayMapTargetSide.Target.DESTINATION, next));
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderFg(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, BACKGROUND);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        blit(poseStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }
    
    public void renderFg(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        BlockEntity be = this.minecraft.level.getBlockEntity(this.menu.getSourceTarget().pos());
        if (be != null) {
            Block block = be.getBlockState().getBlock();
            renderScaledItemIntoGui(new ItemStack(block), this.leftPos + 10, this.topPos + 20, 2);   
        }
        
        this.sourcePos.render(poseStack, mouseX, mouseY, partialTick);
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
