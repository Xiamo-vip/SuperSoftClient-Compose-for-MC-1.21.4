package com.xiamo.mixin;

import com.xiamo.event.RenderEvent;
import com.xiamo.module.modules.render.EffectHud;
import com.xiamo.module.modules.render.PlayerList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinGameHud {
	@Inject(method = "renderMainHud",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHeldItemTooltip(Lnet/minecraft/client/gui/DrawContext;)V"))
	private void hookGameHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci){
		new RenderEvent(context).broadcast();
	}

    @Inject(method = "renderStatusEffectOverlay",at = @At("HEAD"), cancellable = true)
    private void hookEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci){
        if (EffectHud.INSTANCE.getEnabled()) {
            EffectHud.INSTANCE.getTickCounter().setValue(tickCounter.getTickDelta(true));
            ci.cancel();
        }
    }


    @Inject(method = "renderPlayerList",at = @At("HEAD"), cancellable = true)
    private void hookPlayerList(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci){
        if (PlayerList.INSTANCE.getEnabled()){
            ci.cancel();
        }

    }




}