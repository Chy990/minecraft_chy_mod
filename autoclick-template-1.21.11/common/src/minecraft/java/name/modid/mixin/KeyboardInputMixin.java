package name.modid.mixin;

import name.modid.ChyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void chy$applySneaking(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (ChyClient.isAutoSneaking() && player != null && player.isAlive() && !player.isSpectator()) {
            // Augment fresh vanilla input without changing physical key or toggle-sneak state.
            Input input = keyPresses;
            keyPresses = new Input(input.forward(), input.backward(), input.left(), input.right(),
                input.jump(), true, input.sprint());
        }
    }
}
