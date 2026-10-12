package name.modid.forge;

import name.modid.ChyClient;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;

final class ChyForgeClient {
    private ChyForgeClient() {}
    static void initialize() {
        RegisterClientCommandsEvent.BUS.addListener(event ->
            ChyClient.registerCommands(event.getDispatcher(), (source, message) -> source.sendSuccess(() -> message, false)));
        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> ChyClient.tick());
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> ChyClient.reset());
        ChyClient.loaded("Forge");
    }
}
