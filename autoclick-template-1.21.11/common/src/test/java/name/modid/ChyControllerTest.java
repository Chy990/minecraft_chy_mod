package name.modid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChyControllerTest {
    private final ChyState state = new ChyState();
    private final ChyController controller = new ChyController(state);
    private final FakeActions actions = new FakeActions();

    @Test
    void bothButtonsShareTargetRefreshButKeepIndependentTimers() {
        state.leftClick.start(2);
        state.rightClick.start(3);
        for (int i = 0; i < 6; i++) controller.tick(actions);
        assertEquals(3, actions.left);
        assertEquals(2, actions.right);
        assertEquals(4, actions.refreshes);
    }

    @Test
    void pauseDeathAndSpectatorGateFreezeCountdowns() {
        state.leftClick.start(2);
        controller.tick(actions);
        actions.canAct = false;
        for (int i = 0; i < 50; i++) controller.tick(actions);
        assertEquals(0, actions.left);
        assertEquals(0, actions.refreshes);
        assertTrue(state.leftClick.isEnabled());
        actions.canAct = true;
        controller.tick(actions);
        assertEquals(1, actions.left);
    }

    @Test
    void usingItemSkipsRightClickWithoutStoppingLeftOrAccumulatingClicks() {
        state.leftClick.start(1);
        state.rightClick.start(2);
        actions.usingItem = true;
        for (int i = 0; i < 4; i++) controller.tick(actions);
        assertEquals(4, actions.left);
        assertEquals(0, actions.right);
        actions.usingItem = false;
        controller.tick(actions);
        assertEquals(0, actions.right);
        controller.tick(actions);
        assertEquals(1, actions.right);
    }

    @Test
    void missingWorldResetsEverythingEvenWhenCannotAct() {
        state.leftClick.start(1);
        state.rightClick.start(1);
        state.toggleSneaking();
        actions.hasWorld = false;
        actions.canAct = false;
        controller.tick(actions);
        assertFalse(state.leftClick.isEnabled());
        assertFalse(state.rightClick.isEnabled());
        assertFalse(state.isSneaking());
        actions.hasWorld = true;
        actions.canAct = true;
        controller.tick(actions);
        assertEquals(0, actions.refreshes);
    }

    private static final class FakeActions implements ChyController.Actions {
        boolean hasWorld = true, canAct = true, usingItem;
        int left, right, refreshes;
        public boolean hasWorld() { return hasWorld; }
        public boolean canAct() { return canAct; }
        public boolean isUsingItem() { return usingItem; }
        public void refreshTarget() { refreshes++; }
        public void leftClick() { left++; }
        public void rightClick() { right++; }
    }
}
