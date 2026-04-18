package dev.sixik.sdmshop2.libs.platform;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Singleton manager responsible for scheduling and executing background auto-save operations.
 * Prevents the main server thread from hanging during heavy I/O operations.
 */
public class SDMAutoSaveManager implements ServerOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(SDMAutoSaveManager.class);

    public static final SDMAutoSaveManager INSTANCE = new SDMAutoSaveManager();

    private final List<ThreadingOperationTimeSave> saveTasks = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService executorService;

    private SDMAutoSaveManager() {}

    /**
     * Registers a new task to be executed periodically.
     * Must be called before the server starts or during initialization.
     *
     * @param task The auto-save task implementation.
     */
    public void registerTask(ThreadingOperationTimeSave task) {
        saveTasks.add(task);
    }

    @Override
    public void onServerStart(MinecraftServer server) {
        if (saveTasks.isEmpty()) {
            return;
        }

        LOGGER.info("Initializing SDM background auto-save manager with {} tasks.", saveTasks.size());

        executorService = Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable, "SDM-AutoSave-Thread");
            thread.setDaemon(true);
            return thread;
        });

        for (ThreadingOperationTimeSave task : saveTasks) {

            int intervalSeconds = task.getDataSaveTimeSeconds();

            if (intervalSeconds > 0) {
                executorService.scheduleAtFixedRate(
                        () -> executeSave(task),
                        intervalSeconds,
                        intervalSeconds,
                        TimeUnit.SECONDS
                );
            } else {
                LOGGER.warn("Skipping auto-save task {} due to invalid interval: {} seconds.",
                        task.getClass().getSimpleName(), intervalSeconds);
            }
        }
    }

    /**
     * Safe wrapper for task execution to prevent the ScheduledExecutorService
     * from silently suppressing exceptions and halting future executions.
     */
    private void executeSave(ThreadingOperationTimeSave task) {
        try {
            task.onDataStartSave();
        } catch (Exception e) {
            LOGGER.error("An error occurred during background data saving for task: {}", task.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void onServerStop(MinecraftServer server) {
        if (executorService != null && !executorService.isShutdown()) {
            LOGGER.info("Shutting down SDM background auto-save manager...");
            executorService.shutdown();

            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.warn("Auto-save tasks did not terminate gracefully within 5 seconds. Forcing shutdown.");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting for auto-save tasks to complete. Forcing shutdown.", e);
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}