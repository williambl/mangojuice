package com.williambl.mangojuice.impl.platform.services;

import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public interface IEvents {
    void onServerTick(Consumer<MinecraftServer> listener);
    void onServerStopping(Consumer<MinecraftServer> listener);
}
