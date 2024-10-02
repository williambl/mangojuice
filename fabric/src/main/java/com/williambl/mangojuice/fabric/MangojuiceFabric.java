package com.williambl.mangojuice.fabric;

import com.williambl.mangojuice.impl.Mangojuice;
import net.fabricmc.api.ModInitializer;

public class MangojuiceFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // Use Fabric to bootstrap the Common mod.
        Mangojuice.LOG.info("Hello Fabric world!");
        Mangojuice.init();
    }
}
