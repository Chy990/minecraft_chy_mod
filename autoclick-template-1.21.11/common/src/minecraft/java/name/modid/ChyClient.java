package name.modid;

import com.mojang.brigadier.CommandDispatcher;
import java.util.function.BiConsumer;
import name.modid.mixin.MinecraftClientInvoker;
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
    private static final HungerAlert HUNGER_ALERT = new HungerAlert();
    private static final ChyController.Actions ACTIONS = new MinecraftActions();

    private ChyClient() {}
    public static boolean isAutoSneaking() { return STATE.isSneaking(); }
    public static void reset() {
        STATE.reset();
        HUNGER_ALERT.reset();
    }
    public static void tick() {
        CONTROLLER.tick(ACTIONS);
        checkHunger();
    }

    private static void checkHunger() {
        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null || client.level == null || !player.isAlive()
                || player.isSpectator() || player.isCreative()) {
            HUNGER_ALERT.reset();
            return;
        }
        if (client.isPaused()) return;
        int hunger = player.getFoodData().getFoodLevel();
        if (HUNGER_ALERT.update(hunger)) {
            // Local chat only: does not send a message or perform an action on the server.
            client.gui.getChat().addMessage(Component.literal(
                "饥饿提醒：当前饥饿值 " + hunger + "/20，血量 "
                    + player.getHealth() + "/" + player.getMaxHealth() + "。"));
        }
    }

    public static <S> void registerCommands(CommandDispatcher<S> dispatcher, BiConsumer<S, Component> sender) {
        ChyCommands.register(dispatcher, STATE, (source, message) -> {
            sender.accept(source, Component.literal(message));
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
