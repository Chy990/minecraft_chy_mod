package name.modid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.function.BiConsumer;

/** Identical command trees and messages for every loader, without Minecraft types. */
public final class ChyCommands {
    private ChyCommands() {}

    public static <S> void register(CommandDispatcher<S> dispatcher, ChyState state,
                                    BiConsumer<S, String> feedback) {
        registerClick(dispatcher, "left_click", "左", state.leftClick, feedback);
        registerClick(dispatcher, "right_click", "右", state.rightClick, feedback);
        dispatcher.register(LiteralArgumentBuilder.<S>literal("shift").executes(context -> {
            state.toggleSneaking();
            feedback.accept(context.getSource(), state.isSneaking() ? "自动潜行已开启" : "自动潜行已关闭");
            return 1;
        }));
        dispatcher.register(LiteralArgumentBuilder.<S>literal("chy_help").executes(context -> {
            for (String line : HELP) feedback.accept(context.getSource(), line);
            return 1;
        }));
    }

    private static <S> void registerClick(CommandDispatcher<S> dispatcher, String command,
                                          String button, ClickState state, BiConsumer<S, String> feedback) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal(command)
            .executes(context -> {
                state.toggle();
                feedback.accept(context.getSource(), clickMessage(button, state));
                return 1;
            })
            .then(RequiredArgumentBuilder.<S, Integer>argument("tick", IntegerArgumentType.integer(1))
                .executes(context -> {
                    state.start(IntegerArgumentType.getInteger(context, "tick"));
                    feedback.accept(context.getSource(), clickMessage(button, state));
                    return 1;
                })));
    }

    private static String clickMessage(String button, ClickState state) {
        return state.isEnabled() ? "自动" + button + "键，间隔" + state.intervalTicks() + " tick"
            : "自动" + button + "键已关闭";
    }

    private static final String[] HELP = {
        "=== chy 帮助 ===",
        "/left_click - 开启/关闭自动左键；攻击准星指向的实体，无实体时挥手，不挖方块。",
        "/right_click - 开启/关闭自动右键；手持方块时放置，也可触发原版右键交互。",
        "/left_click <tick>、/right_click <tick> - 开启或调整间隔，参数为正整数 tick（最小 1）。",
        "两个功能独立计时；每次无参数开启均恢复 12 tick。例如 /right_click 20：每 20 tick 右键一次。",
        "开启提示：自动左/右键，间隔xx tick。再次输入相应无参数命令即可关闭。",
        "/shift - 开启/关闭持续潜行，相当于一直按住潜行键，可配合右键放置方块。",
        "打开聊天、背包或菜单不关闭功能；单人世界暂停时无法继续实际操作。退出世界后全部关闭。",
        "/chy_help - 查看帮助。旧命令 /auto_attack 已改为 /left_click。"
    };
}
