package com.xiamo.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.scene.ComposeScene
import androidx.compose.ui.scene.ComposeScenePointer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.mojang.blaze3d.systems.RenderSystem
import com.xiamo.module.ModuleManager
import com.xiamo.utils.AWTUtils
import com.xiamo.utils.GlStateUtil
import com.xiamo.utils.glfwToAwtKeyCode
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
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
import java.awt.event.MouseEvent

@OptIn(InternalComposeUiApi::class)
open class ComposeScreen(val text: Text) : Screen(text) {


    var isVisible by mutableStateOf(false)
    var allowExit  = false
    var skiaContext : DirectContext? = null
    var surface : Surface? = null
    var renderTarget : BackendRenderTarget? = null
    var mc = MinecraftClient.getInstance()

    var currentScale= mc.window.scaleFactor

    var lastScaleFactor = currentScale

    @OptIn(InternalComposeUiApi::class)
    var composeScene : ComposeScene? = null

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
        val frameWidth = mc.window.framebufferWidth
        val frameHeight = mc.window.framebufferHeight

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

    override fun removed() {
        super.removed()
    }


    @OptIn(InternalComposeUiApi::class)
    override fun render(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val mc = MinecraftClient.getInstance()

        if (composeScene == null) initCompose(mc.window.width,mc.window.height)

        if (lastScaleFactor != mc.window.scaleFactor){
            closeSkiaResources()
            initCompose(mc.window.width,mc.window.height)
            lastScaleFactor = mc.window.scaleFactor
        }

        if (composeScene?.size?.width != mc.window.width || composeScene?.size?.height != mc.window.height) {
            closeSkiaResources()
            initCompose(mc.window.width,mc.window.height)
        }
        currentScale = mc.window.scaleFactor

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


    override fun shouldCloseOnEsc(): Boolean {
        return false
    }
    private fun toComposeOffset(mouseX: Double, mouseY: Double): Offset {
        currentScale = MinecraftClient.getInstance().window.scaleFactor
        return Offset(
            (mouseX * currentScale).toFloat(),
            (mouseY * currentScale).toFloat()
        )
    }

    override fun resize(client: MinecraftClient?, width: Int, height: Int) {
        closeSkiaResources()
        if (client != null) {
            initCompose(client.window.width, client.window.height)
        }
        super.resize(client, width, height)
    }

    @OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        val pointer = ComposeScenePointer(
            id = PointerId(0),
            position = toComposeOffset(mouseX, mouseY),
            pressed = false,
            type = PointerType.Mouse
        )
        val event = AWTUtils.MouseEvent(
            (mouseX * currentScale).toInt(),
            (mouseY* currentScale).toInt(),
            AWTUtils.getAwtMods(MinecraftClient.getInstance().window.handle),
            0,
            MouseEvent.MOUSE_MOVED
        )
        composeScene?.sendPointerEvent(
            PointerEventType.Move,
            position = toComposeOffset(mouseX, mouseY),
            type = PointerType.Mouse,
            button = PointerButton(0),
            nativeEvent = event
        )
        super.mouseMoved(mouseX, mouseY)
    }

    @OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        currentScale = MinecraftClient.getInstance().window.scaleFactor
        val event = AWTUtils.MouseEvent(
            (mouseX * currentScale).toInt(),
            (mouseY * currentScale).toInt(),
            AWTUtils.getAwtMods(MinecraftClient.getInstance().window.handle),
            button,
            MouseEvent.MOUSE_PRESSED
        )
        val pointer = ComposeScenePointer(
            id = PointerId(0),
            position = toComposeOffset(mouseX, mouseY),
            pressed = true,
            type = PointerType.Mouse
        )

        composeScene?.sendPointerEvent(PointerEventType.Press,toComposeOffset(mouseX, mouseY), nativeEvent = event)
        return super.mouseClicked(mouseX, mouseY, button)
    }

    @OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        val event = AWTUtils.MouseEvent(
            (mouseX * currentScale).toInt(),
            (mouseY * currentScale).toInt(),
            AWTUtils.getAwtMods(MinecraftClient.getInstance().window.handle),
            button,
            MouseEvent.MOUSE_DRAGGED
        )

        val pointer = ComposeScenePointer(
            id = PointerId(0),
            position = toComposeOffset(mouseX, mouseY),
            pressed = true,
            type = PointerType.Mouse
        )

        composeScene?.sendPointerEvent(
            eventType = PointerEventType.Move,
            pointers = listOf(pointer),
            nativeEvent = event
        )



        return true
    }

    @OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val event = AWTUtils.MouseEvent(
            (mouseX * currentScale).toInt(),
            (mouseY * currentScale).toInt(),
            AWTUtils.getAwtMods(MinecraftClient.getInstance().window.handle),
            button,
            MouseEvent.MOUSE_RELEASED
        )

        val pointer = ComposeScenePointer(
            id = PointerId(0),
            position = toComposeOffset(mouseX, mouseY),
            pressed = false,
            type = PointerType.Mouse
        )

        composeScene?.sendPointerEvent(
            eventType = PointerEventType.Release,
            position = toComposeOffset(mouseX, mouseY),
            nativeEvent = event
        )

        return super.mouseReleased(mouseX, mouseY, button)
    }

    @OptIn(InternalComposeUiApi::class)
    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        val time = System.nanoTime() / 1_000_000
        composeScene?.sendKeyEvent(
            AWTUtils.KeyEvent(
                KeyEvent.KEY_TYPED,
                time,
                AWTUtils.getAwtMods(MinecraftClient.getInstance().window.handle),
                Key.Unknown.keyCode.toInt(),
                chr,
                KeyEvent.KEY_LOCATION_UNKNOWN
            )
        )
        return true
    }


    @OptIn(InternalComposeUiApi::class)
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE){
            isVisible = false
            ModuleManager.modules.filter { it.isComposeScreen }.forEach{
                if (it.name == text.string) {
                    it.disable()
                }
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) allowExit = true
        val event = AWTUtils.KeyEvent(
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            AWTUtils.getAwtMods(mc.window.handle),
            glfwToAwtKeyCode(keyCode),
            KeyEvent.CHAR_UNDEFINED,
            KeyEvent.KEY_LOCATION_STANDARD
        )

        composeScene?.sendKeyEvent(event)
        return true
    }

    @OptIn(InternalComposeUiApi::class)
    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val awtKey = glfwToAwtKeyCode(keyCode)
        val time = System.nanoTime() / 1_000_000
        composeScene?.sendKeyEvent(
            AWTUtils.KeyEvent(
                KeyEvent.KEY_RELEASED,
                time,
                AWTUtils.getAwtMods(mc.window.handle),
                awtKey,
                0.toChar(),
                KeyEvent.KEY_LOCATION_STANDARD
            )
        )
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        val event = AWTUtils.MouseWheelEvent(
            (mouseX * currentScale).toInt(),
            (mouseY * currentScale).toInt(),
            mouseY,
            AWTUtils.getAwtMods(MinecraftClient.getInstance().window.handle),
            MouseEvent.MOUSE_WHEEL
        )
        composeScene?.sendPointerEvent(
            position =toComposeOffset(mouseX, mouseY),
            eventType = PointerEventType.Scroll,
            scrollDelta = toComposeOffset(horizontalAmount, -verticalAmount),
            nativeEvent = event
        )
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    @OptIn(InternalComposeUiApi::class)
    override fun close() {
        allowExit = true
        closeSkiaResources()
        composeScene?.close()
        super.close()
    }

    @Composable
    open fun renderCompose(){}
    private fun glStorePixel(){
        GL33C.glBindBuffer(GL33C.GL_PIXEL_UNPACK_BUFFER, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_SWAP_BYTES, GL33C.GL_FALSE)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_LSB_FIRST, GL33C.GL_FALSE)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_ROW_LENGTH, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_SKIP_ROWS, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_SKIP_PIXELS, 0)
        GL33C.glPixelStorei(GL33C.GL_UNPACK_ALIGNMENT, 4)
    }
}