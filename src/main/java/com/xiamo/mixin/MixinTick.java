package com.xiamo.mixin;

import com.xiamo.event.TickEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinTick {
    @Inject(at=@At("HEAD"),method = "tick")
    private void init(CallbackInfo callbackInfo){
        new TickEvent().broadcast();
    }

}