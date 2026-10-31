package net.earthcomputer.clientcommands.compat;

import java.nio.file.Path;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

/**
 * NeoForge replacement for the small part of FabricLoader used by the mod.
 */
public final class CPlatform {
    public static final CPlatform INSTANCE = new CPlatform();

    private CPlatform() {
    }

    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public boolean isDevelopmentEnvironment() {
        return !net.neoforged.fml.loading.FMLLoader.getCurrent().isProduction();
    }
}
