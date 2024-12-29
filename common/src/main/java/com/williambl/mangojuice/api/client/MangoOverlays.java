package com.williambl.mangojuice.api.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.williambl.mangojuice.impl.client.MangoOverlaysImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.Function;
import java.util.function.Predicate;

//todo overlays which fit perfectly to screen
public class MangoOverlays {
    public static void registerTextureOverlay(Function<OverlayRenderContext, @Nullable TextureAtlasSprite> sprite, Function<OverlayRenderContext, Integer> color) {
        registerOverlay((client, partialTicks, stack) -> {
            var ctx = new OverlayRenderContext(client, partialTicks, stack);
            @Nullable TextureAtlasSprite atlasSprite = sprite.apply(ctx);
            if (atlasSprite == null) {
                return;
            }
            Integer colorValue = color.apply(ctx);
            int red = FastColor.ARGB32.red(colorValue);
            int green = FastColor.ARGB32.green(colorValue);
            int blue = FastColor.ARGB32.blue(colorValue);
            int alpha = FastColor.ARGB32.alpha(colorValue);
            RenderSystem.setShaderTexture(0, atlasSprite.atlasLocation());
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            var pose = stack.last();
            var bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.addVertex(pose, -1.0F, -1.0F, -0.5F).setUv(atlasSprite.getU1(), atlasSprite.getV1()).setColor(red, green, blue, alpha);
            bufferBuilder.addVertex(pose, 1.0F, -1.0F, -0.5F).setUv(atlasSprite.getU0(), atlasSprite.getV1()).setColor(red, green, blue, alpha);
            bufferBuilder.addVertex(pose, 1.0F, 1.0F, -0.5F).setUv(atlasSprite.getU0(), atlasSprite.getV0()).setColor(red, green, blue, alpha);
            bufferBuilder.addVertex(pose, -1.0F, 1.0F, -0.5F).setUv(atlasSprite.getU1(), atlasSprite.getV0()).setColor(red, green, blue, alpha);
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            RenderSystem.disableBlend();
        });
    }

    public static void registerTextureOverlay(Function<OverlayRenderContext, @Nullable TextureAtlasSprite> sprite) {
        registerTextureOverlay(sprite, $ -> 0xffffffff);
    }

    public static void registerConditionalTextureOverlay(Predicate<OverlayRenderContext> predicate, Function<OverlayRenderContext, TextureAtlasSprite> sprite, Function<OverlayRenderContext, Integer> color) {
        registerTextureOverlay(ctx -> predicate.test(ctx) ? sprite.apply(ctx) : null, color);
    }

    public static void registerConditionalTextureOverlay(Predicate<OverlayRenderContext> predicate, Function<OverlayRenderContext, TextureAtlasSprite> sprite) {
        registerConditionalTextureOverlay(predicate, sprite, $ -> 0xffffffff);
    }

    public static void registerColorOverlay(Function<OverlayRenderContext, @Nullable Integer> color) {
        registerOverlay((client, partialTicks, stack) -> {
            Integer colorValue = color.apply(new OverlayRenderContext(client, partialTicks, stack));
            if (colorValue == null) {
                return;
            }
            int red = FastColor.ARGB32.red(colorValue);
            int green = FastColor.ARGB32.green(colorValue);
            int blue = FastColor.ARGB32.blue(colorValue);
            int alpha = FastColor.ARGB32.alpha(colorValue);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            var pose = stack.last();
            var bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.addVertex(pose, -1.0F, -1.0F, -0.5F).setColor(red, green, blue, alpha);
            bufferBuilder.addVertex(pose, 1.0F, -1.0F, -0.5F).setColor(red, green, blue, alpha);
            bufferBuilder.addVertex(pose, 1.0F, 1.0F, -0.5F).setColor(red, green, blue, alpha);
            bufferBuilder.addVertex(pose, -1.0F, 1.0F, -0.5F).setColor(red, green, blue, alpha);
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            RenderSystem.disableBlend();
        });
    }

    public static void registerConditionalColorOverlay(Predicate<OverlayRenderContext> predicate, Function<OverlayRenderContext, Integer> color) {
        registerColorOverlay(ctx -> predicate.test(ctx) ? color.apply(ctx) : null);
    }

    public static void registerGradientOverlay(Function<OverlayRenderContext, int @Nullable[]> color) {
        registerOverlay((client, partialTicks, stack) -> {
            int[] colorValues = color.apply(new OverlayRenderContext(client, partialTicks, stack));
            if (colorValues == null || colorValues.length != 4) {
                return;
            }
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            var pose = stack.last();
            var bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.addVertex(pose, -1.0F, -1.0F, -0.5F).setColor(FastColor.ARGB32.red(colorValues[0]), FastColor.ARGB32.green(colorValues[0]), FastColor.ARGB32.blue(colorValues[0]), FastColor.ARGB32.alpha(colorValues[0]));
            bufferBuilder.addVertex(pose, 1.0F, -1.0F, -0.5F).setColor(FastColor.ARGB32.red(colorValues[1]), FastColor.ARGB32.green(colorValues[1]), FastColor.ARGB32.blue(colorValues[1]), FastColor.ARGB32.alpha(colorValues[1]));
            bufferBuilder.addVertex(pose, 1.0F, 1.0F, -0.5F).setColor(FastColor.ARGB32.red(colorValues[0]), FastColor.ARGB32.green(colorValues[2]), FastColor.ARGB32.blue(colorValues[2]), FastColor.ARGB32.alpha(colorValues[2]));
            bufferBuilder.addVertex(pose, -1.0F, 1.0F, -0.5F).setColor(FastColor.ARGB32.red(colorValues[0]), FastColor.ARGB32.green(colorValues[3]), FastColor.ARGB32.blue(colorValues[3]), FastColor.ARGB32.alpha(colorValues[3]));
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            RenderSystem.disableBlend();
        });
    }

    public static void registerConditionalGradientOverlay(Predicate<OverlayRenderContext> predicate, Function<OverlayRenderContext, int[]> colors) {
        registerGradientOverlay(ctx -> predicate.test(ctx) ? colors.apply(ctx) : null);
    }

    public static void registerOverlay(OverlayRenderer renderer) {
        MangoOverlaysImpl.registerOverlay(renderer);
    }

    public record OverlayRenderContext(Minecraft client, float partialTicks, PoseStack stack) {}

    @FunctionalInterface
    public interface OverlayRenderer {
        void renderOverlay(Minecraft client, float partialTicks, PoseStack stack);
    }
}
