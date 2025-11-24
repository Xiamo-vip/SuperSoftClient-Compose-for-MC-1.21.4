package com.xiamo.gui

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.scene.ComposeScene
import androidx.compose.ui.scene.ComposeScenePointer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.mojang.blaze3d.systems.RenderSystem
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
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL33C
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

@OptIn(InternalComposeUiApi::class)
open class ComposeScreen(text: Text) : Screen(text) {



    var skiaContext : DirectContext? = null
    var surface : Surface? = null
    var renderTarget : BackendRenderTarget? = null
    var mc = MinecraftClient.getInstance()

    var currentScale: Double = mc.window.scaleFactor

    var lastScaleFactor = currentScale

    @OptIn(InternalComposeUiApi::class)
    var composeScene : ComposeScene? = null

    private fun initCompose(width: Int,height: Int){
        composeScene = CanvasLayersComposeScene(density = Density(1f), invalidate = {}).apply {
            setContent { renderCompose() }
        }
    }

    @OptIn(InternalComposeUiApi::class)
    private fun buildCompose(){
        val frameWidth = mc.window.framebufferWidth
        val frameHeight = mc.window.framebufferHeight
        renderTarget = BackendRenderTarget.makeGL(frameWidth,frameHeight,0,8,mc.framebuffer.fbo,
            FramebufferFormat.GR_GL_RGBA8)


        surface = Surface.makeFromBackendRenderTarget(
            skiaContext!!, renderTarget!!, SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.BGRA_8888, ColorSpace.sRGB)

    }


    @OptIn(InternalComposeUiApi::class)
    override fun render(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val client = MinecraftClient.getInstance()
        currentScale = client.window.scaleFactor
        val width = client.window.framebufferWidth
        val height = client.window.framebufferHeight
        if (composeScene == null) initCompose(width,height)
        if (lastScaleFactor != currentScale) {
            lastScaleFactor = currentScale
            composeScene?.density = Density(currentScale.toFloat())
        }
        GlStateUtil.save()
        glStorePixel()
        skiaContext?.resetAll()
        try {
            if (skiaContext == null || skiaContext!!.isClosed) {
                skiaContext = DirectContext.makeGL()
            }
            if (surface == null || surface?.width != width || surface?.height != height) {
                surface?.close()
                val renderTarget = BackendRenderTarget.makeGL(
                    width, height, 0, 8,
                    client.framebuffer.fbo, FramebufferFormat.GR_GL_RGBA8
                )
                surface = Surface.makeFromBackendRenderTarget(
                    skiaContext!!, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.sRGB
                )
            }
            composeScene?.size = IntSize(width, height)
            surface?.canvas?.let { canvas ->
                composeScene?.render(canvas.asComposeCanvas(), System.nanoTime())
            }
            skiaContext?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            GlStateUtil.restore()

        }
    }

    override fun shouldPause(): Boolean {
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
        composeScene?.density = Density(currentScale.toFloat())
        buildCompose()
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
        composeScene?.sendPointerEvent(
            PointerEventType.Move,
            listOf(pointer)
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

        composeScene?.sendPointerEvent(
            eventType = PointerEventType.Press,
            pointers = listOf(pointer),
            nativeEvent = event
        )

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
            pointers = listOf(pointer),
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
                AWTUtils.getAwtMods(mc.window.handle),
                Key.Unknown.keyCode.toInt(),
                chr,
                KeyEvent.KEY_LOCATION_UNKNOWN
            )
        )
        return super.charTyped(chr, modifiers)
    }


    @OptIn(InternalComposeUiApi::class)
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val awtKey = glfwToAwtKeyCode(keyCode)
        val time = System.nanoTime() / 1_000_000
        composeScene?.sendKeyEvent(
            AWTUtils.KeyEvent(
                KeyEvent.KEY_PRESSED,
                time,
                AWTUtils.getAwtMods(mc.window.handle),
                awtKey,
                0.toChar(),
                KeyEvent.KEY_LOCATION_STANDARD
            )
        )
        return super.keyPressed(keyCode, scanCode, modifiers)
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

    @OptIn(InternalComposeUiApi::class)
    override fun close() {
        skiaContext?.close()
        renderTarget?.close()
        surface?.close()
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