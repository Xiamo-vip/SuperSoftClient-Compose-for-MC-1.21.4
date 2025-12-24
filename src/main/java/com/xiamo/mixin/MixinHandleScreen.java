package com.xiamo.mixin;


import com.xiamo.module.modules.player.ChestStealer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class MixinHandleScreen {

    @Inject(method = "renderBackground",at = @At("HEAD"), cancellable = true)
    private void hookRenderBackGround(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci){
        if (ChestStealer.INSTANCE.getHide() && ChestStealer.INSTANCE.isSilence().getValue() && ChestStealer.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "render",at = @At("HEAD"), cancellable = true)
    private void hookRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci){
        if (ChestStealer.INSTANCE.getHide() && ChestStealer.INSTANCE.isSilence().getValue() && ChestStealer.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "keyPressed",at = @At("HEAD"), cancellable = true)
    private void hookKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir){
        if (ChestStealer.INSTANCE.getHide() && ChestStealer.INSTANCE.isSilence().getValue() && ChestStealer.INSTANCE.getEnabled()) cir.cancel();

    }

    @Inject(method = "mouseClicked",at = @At("HEAD"), cancellable = true)
    private void hookmouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir){
        if (ChestStealer.INSTANCE.getHide() && ChestStealer.INSTANCE.isSilence().getValue() && ChestStealer.INSTANCE.getEnabled()) cir.cancel();
    }






}
