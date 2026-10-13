package name.modid;

/** One notification per low-hunger episode, including joining an already hungry player. */
public final class HungerAlert {
    public static final int THRESHOLD = 14;
    private boolean alerted;

    public boolean update(int foodLevel) {
        if (foodLevel > THRESHOLD) {
            reset();
            return false;
        }
        if (alerted) return false;
        alerted = true;
        return true;
    }

    public void reset() { alerted = false; }
}
