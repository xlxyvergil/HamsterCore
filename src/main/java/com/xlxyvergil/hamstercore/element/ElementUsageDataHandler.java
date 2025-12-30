package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.attribute.EntityAttributeRegistry;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 元素使用数据处理器
 * 类似Apotheosis的词缀系统，处理将存储在物品NBT中的元素数据转换为实体属性
 * 通过ItemAttributeModifierEvent事件系统将元素属性应用到实体上
 */
@Mod.EventBusSubscriber
public class ElementUsageDataHandler {
    
    /**
     * 处理物品属性修饰符事件
     * 直接从ElementUsageData中读取数据并生成AttributeModifier
     * 使用ElementUsageData中存储的UUID，不重新生成
     * 
     * @param event 物品属性修饰符事件
     */
    @SubscribeEvent
    public static void handleItemAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        
        // 检查物品是否在主手槽位
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 检查物品是否包含元素数据
        if (!ElementUsageData.hasElementData(stack)) {
            return;
        }
        
        // 从物品NBT中读取元素数据
        ElementUsageData.ElementData elementData = ElementUsageData.readElementDataFromItem(stack);
        
        // 从WeaponData获取所有InitialModifiers（用于生成AttributeModifier）
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        List<InitialModifierEntry> allModifiers = weaponData.getInitialModifiers();
        
        // 应用所有属性修饰符
        applyAllAttributes(event, stack, allModifiers);
    }
    
    /**
     * 应用所有属性修饰符
     */
    private static void applyAllAttributes(ItemAttributeModifierEvent event, ItemStack stack, List<InitialModifierEntry> modifiers) {
        // 直接应用每个属性修饰符
        for (InitialModifierEntry modifierEntry : modifiers) {
            applyAttributeModifier(event, stack, modifierEntry, event.getSlotType());
        }
    }
    
    /**
     * 应用单个属性修饰符
     */
    private static void applyAttributeModifier(ItemAttributeModifierEvent event, ItemStack stack, InitialModifierEntry modifierEntry, EquipmentSlot slot) {
        // 获取属性名称
        String attributeName = modifierEntry.getName();
        
        // 尝试获取Minecraft属性，如果获取不到则跳过
        Attribute attribute = getMinecraftAttributeById(attributeName);
        if (attribute == null) {
            return; // 如果属性不存在，则跳过
        }
        
        // 获取操作类型
        AttributeModifier.Operation operation = getOperationFromEntry(modifierEntry.getOperation());
        
        // 直接使用Entry中存储的UUID，不重新生成
        AttributeModifier modifier = new AttributeModifier(
            modifierEntry.getUuid(), // 使用Entry中存储的UUID
            "ElementUsageData." + attributeName, // 使用Entry中存储的name
            modifierEntry.getAmount(), // 使用Entry中存储的amount
            operation // 使用Entry中存储的operation
        );
        
        // 添加属性修饰符到事件
        event.addModifier(attribute, modifier);
    }
    
    /**
     * 从字符串获取操作类型
     */
    private static AttributeModifier.Operation getOperationFromEntry(String operationString) {
        switch (operationString.toUpperCase()) {
            case "MULTIPLY_BASE":
                return AttributeModifier.Operation.MULTIPLY_BASE;
            case "MULTIPLY_TOTAL":
                return AttributeModifier.Operation.MULTIPLY_TOTAL;
            default:
                return AttributeModifier.Operation.ADDITION;
        }
    }
    
    /**
     * 根据ID获取Minecraft属性
     */
    private static Attribute getMinecraftAttributeById(String attributeId) {
        try {
            // 尝试获取原生Minecraft属性或注册表中的属性
            ResourceLocation rl = new ResourceLocation(attributeId);
            return ForgeRegistries.ATTRIBUTES.getValue(rl);
        } catch (Exception e) {
            // 如果失败，返回null
            return null;
        }
    }
}