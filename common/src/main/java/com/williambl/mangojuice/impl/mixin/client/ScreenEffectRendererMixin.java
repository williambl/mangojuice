package com.williambl.mangojuice.impl.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.williambl.mangojuice.impl.client.MangoOverlaysImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
    @Inject(method = "renderScreenEffect", at = @At("HEAD"))
    private static void mangojuice$renderCustomOverlays(Minecraft pMinecraft, PoseStack pPoseStack, CallbackInfo ci) {
        MangoOverlaysImpl.renderOverlays(pMinecraft, pMinecraft.getTimer().getGameTimeDeltaPartialTick(false), pPoseStack);
    }
}
