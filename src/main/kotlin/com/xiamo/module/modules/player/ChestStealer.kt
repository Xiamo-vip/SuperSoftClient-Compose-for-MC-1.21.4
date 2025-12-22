package com.xiamo.module.modules.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import com.xiamo.module.Category
import com.xiamo.module.Module
import com.xiamo.notification.NotificationManager
import com.xiamo.notification.Notify
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Identifier
import org.jetbrains.skia.Image
import java.util.concurrent.CopyOnWriteArrayList

object ChestStealer : Module("ChestStealer","", Category.Player) {

    val isSilence  = booleanSetting("isSilence","isSilence",true)
    val isAutoClose = booleanSetting("isAutoClose","isAutoClose",true)
    var isStealing = false
    var hide = false

    override fun onTick() {
        if (!isStealing){
            isDynamicIsland.unRegister()
            isStealing = false
            hide = false
            if (isChestScreen()) {
                hide = true
                stealer()
            }

        }



        super.onTick()
    }



    fun stealer(){
        isStealing = true
        if (MinecraftClient.getInstance().currentScreen is HandledScreen<*>) {
            val screen = MinecraftClient.getInstance().currentScreen as HandledScreen<*>
            if (MinecraftClient.getInstance().currentScreen !is InventoryScreen && screen !is CreativeInventoryScreen) {
                if (screen.screenHandler.type == ScreenHandlerType.GENERIC_9X6 || screen.screenHandler.type == ScreenHandlerType.GENERIC_9X3){
                    isDynamicIsland.register(screen.screenHandler.syncId)
                    val slot = screen.screenHandler.slots
                    val player = MinecraftClient.getInstance().player
                    Thread {
                        slot.filter {
                        it.inventory != player?.inventory
                    }.filter { it.stack != ItemStack.EMPTY}
                            .forEach { s ->
                            Thread.sleep(100L)
                            clickSlot(screen.screenHandler.syncId,s.index,0,SlotActionType.QUICK_MOVE)
                            //screen.screenHandler.onSlotClick(s.index,0, SlotActionType.PICKUP,player)
                    }.apply {
                            if (isAutoClose.value){
                                MinecraftClient.getInstance().execute {
                                    player?.closeHandledScreen()
                                }
                            }
                                isStealing = false
                                isDynamicIsland.unRegister()
                        }

                    }.start()
                }


            }
        }

    }

    @JvmOverloads
    fun isChestScreen(screen : Screen? = MinecraftClient.getInstance().currentScreen): Boolean {
        if (screen is HandledScreen<*>) {
            if (screen !is InventoryScreen && screen !is CreativeInventoryScreen) {
                if (screen.screenHandler.type == ScreenHandlerType.GENERIC_9X6
                    || screen.screenHandler.type == ScreenHandlerType.GENERIC_9X3
                    ){
                    return true
                }


            }
        }
        return false
    }

    fun clickSlot(screenId : Int,slotId : Int,button : Int,action : SlotActionType){
        val screenHandler = MinecraftClient.getInstance().currentScreen as HandledScreen<*>
        if (screenId != screenHandler.screenHandler.syncId) return
        val defaultedList = screenHandler.screenHandler.slots
        val defaultedSize= defaultedList.size
        val list = CopyOnWriteArrayList<ItemStack>()
        defaultedList.forEach {
            list.add(it.stack)
        }
        val int2ObjectMap: Int2ObjectMap<ItemStack?> = Int2ObjectOpenHashMap<ItemStack?>()

        for (j in 0..<defaultedSize) {
            val itemStack = list[j]
            val itemStack2 = defaultedList[j].stack
            if (!ItemStack.areEqual(itemStack, itemStack2)) {
                int2ObjectMap.put(j, itemStack2)
            }
        }

        val networkHandler = MinecraftClient.getInstance().player!!.networkHandler
        isDynamicIsland.itemOutAnimate(screenHandler.screenHandler.getSlot(slotId))
        networkHandler.sendPacket(
            ClickSlotC2SPacket(
                screenId,screenHandler.screenHandler.revision,slotId,button,action, screenHandler.screenHandler.getSlot(slotId).stack,int2ObjectMap
            )
        )







    }







}

object isDynamicIsland{
    var list = mutableStateMapOf<Slot, Boolean>()
    var containerList = mutableStateMapOf<Int, Slot>()

    fun register(syncId : Int){
        list.clear()
        containerList.clear()
        NotificationManager.notifies.add(Notify("ChestStealing","ChestStealing",-1,{
            val screen = MinecraftClient.getInstance().currentScreen as HandledScreen<*>
            if (screen.screenHandler.syncId == syncId) {
                screen.screenHandler.slots.filter { it.inventory != MinecraftClient.getInstance().player?.inventory }.sortedBy { it.index }.forEach { slot ->
                    containerList.put(slot.index,slot)
                    list.put(slot, true)
                }
            }
            val rows = 9
            val columns = if (screen.screenHandler.type == ScreenHandlerType.GENERIC_9X6) 6 else 3
            val modifier = Modifier.width(16.dp).height(16.dp).padding(3.dp)
            FlowRow(horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                maxItemsInEachRow = rows
            ) {
                containerList.forEach { (_,slot) ->
                    val isClosed = remember { mutableStateOf(false) }
                    val float = animateFloatAsState(
                        if (list.get(slot)!!) 1f else 0f, tween(durationMillis = 100), finishedListener = {
                            isClosed.value = true
                        }
                    )
                    val itemOutFloat = animateFloatAsState(
                        if (isClosed.value) 1f else 0f, tween(durationMillis = 80)
                    )
                   Box(modifier = modifier) {
                       if (slot.stack != ItemStack.EMPTY) {
                           Box(modifier = Modifier.fillMaxSize().scale(float.value).alpha(float.value)){
                               ItemIconView(slot.stack)
                           }

                       }else if (list.get(slot) == false){
                           Box(modifier = Modifier
                               .scale(itemOutFloat.value)
                               .alpha(itemOutFloat.value)
                               .background(Color.White.copy(alpha = 0.4f),RoundedCornerShape(10.dp))
                               .fillMaxSize()
                           )

                       }else {
                           Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray.copy(alpha = 0.7f), RoundedCornerShape(2.dp))) {}
                       }
                   }
                }

            }

        }))
    }


    fun itemOutAnimate(slot: Slot){
        if (list.contains(slot)){
            list.set(slot, false)
        }
    }


    fun unRegister(){
        NotificationManager.notifies.removeIf { it.titile == "ChestStealing" }

    }

    @Composable
    fun ItemIconView(itemStack: ItemStack) {
        val bitmap = remember(itemStack) { ItemIconLoader.getIcon(itemStack) }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawRect(Color.Gray)
            }
        }
    }

}

object ItemIconLoader {
    private val cache = mutableMapOf<Identifier, ImageBitmap?>()
    fun getIcon(itemStack: ItemStack): ImageBitmap? {
        if (itemStack.isEmpty) return null
        val modelId = itemStack.get(DataComponentTypes.ITEM_MODEL) ?: return null
        if (cache.containsKey(modelId)) return cache[modelId]
        val texturePath = Identifier.of(modelId.namespace, "textures/item/${modelId.path}.png")
        val bitmap = try {
            val resource = MinecraftClient.getInstance().resourceManager.getResource(texturePath)
            if (resource.isPresent) {
                resource.get().inputStream.use { stream ->
                    Image.makeFromEncoded(stream.readBytes()).toComposeImageBitmap()
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        cache[modelId] = bitmap
        return bitmap
    }
}