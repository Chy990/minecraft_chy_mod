package name.modid.fabric;

import name.modid.ChyClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class ChyFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            ChyClient.registerCommands(dispatcher, (source, message) -> source.sendFeedback(message)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> ChyClient.tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ChyClient.reset());
        ChyClient.loaded("Fabric");
    }
}
