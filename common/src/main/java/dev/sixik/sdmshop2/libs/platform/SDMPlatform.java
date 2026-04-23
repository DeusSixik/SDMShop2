package dev.sixik.sdmshop2.libs.platform;

import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class SDMPlatform {

    private static final List<ServerOperation> OPERATIONS = new ArrayList<>();

    private static final SDMAutoSaveManager AUTO_SAVE_MANAGER = SDMAutoSaveManager.INSTANCE;

    public static void onReload() {
        for (ServerOperation runnable : OPERATIONS) {
            runnable.onReload();
        }
    }

    public static void addTask(Object o) {
        if(o instanceof ServerOperation serverOperation)
            addOperation(serverOperation);
        if(o instanceof ThreadingOperationTimeSave timeSaveTask)
            addTimeSaveTask(timeSaveTask);
    }

    public static void addOperation(ServerOperation operation) {
        OPERATIONS.add(operation);
    }

    public static void addTimeSaveTask(ThreadingOperationTimeSave task) {
        AUTO_SAVE_MANAGER.registerTask(task);
    }

    public static void onServerOperationLoad(MinecraftServer server) {


        for (ServerOperation operation : OPERATIONS) {
            operation.onServerStart(server);
        }
    }

    public static void onServerOperationUnload(MinecraftServer server) {
        for (ServerOperation operation : OPERATIONS) {
            operation.onServerStop(server);
        }
    }

    public static void init() {
        addOperation(AUTO_SAVE_MANAGER);
        LifecycleEvent.SERVER_BEFORE_START.register(SDMPlatform::onServerOperationLoad);
        LifecycleEvent.SERVER_STOPPED.register(SDMPlatform::onServerOperationUnload);
    }
}
