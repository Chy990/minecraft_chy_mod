package name.modid.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("chy")
public final class ChyForge {
    public ChyForge() {
        // Keep all Minecraft client references in a separate class for dedicated servers.
        if (FMLEnvironment.dist == Dist.CLIENT) ChyForgeClient.initialize();
    }
}
