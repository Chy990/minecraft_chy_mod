package name.modid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChyCommandsTest {
    private final ChyState state = new ChyState();
    private final CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
    private final Object source = new Object();
    private final List<String> messages = new ArrayList<>();

    ChyCommandsTest() {
        ChyCommands.register(dispatcher, state, (s, text) -> {
            assertSame(source, s);
            messages.add(text);
        });
    }

    @Test
    void commandsToggleResetAndAdjustIndependently() throws Exception {
        dispatcher.execute("left_click 40", source);
        assertEquals("自动左键，间隔40 tick", messages.getLast());
        dispatcher.execute("right_click 1", source);
        dispatcher.execute("shift", source);
        dispatcher.execute("left_click", source);
        assertEquals("自动左键已关闭", messages.getLast());
        dispatcher.execute("left_click", source);
        assertEquals("自动左键，间隔12 tick", messages.getLast());
        assertEquals(12, state.leftClick.intervalTicks());
        assertEquals(1, state.rightClick.intervalTicks());
        assertTrue(state.isSneaking());
        dispatcher.execute("shift", source);
        assertFalse(state.isSneaking());
    }

    @Test
    void invalidCommandArgumentsNeverModifyEitherButton() throws Exception {
        for (String command : List.of("left_click", "right_click")) {
            dispatcher.execute(command + " 2", source);
            ClickState click = command.equals("left_click") ? state.leftClick : state.rightClick;
            assertFalse(click.tick());
            int reports = messages.size();
            for (String value : List.of("0", "-1", "0.6", "abc", "2147483648", "1 extra")) {
                assertThrows(CommandSyntaxException.class, () -> dispatcher.execute(command + " " + value, source));
                assertTrue(click.isEnabled());
                assertEquals(2, click.intervalTicks());
            }
            assertEquals(reports, messages.size());
            assertTrue(click.tick());
        }
    }

    @Test
    void maximumIntervalAndHelpWorkAndLegacyCommandsAreAbsent() throws Exception {
        dispatcher.execute("right_click 2147483647", source);
        assertEquals(Integer.MAX_VALUE, state.rightClick.intervalTicks());
        dispatcher.execute("chy_help", source);
        assertTrue(messages.stream().anyMatch(s -> s.contains("/shift")));
        assertTrue(messages.stream().anyMatch(s -> s.contains("退出世界后全部关闭")));
        for (String command : List.of("auto_attack", "chy_autoattack", "chy_deepseek", "chy_say")) {
            assertNull(dispatcher.getRoot().getChild(command));
        }
    }
}
