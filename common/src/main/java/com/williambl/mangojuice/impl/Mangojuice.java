package com.williambl.mangojuice.impl;

import com.williambl.mangojuice.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mangojuice {

    public static final String MOD_ID = "mangojuice";
    public static final String MOD_NAME = "mangojuice";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));
    }
}