package com.williambl.mangojuice.fabric.impl.platform;

import com.williambl.mangojuice.impl.platform.services.IEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public class FabricEvents implements IEvents {
    @Override
    public void onServerTick(Consumer<MinecraftServer> listener) {
        ServerTickEvents.START_SERVER_TICK.register(listener::accept);
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> listener) {
        ServerLifecycleEvents.SERVER_STOPPING.register(listener::accept);
    }
}
