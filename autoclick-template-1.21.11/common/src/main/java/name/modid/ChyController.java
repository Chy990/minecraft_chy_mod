package name.modid;

/** Shared scheduling policy. Platform adapters supply only the actual game actions. */
public final class ChyController {
    private final ChyState state;

    public ChyController(ChyState state) { this.state = state; }

    public interface Actions {
        boolean hasWorld();
        boolean canAct();
        boolean isUsingItem();
        void refreshTarget();
        void leftClick();
        void rightClick();
    }

    public void tick(Actions actions) {
        if (!actions.hasWorld()) {
            state.reset();
            return;
        }
        if (!actions.canAct()) return;
        boolean leftDue = state.leftClick.tick();
        boolean rightDue = state.rightClick.tick();
        if (leftDue || rightDue) actions.refreshTarget();
        if (leftDue) actions.leftClick();
        if (rightDue && !actions.isUsingItem()) actions.rightClick();
    }
}
