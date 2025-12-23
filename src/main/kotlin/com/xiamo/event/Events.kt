package com.xiamo.event

import androidx.compose.ui.graphics.Matrix
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Overlay
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.state.EntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.awt.event.ActionEvent
import java.util.concurrent.CopyOnWriteArrayList




class RenderEvent(val drawContext: DrawContext) : Event(){}


class MouseClickedEvent(val mouseX : Int,val mouseY : Int): Event(){}
class MouseReleasedEvent(val mouseX : Int,val mouseY : Int): Event(){}



class KeyBoardEvent(val key : Int,val action : Int,val scanCode : Int): Event(){}
class TickEvent(): Event(){}

class NavigateEvent(val screen : Screen): Event(){}

class PlayerMovementTickPacketSendPre(x: Double,y: Double,z: Double,isOnGround : Boolean): Event(){
    var isCancelled  = false
}

class EntityRenderEvent(val entity: Entity,val matrix: MatrixStack,val tickDelta : Float): Event(){}





