package com.xiamo.mixin;


import com.xiamo.event.KeyBoardEvent;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKey {



    @Inject(method = "onKey",at=@At("HEAD"))
    private void hookKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci){
        new KeyBoardEvent(key,action,scancode).broadcast();
    }
}
