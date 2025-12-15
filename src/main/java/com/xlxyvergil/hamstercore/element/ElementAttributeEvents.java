package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xlxyvergil.hamstercore.HamsterCore.MODID;
import com.xlxyvergil.hamstercore.api.element.ElementAttributeAPI;

/**
 * 元素属性事件处理器
 * 参考Apotheosis的AdventureEvents实现
 */
@Mod.EventBusSubscriber(modid = MODID)
public class ElementAttributeEvents {

    /**
     * 处理物品属性修饰符事件
     * 只通过event.addModifier()安全地添加修饰符，从不直接修改或删除ItemStack的NBT
     */
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        
        // 检查物品是否有元素修饰符
        if (ElementAttributeAPI.hasAnyElementModifiers(stack)) {
            // 获取物品的所有元素修饰符
            ElementAttributeAPI.getAllElementModifiers(stack).forEach((attribute, modifiers) -> {
                // 遍历所有修饰符并添加到事件中
                modifiers.forEach(modifier -> {
                    // 只在正确的槽位应用修饰符
                    if (modifier.getSlot() == event.getSlotType()) {
                        event.addModifier(attribute, modifier.getModifier());
                    }
                });
            });
        }
    }

    /**
     * 元素修饰符包装类
     * 用于存储修饰符和应用的槽位
     */
    public static record ElementModifierWrapper(Attribute attribute, AttributeModifier modifier, EquipmentSlot slot) {
        public Attribute getAttribute() {
            return attribute;
        }

        public AttributeModifier getModifier() {
            return modifier;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }
    }
}
