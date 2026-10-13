package name.modid;

import com.mojang.brigadier.CommandDispatcher;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import name.modid.mixin.MinecraftClientInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared vanilla client integration, compiled with official mappings on all loaders. */
public final class ChyClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("chy");
    private static final ChyState STATE = new ChyState();
    private static final ChyController CONTROLLER = new ChyController(STATE);
    private static final AutoEatController AUTO_EAT = new AutoEatController();
    private static final AutoEatController.Actions EAT_ACTIONS = new EatingActions();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final ChyController.Actions ACTIONS = new MinecraftActions();

    private ChyClient() {}
    public static boolean isAutoSneaking() { return STATE.isSneaking(); }
    public static void reset() {
        STATE.reset();
        AUTO_EAT.reset(EAT_ACTIONS);
    }
    public static void tick() {
        if (!ACTIONS.hasWorld()) {
            reset();
            return;
        }
        if (!AUTO_EAT.tick(STATE.isStayingZhuren(), EAT_ACTIONS)) CONTROLLER.tick(ACTIONS);
    }

    public static boolean shouldKeepEating() {
        return STATE.isStayingZhuren() && AUTO_EAT.shouldKeepUsing(EAT_ACTIONS);
    }

    private static void localMessage(String message) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
    }

    public static <S> void registerCommands(CommandDispatcher<S> dispatcher, BiConsumer<S, Component> sender) {
        ChyCommands.register(dispatcher, STATE, (source, message) -> {
            if (!STATE.isStayingZhuren()) AUTO_EAT.reset(EAT_ACTIONS);
            sender.accept(source, Component.literal(message));
            LOGGER.info("{}", message);
        });
    }

    public static void loaded(String loader) {
        LOGGER.info("chy ({}) 已加载，使用 /left_click、/right_click、/shift、/stay_zhuren 或 /chy_help。", loader);
    }

    private static final class EatingActions implements AutoEatController.Actions {
        private Minecraft client() { return Minecraft.getInstance(); }
        public boolean canEat() {
            var player = client().player;
            return player != null && client().level != null && client().gameMode != null
                && player.isAlive() && !player.isSpectator() && !player.isCreative();
        }
        public boolean isPaused() { return client().isPaused(); }
        public int foodLevel() { return client().player.getFoodData().getFoodLevel(); }
        public int offhandCarrotCount() {
            var stack = client().player.getOffhandItem();
            return stack.is(Items.GOLDEN_CARROT) ? stack.getCount() : 0;
        }
        public boolean isUsingItem() { return client().player.isUsingItem(); }
        public boolean isEatingOffhandCarrot() {
            var player = client().player;
            return player != null && player.isUsingItem()
                && player.getUsedItemHand() == InteractionHand.OFF_HAND
                && player.getUseItem().is(Items.GOLDEN_CARROT);
        }
        public boolean startEating() {
            // Use the offhand item directly, so a main-hand tool or targeted container cannot intercept it.
            client().gameMode.stopDestroyBlock();
            client().gameMode.useItem(client().player, InteractionHand.OFF_HAND);
            return isEatingOffhandCarrot();
        }
        public void stopEating() {
            if (client().gameMode != null && isEatingOffhandCarrot()) {
                client().gameMode.releaseUsingItem(client().player);
            }
        }
        public void reportHunger() {
            var player = client().player;
            localMessage("[" + LocalTime.now().format(TIME_FORMAT) + "] 饥饿提醒：当前饥饿值 "
                + foodLevel() + "/20，血量 " + player.getHealth() + "/" + player.getMaxHealth() + "。");
        }
        public void reportMissingFood() {
            localMessage("副手没有金胡萝卜，本次未自动进食。");
        }
        public void reportFailedAttempt() {
            localMessage("本次自动进食未能完成，已停止尝试。");
        }
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
