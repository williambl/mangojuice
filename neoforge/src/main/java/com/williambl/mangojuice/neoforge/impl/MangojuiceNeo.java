package com.williambl.mangojuice.neoforge.impl;


import com.williambl.mangojuice.impl.Mangojuice;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Mangojuice.MOD_ID)
public class MangojuiceNeo {

    public MangojuiceNeo(IEventBus eventBus) {
        // Use NeoForge to bootstrap the Common mod.
        Mangojuice.LOG.info("Hello NeoForge world!");
        Mangojuice.init();
    }
}