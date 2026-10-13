package name.modid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AutoEatControllerTest {
    private final AutoEatController controller = new AutoEatController();
    private final FakeActions actions = new FakeActions();

    @Test
    void disabledDoesNotNotifyOrEatEvenWhenHungry() {
        for (int i = 0; i < 100; i++) assertFalse(controller.tick(false, actions));
        assertEquals(0, actions.reports);
        assertEquals(0, actions.starts);
        assertEquals(0, actions.stops);
    }

    @Test
    void eatsOnceAndReleasesEvenIfServerStillReportsUsingAfterStackDecrements() {
        assertTrue(controller.tick(true, actions));
        assertTrue(controller.shouldKeepUsing(actions));
        for (int i = 0; i < 32; i++) assertTrue(controller.tick(true, actions));
        actions.carrots--;
        assertFalse(controller.shouldKeepUsing(actions));
        assertTrue(controller.tick(true, actions));
        assertEquals(1, actions.stops);
        for (int i = 0; i < 100; i++) assertFalse(controller.tick(true, actions));
        assertEquals(1, actions.starts);
        assertEquals(1, actions.reports);
    }

    @Test
    void normalCompletionAndHungerRecoveryRearmNextMeal() {
        controller.tick(true, actions);
        actions.using = actions.eatingCarrot = false;
        actions.carrots--;
        actions.hunger = 20;
        assertTrue(controller.tick(true, actions));
        assertFalse(controller.tick(true, actions));
        assertEquals(0, actions.stops);
        actions.hunger = 14;
        assertTrue(controller.tick(true, actions));
        assertEquals(2, actions.starts);
        assertEquals(2, actions.reports);
    }

    @Test
    void manualItemUseFinishesBeforeAutomaticMealBegins() {
        actions.using = true;
        assertFalse(controller.tick(true, actions));
        assertEquals(1, actions.reports);
        assertEquals(0, actions.starts);
        assertEquals(0, actions.stops);
        actions.using = false;
        assertTrue(controller.tick(true, actions));
        assertEquals(1, actions.starts);
    }

    @Test
    void manualFoodRecoveryCancelsPendingMeal() {
        actions.using = true;
        controller.tick(true, actions);
        actions.hunger = 20;
        actions.using = false;
        assertFalse(controller.tick(true, actions));
        assertEquals(0, actions.starts);
    }

    @Test
    void missingCarrotsOnlyReportsOnceAndNeverUsesOtherItems() {
        actions.carrots = 0;
        for (int i = 0; i < 100; i++) assertFalse(controller.tick(true, actions));
        assertEquals(1, actions.reports);
        assertEquals(1, actions.missing);
        assertEquals(0, actions.starts);
        actions.carrots = 64;
        assertFalse(controller.tick(true, actions));
        controller.tick(false, actions);
        assertTrue(controller.tick(true, actions));
    }

    @Test
    void rejectedStartDoesNotRetryEveryTick() {
        actions.acceptStart = false;
        for (int i = 0; i < 100; i++) assertFalse(controller.tick(true, actions));
        assertEquals(1, actions.starts);
        assertEquals(1, actions.failures);
        assertFalse(controller.shouldKeepUsing(actions));
    }

    @Test
    void pausedWorldDoesNotStartOrTimeOutMeal() {
        actions.paused = true;
        assertFalse(controller.tick(true, actions));
        assertEquals(0, actions.reports);
        actions.paused = false;
        assertTrue(controller.tick(true, actions));
        actions.paused = true;
        for (int i = 0; i < 500; i++) assertTrue(controller.tick(true, actions));
        assertEquals(0, actions.stops);
        actions.paused = false;
        assertTrue(controller.tick(true, actions));
        assertTrue(controller.shouldKeepUsing(actions));
    }

    @Test
    void stalledMealTimesOutAndDoesNotRestart() {
        controller.tick(true, actions);
        for (int i = 0; i < 200; i++) assertTrue(controller.tick(true, actions));
        assertEquals(1, actions.stops);
        assertEquals(1, actions.failures);
        assertFalse(controller.shouldKeepUsing(actions));
        assertFalse(controller.tick(true, actions));
        assertEquals(1, actions.starts);
    }

    @Test
    void disableAndInvalidPlayerStopOwnedMealButNeverStopManualUse() {
        controller.tick(true, actions);
        assertFalse(controller.tick(false, actions));
        assertFalse(controller.shouldKeepUsing(actions));
        assertEquals(1, actions.stops);
        controller.tick(true, actions);
        actions.available = false;
        assertFalse(controller.shouldKeepUsing(actions));
        assertFalse(controller.tick(true, actions));
        assertEquals(2, actions.stops);
        actions.available = true;
        actions.using = actions.eatingCarrot = true;
        controller.reset(actions);
        assertEquals(2, actions.stops);
    }

    @Test
    void switchedItemEndsOwnershipWithoutReleasingUnrelatedItem() {
        controller.tick(true, actions);
        actions.eatingCarrot = false;
        actions.carrots = 0;
        assertFalse(controller.shouldKeepUsing(actions));
        assertTrue(controller.tick(true, actions));
        assertEquals(0, actions.stops);
        assertFalse(controller.tick(true, actions));
        assertEquals(1, actions.starts);
    }

    private static final class FakeActions implements AutoEatController.Actions {
        boolean available = true, paused, using, eatingCarrot, acceptStart = true;
        int hunger = 14, carrots = 64;
        int starts, stops, reports, missing, failures;
        public boolean canEat() { return available; }
        public boolean isPaused() { return paused; }
        public int foodLevel() { return hunger; }
        public int offhandCarrotCount() { return carrots; }
        public boolean isUsingItem() { return using; }
        public boolean isEatingOffhandCarrot() { return using && eatingCarrot; }
        public boolean startEating() {
            starts++;
            using = eatingCarrot = acceptStart;
            return acceptStart;
        }
        public void stopEating() { stops++; using = eatingCarrot = false; }
        public void reportHunger() { reports++; }
        public void reportMissingFood() { missing++; }
        public void reportFailedAttempt() { failures++; }
    }
}
