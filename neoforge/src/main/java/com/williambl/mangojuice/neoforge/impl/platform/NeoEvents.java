package com.williambl.mangojuice.neoforge.impl.platform;

import com.williambl.mangojuice.impl.platform.services.IEvents;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.function.Consumer;

public class NeoEvents implements IEvents {
    @Override
    public void onServerTick(Consumer<MinecraftServer> listener) {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent event) -> listener.accept(event.getServer()));
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> listener) {
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> listener.accept(event.getServer()));
    }
}
