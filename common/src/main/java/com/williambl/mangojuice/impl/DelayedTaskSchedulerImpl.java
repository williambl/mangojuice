package com.williambl.mangojuice.impl;

import com.williambl.mangojuice.impl.platform.Services;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class DelayedTaskSchedulerImpl {
    private static final List<Task> TASKS = new ArrayList<>();
    private static final List<Task> TASKS_TO_ADD = new ArrayList<>();

    /**
     * Schedule a new delayed task.
     * @param server    the minecraft server
     * @param delay     the amount of ticks to wait until running
     * @param task      the function to run
     */
    public static void scheduleTask(MinecraftServer server, int delay, Runnable task) {
        TASKS_TO_ADD.add(new Task(server.getTickCount()+delay, task));
    }

    public static void onTick(MinecraftServer server) {
        int ticks = server.getTickCount();
        List<Task> finishedTasks = new ArrayList<>();
        TASKS.addAll(TASKS_TO_ADD);
        TASKS_TO_ADD.clear();
        for (Task task : TASKS) {
            if (task.scheduledTime() <= ticks) {
                task.task().run();
                finishedTasks.add(task);
            }
        }
        TASKS.removeAll(finishedTasks);
    }

    public static void onServerShutdown(MinecraftServer server) {
        TASKS.clear();
        TASKS_TO_ADD.clear();
    }

    public static void init() {
        Services.EVENTS.onServerTick(DelayedTaskSchedulerImpl::onTick);
        Services.EVENTS.onServerStopping(DelayedTaskSchedulerImpl::onServerShutdown);
    }

    private record Task(int scheduledTime, Runnable task) {}
}
