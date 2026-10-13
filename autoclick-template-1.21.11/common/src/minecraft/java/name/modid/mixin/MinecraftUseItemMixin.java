package name.modid.mixin;

import name.modid.ChyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MinecraftUseItemMixin {
    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
    private void chy$keepOneMealActive(MultiPlayerGameMode gameMode, Player player) {
        // Simulate holding use only for our current meal, without latching the physical key.
        if (!ChyClient.shouldKeepEating()) gameMode.releaseUsingItem(player);
    }
}
