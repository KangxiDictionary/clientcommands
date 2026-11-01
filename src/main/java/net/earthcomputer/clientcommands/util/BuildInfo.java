package net.earthcomputer.clientcommands.util;

import net.neoforged.fml.ModList;

public final class BuildInfo {
    private BuildInfo() {
    }

    public static final String VERSION;
    public static final String BRANCH = "neoforge-clean";
    public static final String SHORT_COMMIT_HASH = "unknown";
    public static final String COMMIT_HASH = "unknown";

    static {
        String version = "unknown";
        try {
            version = ModList.get().getModContainerById("clientcommands")
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
        } catch (RuntimeException e) {
            // ignore
        }
        VERSION = version;
    }
}
