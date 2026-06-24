package dev.Jules.foliaRegionBlockOverride.model;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Material;

import java.util.concurrent.atomic.AtomicReference;

public final class TrackedBlock {

    private final Material material;
    private final boolean allowBreak;
    private final AtomicReference<ScheduledTask> task = new AtomicReference<>();

    public TrackedBlock(Material material, boolean allowBreak) {
        this.material = material;
        this.allowBreak = allowBreak;
    }

    public Material material() {
        return material;
    }

    public boolean allowBreak() {
        return allowBreak;
    }

    public void task(ScheduledTask scheduledTask) {
        task.set(scheduledTask);
    }

    public void cancelTask() {
        ScheduledTask scheduledTask = task.getAndSet(null);
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
    }
}
