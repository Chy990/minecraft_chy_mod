package name.modid.neoforge;

import name.modid.ChyClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "chy", dist = Dist.CLIENT)
public final class ChyNeoForge {
    public ChyNeoForge() {
        NeoForge.EVENT_BUS.addListener(ChyNeoForge::registerCommands);
        NeoForge.EVENT_BUS.addListener(ChyNeoForge::tick);
        NeoForge.EVENT_BUS.addListener(ChyNeoForge::disconnect);
        ChyClient.loaded("NeoForge");
    }
    private static void registerCommands(RegisterClientCommandsEvent event) {
        ChyClient.registerCommands(event.getDispatcher(), (source, message) -> source.sendSuccess(() -> message, false));
    }
    private static void tick(ClientTickEvent.Post event) { ChyClient.tick(); }
    private static void disconnect(ClientPlayerNetworkEvent.LoggingOut event) { ChyClient.reset(); }
}
