package net.earthcomputer.clientcommands.util;

import net.neoforged.fml.ModList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Build metadata, written into the jar by the {@code generateBuildInfo} Gradle task.
 */
public final class BuildInfo {
    private BuildInfo() {
    }

    public static final String VERSION;
    public static final String BRANCH;
    public static final String SHORT_COMMIT_HASH;
    public static final String COMMIT_HASH;

    static {
        Properties properties = new Properties();
        try (InputStream in = BuildInfo.class.getResourceAsStream("/clientcommands-buildinfo.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException | RuntimeException e) {
            // fall through to the defaults
        }

        String version = "unknown";
        try {
            version = ModList.get().getModContainerById("clientcommands")
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
        } catch (RuntimeException e) {
            // not in a modded environment
        }
        VERSION = version;
        BRANCH = properties.getProperty("branch", "unknown");
        COMMIT_HASH = properties.getProperty("commit", "unknown");
        SHORT_COMMIT_HASH = COMMIT_HASH.length() >= 7 ? COMMIT_HASH.substring(0, 7) : COMMIT_HASH;
    }
}
