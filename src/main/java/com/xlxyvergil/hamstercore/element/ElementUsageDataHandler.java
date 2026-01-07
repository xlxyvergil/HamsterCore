package com.xlxyvergil.hamstercore.element;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * 元素使用数据处理器
 * 通过ItemAttributeModifierEvent事件系统将元素属性应用到实体上
 */
@Mod.EventBusSubscriber(modid = "hamstercore")
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
        
        // 只有主手装备才应用元素属性
        if (event.getSlotType() != net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 检查物品是否包含元素数据
        if (!ElementUsageData.hasElementData(stack)) {
            return;
        }
        
        // 从物品NBT中读取元素数据
        List<ElementUsageData.AttributeModifierEntry> modifierEntries = ElementUsageData.readElementDataFromItem(stack);
        
        // 应用所有属性修饰符
        applyAllAttributes(event, modifierEntries);
    }
    
    /**
     * 应用所有属性修饰符
     */
    private static void applyAllAttributes(ItemAttributeModifierEvent event, List<ElementUsageData.AttributeModifierEntry> modifierEntries) {
        // 直接应用每个属性修饰符条目
        for (ElementUsageData.AttributeModifierEntry entry : modifierEntries) {
            applyAttributeModifier(event, entry);
        }
    }
    
    /**
     * 应用单个属性修饰符
     */
    private static void applyAttributeModifier(ItemAttributeModifierEvent event, ElementUsageData.AttributeModifierEntry entry) {
        // 使用elementType作为属性ID，确保包含命名空间
        String attributeId = entry.getElementType();
        
        // 尝试获取属性，无论是Minecraft原生属性还是其他mod的属性
        Attribute attribute = getAttributeById(attributeId);
        if (attribute == null) {
            return; // 如果属性不存在，则跳过
        }
        
        // 获取操作类型
        AttributeModifier.Operation operation = getOperationFromEntry(entry.getOperation());
        
        double adjustedAmount = adjustAttributeValue(entry);
        
        // 直接使用Entry中存储的UUID和其他数据生成属性修饰符
        AttributeModifier modifier = new AttributeModifier(
            entry.getUuid(), // 使用Entry中存储的UUID
            entry.getName(), // 使用Entry中存储的名称
            adjustedAmount, // 使用调整后的数值
            operation // 使用Entry中存储的操作类型
        );
        
        // 添加属性修饰符到事件
        event.addModifier(attribute, modifier);
    }
    
    /**
     * 获取操作类型（固定为加法）
     */
    private static AttributeModifier.Operation getOperationFromEntry(String operationString) {
        return AttributeModifier.Operation.ADDITION;
    }
    
    /**
     * 根据ID获取属性
     */
    private static Attribute getAttributeById(String attributeId) {
        try {
            // 尝试获取属性，无论是Minecraft原生属性还是其他mod的属性
            ResourceLocation rl = new ResourceLocation(attributeId);
            return ForgeRegistries.ATTRIBUTES.getValue(rl);
        } catch (Exception e) {
            // 如果失败，返回null
            return null;
        }
    }
    
    /**
     * 调整属性值，对特定属性进行特殊处理
     */
    private static double adjustAttributeValue(ElementUsageData.AttributeModifierEntry entry) {
        String elementType = entry.getElementType();
        double amount = entry.getAmount();
        
        // 检查是否为暴击相关属性
        if (elementType.contains("crit_damage")) {
            // 特殊处理 crit_damage：减去 1.5，使武器默认值 2.0 加上玩家默认 0.5 后为 2.0
            // 这个处理始终应用，因为计算已经完成
            amount -= 1.5;
        } else if (elementType.contains("crit_chance")) {
            // 特殊处理 crit_chance：减去 0.05
            amount -= 0.05;
        }
        
        return amount;
    }
}