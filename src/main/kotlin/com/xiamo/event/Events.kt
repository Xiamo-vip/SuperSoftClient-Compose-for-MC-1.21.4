package com.xiamo.event

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Overlay
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.awt.event.ActionEvent


class RenderEvent(val drawContext: DrawContext) : Event(){}


class MouseClickedEvent(val mouseX : Int,val mouseY : Int): Event(){}
class MouseReleasedEvent(val mouseX : Int,val mouseY : Int): Event(){}



class KeyBoardEvent(val key : Int,val action : Int): Event(){}
class TickEvent(): Event(){}




