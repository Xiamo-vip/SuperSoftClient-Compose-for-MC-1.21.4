package com.xiamo.module.modules.combat

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.xiamo.module.Category
import com.xiamo.module.Module
import com.xiamo.setting.AbstractSetting
import com.xiamo.utils.rotation.RotationManager
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW
import java.util.concurrent.CopyOnWriteArrayList

/**
 * KillAura
 */
object KillAura : Module("KillAura", "自动攻击附近的实体", Category.Combat) {
    var isAttacking = mutableStateOf(false)
    var targetObject: LivingEntity? = null

    private val rangeSetting = numberSetting(
        "Range", "攻击范围",
        3.5, 2.0, 6.0, 0.1
    )

    private val rotationSpeedSetting = numberSetting(
        "RotSpeed", "转头速度",
        60.0, 10.0, 180.0, 5.0
    )

    private val smoothnessSetting = numberSetting(
        "Smooth", "平滑度",
        0.5, 0.1, 1.0, 0.05
    )

    private val silentRotationSetting = booleanSetting(
        "Silent", "静默转头",
        true
    )

    private val predictSetting = booleanSetting(
        "Predict", "目标预测（提高命中率）",
        true
    )

    private val predictFactorSetting = numberSetting(
        "PredictFactor", "预测强度",
        2.0, 0.5, 5.0, 0.5
    ).apply {
        dependency = { predictSetting.value }
    }

    private val autoAttackSetting = booleanSetting(
        "AutoAttack", "自动攻击",
        true
    )
    public val targetBarSetting = booleanSetting(
        "TargetBar", "目标信息",
        true
    )

    private val hitChanceSetting = numberSetting(
        "HitChance", "命中率%",
        100.0, 50.0, 100.0, 5.0
    )

    private val randomizeSetting = booleanSetting(
        "Randomize", "随机化",
        true
    )


    private var currentTarget: LivingEntity? = null
    private var lastTargetPos: Vec3d? = null

    init {
        this.key = GLFW.GLFW_KEY_G
    }

    override fun onSettingChanged(setting: AbstractSetting<*>) {
        super.onSettingChanged(setting)
        updateRotationConfig()
    }

    private fun updateRotationConfig() {
        RotationManager.renderRotation = !silentRotationSetting.value
        RotationManager.moveFixEnabled = false
        RotationManager.rotationSpeed = rotationSpeedSetting.floatValue
        RotationManager.smoothness = smoothnessSetting.floatValue
        RotationManager.randomizationEnabled = randomizeSetting.value
    }

    override fun enable() {
        updateRotationConfig()
        currentTarget = null
        lastTargetPos = null
        super.enable()
    }

    override fun onTick() {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val world = mc.world ?: return

        val attackRange = rangeSetting.value


        val targets = CopyOnWriteArrayList<LivingEntity>()
        world.entities.forEach { entity ->
            if (entity is LivingEntity
                && entity.isAlive
                && entity.isAttackable
                && entity != player
                && player.squaredDistanceTo(entity) < attackRange * attackRange
            ) {
                targets.add(entity)
            }
        }

        if (targets.isEmpty()) {
            isAttacking.value = false
            currentTarget = null
            lastTargetPos = null
            RotationManager.clearTarget()
            return
        }


        val target = targets.minByOrNull { it.distanceTo(player) } ?: return
        isAttacking.value = true
        targetObject = target


        val targetPos = getTargetPosition(target)


        if (currentTarget != target) {
            lastTargetPos = null
        }
        currentTarget = target
        lastTargetPos = target.pos


        val rotation = RotationManager.calculateRotation(
            player.eyePos.x, player.eyePos.y, player.eyePos.z,
            targetPos.x, targetPos.y, targetPos.z
        )

        if (silentRotationSetting.value) {

            RotationManager.setTargetRotation(rotation)


            if (autoAttackSetting.value && canAttack(target)) {
                attackTarget(target)
            }
        } else {
            rotatePlayer(rotation.yaw, rotation.pitch)


            if (autoAttackSetting.value && canAttackNonSilent(target)) {
                attackTarget(target)
            }
        }




        super.onTick()
    }


    private fun getTargetPosition(target: LivingEntity): Vec3d {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return target.eyePos


        var targetY = target.y + target.height * 0.7

        if (!predictSetting.value || lastTargetPos == null) {
            return Vec3d(target.x, targetY, target.z)
        }


        val velocity = Vec3d(
            target.x - target.prevX,
            target.y - target.prevY,
            target.z - target.prevZ
        )


        val distance = player.distanceTo(target)


        val factor = predictFactorSetting.value * (distance / 3.0).coerceIn(0.5, 2.0)


        val predictedX = target.x + velocity.x * factor
        val predictedY = targetY + velocity.y * factor
        val predictedZ = target.z + velocity.z * factor

        return Vec3d(predictedX, predictedY, predictedZ)
    }


    private fun rotatePlayer(targetYaw: Float, targetPitch: Float) {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return

        val speed = rotationSpeedSetting.floatValue
        val smooth = smoothnessSetting.floatValue


        val yawDiff = MathHelper.wrapDegrees(targetYaw - player.yaw)
        val pitchDiff = targetPitch - player.pitch


        val yawStep = calculateStep(yawDiff, speed, smooth)
        val pitchStep = calculateStep(pitchDiff, speed * 0.7f, smooth)


        val newYaw = player.yaw + yawStep
        val newPitch = (player.pitch + pitchStep).coerceIn(-90f, 90f)


        player.yaw = newYaw
        player.pitch = newPitch
    }


    private fun calculateStep(diff: Float, maxSpeed: Float, smooth: Float): Float {
        val absDiff = kotlin.math.abs(diff)


        if (absDiff < 0.5f) return diff


        val exponential = diff * (1f - smooth) * 0.8f
        val linear = kotlin.math.sign(diff) * kotlin.math.min(absDiff, maxSpeed) * smooth * 0.5f

        val step = exponential + linear


        return step.coerceIn(-maxSpeed, maxSpeed)
    }


    private fun canAttack(target: LivingEntity): Boolean {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return false


        if (player.getAttackCooldownProgress(0.5f) < 1.0f) return false


        if (player.squaredDistanceTo(target) > rangeSetting.value * rangeSetting.value) return false


        if (hitChanceSetting.value < 100.0 && Math.random() * 100 > hitChanceSetting.value) return false


        val targetRot = RotationManager.targetRotation ?: return false
        val serverYaw = RotationManager.serverYaw
        val serverPitch = RotationManager.serverPitch

        val yawDiff = kotlin.math.abs(MathHelper.wrapDegrees(targetRot.yaw - serverYaw))
        val pitchDiff = kotlin.math.abs(targetRot.pitch - serverPitch)


        return yawDiff < 25f && pitchDiff < 25f
    }


    private fun canAttackNonSilent(target: LivingEntity): Boolean {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return false


        if (player.getAttackCooldownProgress(0.5f) < 1.0f) return false


        if (player.squaredDistanceTo(target) > rangeSetting.value * rangeSetting.value) return false


        if (hitChanceSetting.value < 100.0 && Math.random() * 100 > hitChanceSetting.value) return false


        val targetPos = getTargetPosition(target)
        val rotation = RotationManager.calculateRotation(
            player.eyePos.x, player.eyePos.y, player.eyePos.z,
            targetPos.x, targetPos.y, targetPos.z
        )

        val yawDiff = kotlin.math.abs(MathHelper.wrapDegrees(rotation.yaw - player.yaw))
        val pitchDiff = kotlin.math.abs(rotation.pitch - player.pitch)


        return yawDiff < 30f && pitchDiff < 30f
    }


    private fun attackTarget(target: LivingEntity) {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val interactionManager = mc.interactionManager ?: return

        interactionManager.attackEntity(player, target)
        player.swingHand(net.minecraft.util.Hand.MAIN_HAND)
    }

    override fun disable() {
        currentTarget = null
        lastTargetPos = null
        RotationManager.clearTarget()
        super.disable()
    }
}
