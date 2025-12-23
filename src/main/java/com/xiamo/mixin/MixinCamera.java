package com.xiamo.mixin;

import com.xiamo.module.modules.render.SmoothCamera;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow
    private Vec3d pos;

    @Shadow
    private float yaw;

    @Shadow
    private float pitch;

    @Shadow
    protected abstract void setPos(Vec3d pos);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!SmoothCamera.INSTANCE.shouldApply()) {
            return;
        }

        if (!thirdPerson) {
            return;
        }

        Vec3d smoothPos = SmoothCamera.INSTANCE.updateSmoothPosition(
                pos.x, pos.y, pos.z, tickDelta
        );

        kotlin.Pair<Float, Float> smoothRot = SmoothCamera.INSTANCE.updateSmoothRotation(
                yaw, pitch, tickDelta
        );

        setPos(smoothPos);
        setRotation(smoothRot.getFirst(), smoothRot.getSecond());
    }
}
