package com.xiamo.module

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.scene.ComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mojang.blaze3d.systems.RenderSystem
import com.xiamo.utils.AWTUtils
import com.xiamo.utils.GlStateUtil
import com.xiamo.utils.config.ConfigManager
import com.xiamo.utils.glfwToAwtKeyCode
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL33C
import java.awt.event.KeyEvent

@OptIn(InternalComposeUiApi::class)
open class ComposeModule(name : String, description : String) : Module(name,description, Category.Render) {

    var skiaContext : DirectContext? = null
    var renderTarget : BackendRenderTarget? = null

    private var currentScale: Float = 1f
    private var lastScaleFactor: Float = -1f

    var surface: Surface? = null
    @OptIn(InternalComposeUiApi::class)
    var composeScene : ComposeScene? = null


    private val componentPositions = mutableMapOf<String, Pair<Float, Float>>()




    private fun closeSkiaResources() {
        skiaContext?.close()
        renderTarget?.close()
        surface?.close()
        skiaContext = null
        renderTarget = null
        surface = null
    }

    private fun initCompose(width: Int, height: Int){
        if (composeScene == null) {
            composeScene = CanvasLayersComposeScene(
                density = Density(MinecraftClient.getInstance().window.scaleFactor.toFloat()
                ), invalidate = {}).apply {
                setContent { renderCompose() }
            }
        } else {
            composeScene?.density = Density(MinecraftClient.getInstance().window.scaleFactor.toFloat())
        }
        composeScene?.size = IntSize(width, height)
    }

    private fun buildCompose(){
        val frameWidth = MinecraftClient.getInstance().window.framebufferWidth
        val frameHeight = MinecraftClient.getInstance().window.framebufferHeight
        val mc = MinecraftClient.getInstance()
        if (skiaContext != null && surface != null &&
            surface!!.width == frameWidth && surface!!.height == frameHeight) {
            return
        }

        closeSkiaResources()

        skiaContext = DirectContext.makeGL()
        renderTarget = BackendRenderTarget.makeGL(frameWidth,frameHeight,0,8,mc.framebuffer.fbo,
            FramebufferFormat.GR_GL_RGBA8)


        surface = Surface.makeFromBackendRenderTarget(
            skiaContext!!, renderTarget!!, SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.BGRA_8888, ColorSpace.sRGB)

    }

    @OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
    override fun onRender(drawContext: DrawContext) {
        val mc = MinecraftClient.getInstance()

        if (composeScene == null) initCompose(mc.window.width,mc.window.height)

        if (lastScaleFactor != mc.window.scaleFactor.toFloat()){
            closeSkiaResources()
            initCompose(mc.window.width,mc.window.height)
            lastScaleFactor = mc.window.scaleFactor.toFloat()
        }

        if (composeScene?.size?.width != mc.window.width || composeScene?.size?.height != mc.window.height) {
            closeSkiaResources()
            initCompose(mc.window.width,mc.window.height)
        }
        currentScale = mc.window.scaleFactor.toFloat()
        buildCompose()
        GlStateUtil.save()
        glStorePixel()
        skiaContext?.resetAll()
        RenderSystem.enableBlend()
        surface?.let { composeScene?.render(it.canvas.asComposeCanvas(), System.nanoTime()) }
        surface?.flush()
        GlStateUtil.restore()
        RenderSystem.disableBlend()


    }



    private fun toComposeOffset(mouseX: Double, mouseY: Double): Offset {
        return Offset(
            (mouseX * currentScale).toFloat(),
            (mouseY * currentScale).toFloat()
        )
    }
    private fun glStorePixel(){
        GL33C.glBindBuffer(GL33C.GL_PIXEL_UNPACK_BUFFER, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_SWAP_BYTES, GL33C.GL_FALSE)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_LSB_FIRST, GL33C.GL_FALSE)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_ROW_LENGTH, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_SKIP_ROWS, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_SKIP_PIXELS, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_ALIGNMENT, 4)
    }

    @Composable
    open fun renderCompose(){
    }

    override fun onKey(keyCode: Int, keyState: Int,scanCode: Int) {
        if (keyState == GLFW.GLFW_PRESS){
            val event = AWTUtils.KeyEvent(
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                AWTUtils.getAwtMods(MinecraftClient.getInstance().window.handle),
                glfwToAwtKeyCode(keyCode),
                KeyEvent.CHAR_UNDEFINED,
                KeyEvent.KEY_LOCATION_STANDARD
            )
            composeScene?.sendKeyEvent(event)
        }

        if (keyState == GLFW.GLFW_RELEASE){
            val awtKey = glfwToAwtKeyCode(keyCode)
            val time = System.nanoTime() / 1_000_000
            composeScene?.sendKeyEvent(
                AWTUtils.KeyEvent(
                    KeyEvent.KEY_RELEASED,
                    time,
                    AWTUtils.getAwtMods(MinecraftClient.getInstance().window.handle),
                    awtKey,
                    0.toChar(),
                    KeyEvent.KEY_LOCATION_STANDARD
                )
            )
        }


        super.onKey(keyCode, keyState,scanCode)
    }

    fun saveComponentPositions(positions: Map<String, Pair<Float, Float>>) {
        componentPositions.clear()
        componentPositions.putAll(positions)
        positions.forEach { (componentId, pos) ->
            val xKey = "Component_${componentId}_X"
            val yKey = "Component_${componentId}_Y"


            var xSetting = settings.find { it.name == xKey } as? com.xiamo.setting.NumberSetting
            var ySetting = settings.find { it.name == yKey } as? com.xiamo.setting.NumberSetting

            if (xSetting == null) {
                xSetting = numberSetting(xKey, "Component $componentId X", pos.first.toDouble(), -10000.0, 10000.0, 1.0)
                xSetting.visible = false
            } else {
                xSetting.value = pos.first.toDouble()
            }

            if (ySetting == null) {
                ySetting = numberSetting(yKey, "Component $componentId Y", pos.second.toDouble(), -10000.0, 10000.0, 1.0)
                ySetting.visible = false
            } else {
                ySetting.value = pos.second.toDouble()
            }
        }
        com.xiamo.utils.config.ConfigManager.saveModule(this)
    }

    fun loadComponentPosition(componentId: String, defaultX: Float, defaultY: Float): Pair<Float, Float> {
        val componentX = numberSetting("Component_${componentId}_X","组件位置",defaultX.toDouble(),0.0, 2000.0,.0)
        val componentY = numberSetting("Component_${componentId}_Y","组件位置",defaultY.toDouble(),0.0,2000.0)
        ConfigManager.load()
        componentPositions[componentId]?.let { return it }
        val x = componentX.value.toFloat()
        val y = componentY.value.toFloat()



        val position = Pair(x, y)
        componentPositions[componentId] = position
        return position
    }




}