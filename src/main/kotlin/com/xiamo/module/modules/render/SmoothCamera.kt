package com.xiamo.module.modules.render

import com.xiamo.module.Category
import com.xiamo.module.Module
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.Perspective
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.sqrt

object SmoothCamera : Module("SmoothCamera", "平滑相机", Category.Render) {


    val smoothness = numberSetting("Smoothness", "平滑度", 8.0, 1.0, 20.0, 0.5)
    val rotationSmoothness = numberSetting("RotationSmooth", "旋转平滑度", 10.0, 1.0, 20.0, 0.5)
    val distance = numberSetting("Distance", "相机距离", 4.0, 1.0, 20.0, 0.5)
    val heightOffset = numberSetting("HeightOffset", "高度偏移", 0.0, -5.0, 5.0, 0.1)

    val enableInThirdPerson = booleanSetting("ThirdPerson", "第三人称后视", true)
    val enableInThirdPersonFront = booleanSetting("ThirdPersonFront", "第三人称前视", true)


    val motionCamera = booleanSetting("MotionCamera", "运动相机", true)
    val followDelay = numberSetting("FollowDelay", "跟随延迟", 0.3, 0.0, 1.0, 0.05)
    val sideOffset = numberSetting("SideOffset", "侧向偏移", 0.8, 0.0, 3.0, 0.1)
    val lookAhead = numberSetting("LookAhead", "前向预判", 1.5, 0.0, 5.0, 0.1)


    val elasticMode = booleanSetting("ElasticMode", "弹性模式", true)
    val springStiffness = numberSetting("SpringStiffness", "弹簧刚度", 50.0, 10.0, 200.0, 5.0)
    val springDamping = numberSetting("SpringDamping", "弹簧阻尼", 8.0, 1.0, 20.0, 0.5)


    val inertia = booleanSetting("Inertia", "惯性效果", true)
    val inertiaStrength = numberSetting("InertiaStrength", "惯性强度", 0.5, 0.0, 2.0, 0.1)


    private var cameraX = 0.0
    private var cameraY = 0.0
    private var cameraZ = 0.0
    private var cameraYaw = 0.0f
    private var cameraPitch = 0.0f


    private var velocityX = 0.0
    private var velocityY = 0.0
    private var velocityZ = 0.0
    private var velocityYaw = 0.0f
    private var velocityPitch = 0.0f


    private var lastPlayerX = 0.0
    private var lastPlayerY = 0.0
    private var lastPlayerZ = 0.0
    private var playerVelX = 0.0
    private var playerVelY = 0.0
    private var playerVelZ = 0.0


    private var currentSideOffset = 0.0
    private var currentForwardOffset = 0.0

    private var initialized = false
    private var lastTickDelta = 0f
    private var lastUpdateTime = System.nanoTime()

    fun shouldApply(): Boolean {
        val mc = MinecraftClient.getInstance()
        val perspective = mc.options.perspective
        return enabled && perspective != Perspective.FIRST_PERSON && when (perspective) {
            Perspective.THIRD_PERSON_BACK -> enableInThirdPerson.value
            Perspective.THIRD_PERSON_FRONT -> enableInThirdPersonFront.value
            else -> false
        }
    }

    fun getCameraDistance(): Double {
        return distance.value
    }

    fun getHeightOffset(): Double {
        return heightOffset.value
    }


    private fun smoothDamp(current: Double, target: Double, smoothTime: Double, deltaTime: Double): Double {
        if (smoothTime <= 0.0) return target
        val factor = 1.0 - (-deltaTime / smoothTime).let { if (it < -20) 0.0 else kotlin.math.exp(it) }
        return current + (target - current) * factor.coerceIn(0.0, 1.0)
    }


    private fun springDamper(
        current: Double,
        velocity: Double,
        target: Double,
        stiffness: Double,
        damping: Double,
        deltaTime: Double
    ): Pair<Double, Double> {
        val displacement = current - target
        val springForce = -stiffness * displacement
        val dampingForce = -damping * velocity
        val acceleration = springForce + dampingForce

        val newVelocity = velocity + acceleration * deltaTime
        val newPosition = current + newVelocity * deltaTime

        return Pair(newPosition, newVelocity)
    }

    fun updateSmoothPosition(targetX: Double, targetY: Double, targetZ: Double, tickDelta: Float): Vec3d {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return Vec3d(targetX, targetY, targetZ)


        val currentTime = System.nanoTime()
        val deltaTime = ((currentTime - lastUpdateTime) / 1_000_000_000.0).coerceIn(0.001, 0.1)
        lastUpdateTime = currentTime

        if (!initialized) {
            cameraX = targetX
            cameraY = targetY
            cameraZ = targetZ
            lastPlayerX = player.x
            lastPlayerY = player.y
            lastPlayerZ = player.z
            initialized = true
            return Vec3d(targetX, targetY, targetZ)
        }


        val rawVelX = player.x - lastPlayerX
        val rawVelY = player.y - lastPlayerY
        val rawVelZ = player.z - lastPlayerZ


        val velSmooth = 0.3
        playerVelX = smoothDamp(playerVelX, rawVelX / deltaTime, velSmooth, deltaTime)
        playerVelY = smoothDamp(playerVelY, rawVelY / deltaTime, velSmooth, deltaTime)
        playerVelZ = smoothDamp(playerVelZ, rawVelZ / deltaTime, velSmooth, deltaTime)

        lastPlayerX = player.x
        lastPlayerY = player.y
        lastPlayerZ = player.z

        var adjustedTargetX = targetX
        var adjustedTargetY = targetY
        var adjustedTargetZ = targetZ


        if (motionCamera.value) {
            val speed = sqrt(playerVelX * playerVelX + playerVelZ * playerVelZ)

            if (speed > 0.1) {
                val moveDirX = playerVelX / speed
                val moveDirZ = playerVelZ / speed

                val targetSideOffset = sideOffset.value * (speed / 10.0).coerceAtMost(1.0)
                currentSideOffset = smoothDamp(currentSideOffset, targetSideOffset, 0.5, deltaTime)


                val targetForwardOffset = lookAhead.value * (speed / 10.0).coerceAtMost(1.0)
                currentForwardOffset = smoothDamp(currentForwardOffset, targetForwardOffset, 0.3, deltaTime)


                adjustedTargetX += -moveDirZ * currentSideOffset
                adjustedTargetZ += moveDirX * currentSideOffset


                adjustedTargetX -= moveDirX * currentForwardOffset
                adjustedTargetZ -= moveDirZ * currentForwardOffset
            } else {
                currentSideOffset = smoothDamp(currentSideOffset, 0.0, 0.5, deltaTime)
                currentForwardOffset = smoothDamp(currentForwardOffset, 0.0, 0.5, deltaTime)
            }


            if (inertia.value) {
                val inertiaFactor = inertiaStrength.value * deltaTime
                adjustedTargetX += playerVelX * inertiaFactor
                adjustedTargetY += playerVelY * inertiaFactor
                adjustedTargetZ += playerVelZ * inertiaFactor
            }
        }


        if (followDelay.value > 0.0) {
            val delay = followDelay.value
            adjustedTargetX = cameraX + (adjustedTargetX - cameraX) * (1.0 - delay)
            adjustedTargetY = cameraY + (adjustedTargetY - cameraY) * (1.0 - delay)
            adjustedTargetZ = cameraZ + (adjustedTargetZ - cameraZ) * (1.0 - delay)
        }


        if (elasticMode.value) {
            val stiffness = springStiffness.value
            val damping = springDamping.value

            val (newX, newVelX) = springDamper(cameraX, velocityX, adjustedTargetX, stiffness, damping, deltaTime)
            val (newY, newVelY) = springDamper(cameraY, velocityY, adjustedTargetY, stiffness, damping, deltaTime)
            val (newZ, newVelZ) = springDamper(cameraZ, velocityZ, adjustedTargetZ, stiffness, damping, deltaTime)

            cameraX = newX
            cameraY = newY
            cameraZ = newZ
            velocityX = newVelX
            velocityY = newVelY
            velocityZ = newVelZ
        } else {
            val smoothTime = 1.0 / smoothness.value
            cameraX = smoothDamp(cameraX, adjustedTargetX, smoothTime, deltaTime)
            cameraY = smoothDamp(cameraY, adjustedTargetY, smoothTime, deltaTime)
            cameraZ = smoothDamp(cameraZ, adjustedTargetZ, smoothTime, deltaTime)
        }

        return Vec3d(cameraX, cameraY, cameraZ)
    }

    fun updateSmoothRotation(targetYaw: Float, targetPitch: Float, tickDelta: Float): Pair<Float, Float> {
        val currentTime = System.nanoTime()
        val deltaTime = ((currentTime - lastUpdateTime) / 1_000_000_000.0).coerceIn(0.001, 0.1)

        if (!initialized) {
            cameraYaw = targetYaw
            cameraPitch = targetPitch
            return Pair(targetYaw, targetPitch)
        }

        if (elasticMode.value) {
            val stiffness = springStiffness.value.toFloat()
            val damping = springDamping.value.toFloat()
            val dt = deltaTime.toFloat()


            val yawDiff = MathHelper.wrapDegrees(targetYaw - cameraYaw)
            val yawTarget = cameraYaw + yawDiff

            val (newYaw, newVelYaw) = springDamperFloat(cameraYaw, velocityYaw, yawTarget, stiffness, damping, dt)
            val (newPitch, newVelPitch) = springDamperFloat(cameraPitch, velocityPitch, targetPitch, stiffness, damping, dt)

            cameraYaw = MathHelper.wrapDegrees(newYaw)
            cameraPitch = newPitch.coerceIn(-90f, 90f)
            velocityYaw = newVelYaw
            velocityPitch = newVelPitch
        } else {
            val smoothTime = 1.0 / rotationSmoothness.value


            val yawDiff = MathHelper.wrapDegrees(targetYaw - cameraYaw)
            cameraYaw = MathHelper.wrapDegrees(cameraYaw + (yawDiff * smoothDamp(0.0, 1.0, smoothTime, deltaTime)).toFloat())
            cameraPitch = smoothDamp(cameraPitch.toDouble(), targetPitch.toDouble(), smoothTime, deltaTime).toFloat()
            cameraPitch = cameraPitch.coerceIn(-90f, 90f)
        }

        return Pair(cameraYaw, cameraPitch)
    }

    private fun springDamperFloat(
        current: Float,
        velocity: Float,
        target: Float,
        stiffness: Float,
        damping: Float,
        deltaTime: Float
    ): Pair<Float, Float> {
        val displacement = current - target
        val springForce = -stiffness * displacement
        val dampingForce = -damping * velocity
        val acceleration = springForce + dampingForce

        val newVelocity = velocity + acceleration * deltaTime
        val newPosition = current + newVelocity * deltaTime

        return Pair(newPosition, newVelocity)
    }

    override fun disable() {
        initialized = false
        velocityX = 0.0
        velocityY = 0.0
        velocityZ = 0.0
        velocityYaw = 0f
        velocityPitch = 0f
        playerVelX = 0.0
        playerVelY = 0.0
        playerVelZ = 0.0
        currentSideOffset = 0.0
        currentForwardOffset = 0.0
        super.disable()
    }
}
