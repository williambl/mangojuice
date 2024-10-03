package com.williambl.mangojuice.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mangojuice {

    public static final String MOD_ID = "mangojuice";
    public static final String MOD_NAME = "mangojuice";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        DelayedTaskSchedulerImpl.init();
    }
}