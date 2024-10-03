package com.williambl.mangojuice.api;

import com.williambl.mangojuice.impl.DelayedTaskSchedulerImpl;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * Handles running functions after a given amount of ticks.
 */
public final class DelayedTaskScheduler {
    /**
     * Schedule a new delayed task.
     * @param server    the minecraft server
     * @param delay     the amount of ticks to wait until running
     * @param task      the function to run
     */
    public static void scheduleTask(MinecraftServer server, int delay, Runnable task) {
        DelayedTaskSchedulerImpl.scheduleTask(server, delay, task);
    }

    /**
     * Schedule a delayed task which repeats itself.
     * @param server            the minecraft server
     * @param delay             the amount of ticks to wait until running / between runs
     * @param task              the task (taking in the iteration number)
     * @param shouldContinue    whether the task should continue to repeat, given the iteration number
     */
    public static void scheduleRepeatingTask(MinecraftServer server, int initalDelay, int delay, Consumer<Integer> task, IntFunction<Boolean> shouldContinue) {
        scheduleTask(server, initalDelay, new Runnable() {
            int iterations = 0;
            @Override
            public void run() {
                task.accept(this.iterations);
                if (shouldContinue.apply(this.iterations++)) {
                    scheduleTask(server, delay, this);
                }
            }
        });
    }
}
