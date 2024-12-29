package com.williambl.mangojuice.impl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.williambl.mangojuice.api.client.MangoOverlays;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class MangoOverlaysImpl {
    private static final List<MangoOverlays.OverlayRenderer> RENDERERS = new ArrayList<>();

    public static void registerOverlay(MangoOverlays.OverlayRenderer renderer) {
        RENDERERS.add(renderer);
    }

    public static void renderOverlays(Minecraft client, float partialTicks, PoseStack poseStack) {
        for (var overlay : RENDERERS) {
            overlay.renderOverlay(client, partialTicks, poseStack);
        }
    }
}
