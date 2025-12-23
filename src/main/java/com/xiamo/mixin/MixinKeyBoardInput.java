package com.xiamo.mixin;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.xiamo.module.modules.movement.InventoryMove;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyBoardInput {


    @WrapOperation(method = "tick",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
    private boolean tick(KeyBinding instance, Operation<Boolean> original){
        return original.call(instance) ||
                InventoryMove.INSTANCE.getEnabled() && InventoryMove.INSTANCE.shouldHandle(instance.getDefaultKey().getCode())
                 && instance.isPressed();

    }
}
