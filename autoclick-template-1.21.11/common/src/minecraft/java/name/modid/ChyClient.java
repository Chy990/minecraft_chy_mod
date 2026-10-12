package name.modid;

import com.mojang.brigadier.CommandDispatcher;
import java.util.function.BiConsumer;
import name.modid.mixin.MinecraftClientInvoker;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared vanilla client integration, compiled with official mappings on all loaders. */
public final class ChyClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("chy");
    private static final ChyState STATE = new ChyState();
    private static final ChyController CONTROLLER = new ChyController(STATE);
    private static final ChyController.Actions ACTIONS = new MinecraftActions();

    private ChyClient() {}
    public static boolean isAutoSneaking() { return STATE.isSneaking(); }
    public static void reset() { STATE.reset(); }
    public static void tick() { CONTROLLER.tick(ACTIONS); }

    public static <S> void registerCommands(CommandDispatcher<S> dispatcher, BiConsumer<S, Component> sender) {
        ChyCommands.register(dispatcher, STATE, (source, message) -> {
            sender.accept(source, Component.literal(message).withStyle(ChatFormatting.RED));
            LOGGER.info("{}", message);
        });
    }

    public static void loaded(String loader) {
        LOGGER.info("chy ({}) 已加载，使用 /left_click、/right_click、/shift 或 /chy_help。", loader);
    }

    private static final class MinecraftActions implements ChyController.Actions {
        private Minecraft client() { return Minecraft.getInstance(); }
        public boolean hasWorld() {
            return client().player != null && client().level != null && client().gameMode != null;
        }
        public boolean canAct() {
            return !client().isPaused() && client().player.isAlive() && !client().player.isSpectator();
        }
        public boolean isUsingItem() { return client().player.isUsingItem(); }
        public void refreshTarget() { client().gameRenderer.pick(1.0F); }
        public void leftClick() {
            var client = client();
            if (client.crosshairPickEntity != null) client.gameMode.attack(client.player, client.crosshairPickEntity);
            client.player.swing(InteractionHand.MAIN_HAND);
        }
        public void rightClick() { ((MinecraftClientInvoker) client()).chy$doItemUse(); }
    }
}
