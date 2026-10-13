package name.modid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HungerAlertTest {
    private final HungerAlert alert = new HungerAlert();

    @Test
    void reachingThresholdNotifiesOnceWithoutSpammingOnFurtherDrops() {
        assertFalse(alert.update(20));
        assertFalse(alert.update(15));
        assertTrue(alert.update(14));
        for (int i = 0; i < 100; i++) assertFalse(alert.update(14));
        assertFalse(alert.update(11));
        assertFalse(alert.update(0));
    }

    @Test
    void serverUpdateSkippingThresholdStillNotifies() {
        assertFalse(alert.update(16));
        assertTrue(alert.update(10));
        assertFalse(alert.update(9));
    }

    @Test
    void onlyRecoveryAboveThresholdRearmsNotification() {
        assertTrue(alert.update(8));
        assertFalse(alert.update(14));
        assertFalse(alert.update(15));
        assertTrue(alert.update(14));
    }

    @Test
    void joiningHungryAndResettingSessionAllowFreshNotifications() {
        assertTrue(alert.update(14));
        assertFalse(alert.update(14));
        alert.reset();
        assertTrue(alert.update(10));
        assertFalse(alert.update(10));
    }
}
