package name.modid;

/** Owns one offhand meal per low-hunger episode without taking over manual item use. */
public final class AutoEatController {
    // Allow for server latency, but never keep an interrupted/rejected meal active indefinitely.
    private static final int MAX_EATING_TICKS = 200;
    private final HungerAlert alert = new HungerAlert();
    private boolean pending;
    private boolean eating;
    private int startingCount;
    private int eatingTicks;

    public interface Actions {
        boolean canEat();
        boolean isPaused();
        int foodLevel();
        int offhandCarrotCount();
        boolean isUsingItem();
        boolean isEatingOffhandCarrot();
        boolean startEating();
        void stopEating();
        void reportHunger();
        void reportMissingFood();
        void reportFailedAttempt();
    }

    /** Returns true while automatic clicks must wait, including the completion tick. */
    public boolean tick(boolean enabled, Actions actions) {
        if (!enabled || !actions.canEat()) {
            reset(actions);
            return false;
        }
        if (actions.isPaused()) return eating;

        if (eating) {
            if (!shouldKeepUsing(actions)) {
                // Vanilla or the server ended this meal. Never restart it in this episode.
                resetMeal(actions);
            } else if (++eatingTicks >= MAX_EATING_TICKS) {
                resetMeal(actions);
                actions.reportFailedAttempt();
            }
            return true;
        }

        int hunger = actions.foodLevel();
        if (alert.update(hunger)) {
            actions.reportHunger();
            pending = true;
        }
        if (hunger > HungerAlert.THRESHOLD) pending = false;
        if (!pending || actions.isUsingItem()) return false;

        pending = false;
        startingCount = actions.offhandCarrotCount();
        if (startingCount == 0) {
            actions.reportMissingFood();
            return false;
        }
        eating = actions.startEating();
        eatingTicks = 0;
        if (!eating) actions.reportFailedAttempt();
        return eating;
    }

    public boolean shouldKeepUsing(Actions actions) {
        return eating && actions.canEat() && actions.isEatingOffhandCarrot()
            && actions.offhandCarrotCount() == startingCount;
    }

    public void reset(Actions actions) {
        resetMeal(actions);
        pending = false;
        alert.reset();
    }

    private void resetMeal(Actions actions) {
        if (eating && actions.isEatingOffhandCarrot()) actions.stopEating();
        eating = false;
        eatingTicks = 0;
    }
}
