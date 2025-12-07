package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 元素属性事件处理器，负责处理元素属性的应用和计算
 */
@Mod.EventBusSubscriber(modid = HamsterCore.MODID)
public class ElementEventHandler {
    
    /**
     * 在物品属性修饰事件中应用元素属性
     * 这是将元素属性应用到物品的主要入口点
     */
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        EquipmentSlot slotType = event.getSlotType();
        
        // 检查物品是否有元素属性
        if (ElementHelper.hasElementAttributes(stack)) {
            // 应用元素属性到指定的装备槽位
            ElementHelper.applyElementAttributes(stack, slotType, event::addModifier);
        }
    }
}