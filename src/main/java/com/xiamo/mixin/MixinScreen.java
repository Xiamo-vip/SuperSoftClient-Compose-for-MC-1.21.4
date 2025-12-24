package com.xiamo.mixin;


import com.xiamo.event.NavigateEvent;
import com.xiamo.module.modules.player.ChestStealer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.concurrent.CopyOnWriteArrayList;


@Mixin(MinecraftClient.class)
public class MixinScreen {
    @Unique
    private static final CopyOnWriteArrayList<Class<? extends Screen>> hookScreenList =
            new CopyOnWriteArrayList<>();

    static {
        hookScreenList.add(TitleScreen.class);
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen != null && hookScreenList.contains(screen.getClass())) {
            ci.cancel();
            new NavigateEvent(screen).broadcast();
        }
        if (ChestStealer.INSTANCE.isChestScreen(screen) && screen != null) {
            ChestStealer.INSTANCE.setHide(true);
        }



    }

    @Redirect(method = "setScreen",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;unlockCursor()V"))
    private void unlockCursor(Mouse instance){
        if (MinecraftClient.getInstance().world != null) {
            if (!(ChestStealer.INSTANCE.getHide() && ChestStealer.INSTANCE.getEnabled() && ChestStealer.INSTANCE.isSilence().getValue())){
                instance.unlockCursor();
            }



        }
    }
}


    


