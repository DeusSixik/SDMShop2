package dev.sixik.sdmshop2.libs.platform;

import java.util.ArrayList;
import java.util.List;

public class SDMPlatform {

    private static List<Runnable> RELOADABLE = new ArrayList<>();

    public static void addReloading(Runnable runnable) {
        RELOADABLE.add(runnable);
    }

    public static void onReload() {
        for (Runnable runnable : RELOADABLE) {
            runnable.run();
        }
    }
}
