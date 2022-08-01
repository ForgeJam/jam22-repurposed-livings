/*
 * Copyright (c) 2021 DarkKronicle
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package wtf.gofancy.mc.repurposedlivings.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import wtf.gofancy.mc.repurposedlivings.Capabilities;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;

import java.util.stream.Stream;

/**
 * Modified version of DarkKronicle's BetterBlockOutline renderer.<br>
 * <b>SOURCE</b>: <a href="https://github.com/DarkKronicle/BetterBlockOutline">DarkKronicle's BetterBlockOutline</a><br>
 * <ul>
 *     <li>
 *         <a href="https://github.com/DarkKronicle/BetterBlockOutline/blob/607b0c3b280af1516a91aac41457c6cf0abf3508/src/main/java/io/github/darkkronicle/betterblockoutline/renderers/BasicOutlineRenderer.java">BasicOutlineRenderer</a>
 *     </li>
 *     <li>
 *         <a href="https://github.com/DarkKronicle/BetterBlockOutline/blob/607b0c3b280af1516a91aac41457c6cf0abf3508/src/main/java/io/github/darkkronicle/betterblockoutline/util/RenderingUtil.java">RenderingUtil</a>
 *     </li>
 * </ul>
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = RepurposedLivings.MODID)
public final class AllayMapBlockHighlighter {
    private static final float OUTLINE_WIDTH = 1.0F;
    private static final Color OUTLINE_COLOR = new Color(1, 1, 1, 0.5F);
    private static final VoxelShape OUTLINE_SHAPE = Shapes.block();
    private static final Color SOURCE_FILL_COLOR = new Color(0, 1, 0, 0.15F);
    private static final Color DESTINATION_FILL_COLOR = new Color(1, 0, 0, 0.25F);

    @SubscribeEvent
    public static void onRenderLevel(final RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            final Minecraft minecraft = Minecraft.getInstance();
            minecraft.level.getCapability(Capabilities.ALLAY_MAP_DATA).resolve()
                .flatMap(data -> Stream.of(minecraft.player.getMainHandItem(), minecraft.player.getOffhandItem())
                    .flatMap(stack -> data.get(stack).stream())
                    .findFirst())
                .ifPresent(data -> {
                    final PoseStack pose = event.getPoseStack();
                    final Vec3 cameraPos = event.getCamera().getPosition();
                    
                    data.getSource().ifPresent(target -> highlightTarget(pose, cameraPos, target, SOURCE_FILL_COLOR));
                    data.getDestination().ifPresent(target -> highlightTarget(pose, cameraPos, target, DESTINATION_FILL_COLOR));
                });
        }
    }
    
    private static void highlightTarget(final PoseStack poseStack, final Vec3 cameraPos, final ItemTarget target, final Color color) {
        renderShape(poseStack, cameraPos, target.pos(), color);
    }

    private static void renderShape(final PoseStack matrices, final Vec3 cameraPos, final BlockPos pos, final Color color) {
        final Vector3d camDif = getCameraOffset(cameraPos, pos);
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder buffer = tessellator.getBuilder();

        // Setup rendering
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.disableDepthTest(); // See through
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        
        // Allow glass and other translucent/transparent objects to render properly
        drawOutlineBoxes(tessellator, matrices, buffer, camDif, color, OUTLINE_SHAPE);
        drawOutlineLines(tessellator, matrices, buffer, camDif, OUTLINE_COLOR, OUTLINE_SHAPE);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    /**
     * Draws boxes for an outline. Depth and blending should be set before this is called.
     */
    private static void drawOutlineBoxes(final Tesselator tessellator, final PoseStack matrices, final BufferBuilder buffer, final Vector3d camDif, final Color color, final VoxelShape outline) {
        final PoseStack.Pose entry = matrices.last();
        
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // Divide into each edge and draw all of them
        outline.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            // Fix Z fighting
            minX -= .001;
            minY -= .001;
            minZ -= .001;
            maxX += .001;
            maxY += .001;
            maxZ += .001;
            drawBox(entry, buffer, camDif, (float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, color);
        });
        tessellator.end();
    }

    /**
     * Renders an outline, sets shader and smooth lines.
     * Before calling blend and depth should be set
     */
    private static void drawOutlineLines(final Tesselator tessellator, final PoseStack matrices, final BufferBuilder buffer, final Vector3d camDif, final Color color, final VoxelShape outline) {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(OUTLINE_WIDTH);

        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        drawOutlineLine(tessellator, matrices.last(), buffer, camDif, color, outline);

        // Revert some changes
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    /**
     * Draws an outline. Setup should be done before this method is called.
     */
    private static void drawOutlineLine(final Tesselator tessellator, final PoseStack.Pose entry, final BufferBuilder buffer, final Vector3d camDif, final Color color, final VoxelShape outline) {
        outline.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
            // Fix Z fighting
            minX -= .001;
            minY -= .001;
            minZ -= .001;
            maxX += .001;
            maxY += .001;
            maxZ += .001;
            drawLine(entry, buffer, camDif, new Vector3d(minX, minY, minZ), new Vector3d(maxX, maxY, maxZ), color);
        });
        tessellator.end();
    }

    /**
     * Gets the camera offset from a position
     *
     * @param camera Camera position
     * @param pos    Position to get difference
     * @return Difference
     */
    private static Vector3d getCameraOffset(final Vec3 camera, final BlockPos pos) {
        final double xDif = (double) pos.getX() - camera.x;
        final double yDif = (double) pos.getY() - camera.y;
        final double zDif = (double) pos.getZ() - camera.z;
        return new Vector3d(xDif, yDif, zDif);
    }

    private static void drawBox(final PoseStack.Pose entry, final BufferBuilder buffer, final Vector3d cameraOffset, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, final Color color) {
        minX += (float) cameraOffset.x;
        minY += (float) cameraOffset.y;
        minZ += (float) cameraOffset.z;
        maxX += (float) cameraOffset.x;
        maxY += (float) cameraOffset.y;
        maxZ += (float) cameraOffset.z;

        final Matrix4f position = entry.pose();

        final float r = color.red();
        final float g = color.green();
        final float b = color.blue();
        final float a = color.alpha();

        // West
        buffer.vertex(position, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, minX, maxY, minZ).color(r, g, b, a).endVertex();

        // East
        buffer.vertex(position, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, maxX, maxY, maxZ).color(r, g, b, a).endVertex();

        // North
        buffer.vertex(position, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, maxX, maxY, minZ).color(r, g, b, a).endVertex();

        // South
        buffer.vertex(position, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, minX, maxY, maxZ).color(r, g, b, a).endVertex();

        // Top
        buffer.vertex(position, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, minX, maxY, minZ).color(r, g, b, a).endVertex();

        // Bottom
        buffer.vertex(position, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(position, maxX, minY, minZ).color(r, g, b, a).endVertex();
    }

    /**
     * Gets the normal line from a starting and ending point.
     *
     * @param start Starting point
     * @param end   Ending point
     * @return Normal line
     */
    private static Vector3d getNormalAngle(final Vector3d start, final Vector3d end) {
        double xLength = end.x - start.x;
        double yLength = end.y - start.y;
        double zLength = end.z - start.z;
        final double distance = Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);
        xLength /= distance;
        yLength /= distance;
        zLength /= distance;
        return new Vector3d(xLength, yLength, zLength);
    }

    /**
     * Draw's a line and adds the camera position difference to the render.
     * Rendering system should already be setup
     *
     * @param entry  Matrix entry
     * @param buffer Buffer builder that is already setup
     * @param camDif The position of render minus camera
     * @param start  Starting point
     * @param end    Ending point
     * @param color  Color to render
     */
    private static void drawLine(final PoseStack.Pose entry, final BufferBuilder buffer, final Vector3d camDif, final Vector3d start, final Vector3d end, final Color color) {
        final Vector3d startRaw = new Vector3d(start.x + camDif.x, start.y + camDif.y, start.z + camDif.z);
        final Vector3d endRaw = new Vector3d(end.x + camDif.x, end.y + camDif.y, end.z + camDif.z);
        drawLine(entry, buffer, startRaw, endRaw, color);
    }

    /**
     * This method doesn't do any of the {@link RenderSystem} setting up. Should be setup before call.
     * @param entry  Matrix entry
     * @param buffer Buffer builder that is already setup
     * @param start  Starting point
     * @param end    Ending point
     * @param color  Color to render
     */
    private static void drawLine(final PoseStack.Pose entry, final BufferBuilder buffer, final Vector3d start, final Vector3d end, final Color color) {
        final Vector3d normal = getNormalAngle(start, end);
        final float red = color.red();
        final float green = color.green();
        final float blue = color.blue();
        final float alpha = color.alpha();

        buffer.vertex(entry.pose(), (float) start.x, (float) start.y, (float) start.z)
            .color(red, green, blue, alpha)
            .normal(entry.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
            .endVertex();
        
        buffer.vertex(entry.pose(), (float) end.x, (float) end.y, (float) end.z)
            .color(red, green, blue, alpha)
            .normal(entry.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
            .endVertex();
    }
    
    private record Color(float red, float green, float blue, float alpha) {}

    private AllayMapBlockHighlighter() {}
}
