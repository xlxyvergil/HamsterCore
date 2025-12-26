package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.attribute.EntityAttributeRegistry;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * 元素使用数据处理器
 * 类似Apotheosis的词缀系统，处理将存储在物品NBT中的元素数据转换为实体属性
 * 通过ItemAttributeModifierEvent事件系统将元素属性应用到实体上
 */
@Mod.EventBusSubscriber
public class ElementUsageDataHandler {
    
    // 为不同类型的元素属性创建不同的UUID，确保每个属性修饰符的唯一性
    private static final UUID ELEMENT_MODIFIER_UUID = UUID.fromString("a238409a-00f7-433c-8bff-4a47239ddd8a");
    
    /**
     * 处理物品属性修饰符事件
     * 类似Apotheosis的ItemAttributeModifierEvent机制，从物品NBT中读取元素数据
     * 并将其转换为属性修饰符应用到实体上
     * 
     * @param event 物品属性修饰符事件
     */
    @SubscribeEvent
    public static void handleItemAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        
        // 检查物品是否在主手槽位，因为只有主手道具才有词缀
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 检查物品是否包含元素数据
        if (!ElementUsageData.hasElementData(stack)) {
            return;
        }
        
        // 从物品NBT中读取元素数据
        ElementUsageData.ElementData elementData = ElementUsageData.readElementDataFromItem(stack);
        
        // 应用暴击相关统计到实体属性
        applyCriticalStats(event, stack, elementData.getCriticalStats());
        
        // 应用物理元素到实体属性
        applyPhysicalElements(event, stack, elementData.getPhysicalElements());
        
        // 应用派系元素到实体属性
        applyFactionElements(event, stack, elementData.getFactionElements());
        
        // 应用复合元素到实体属性
        applyCombinedElements(event, stack, elementData.getCombinedElements());
    }
    
    /**
     * 应用暴击相关统计到实体属性
     * @param event 物品属性修饰符事件
     * @param stack 物品堆栈
     * @param criticalStats 暴击相关统计数据
     */
    private static void applyCriticalStats(ItemAttributeModifierEvent event, ItemStack stack, Map<String, Double> criticalStats) {
        for (Map.Entry<String, Double> entry : criticalStats.entrySet()) {
            String elementType = entry.getKey();
            double value = entry.getValue();
            
            // 根据元素类型名称映射到对应的实体属性
            Attribute attribute = getAttributeByElementType(elementType);
            if (attribute != null) {
                // 创建唯一ID的属性修饰符，基于物品堆栈哈希值、元素类型和槽位
                UUID modifierUUID = createUniqueUUID(stack, elementType, event.getSlotType());
                
                // 创建属性修饰符
                AttributeModifier modifier = new AttributeModifier(
                    modifierUUID,
                    "ElementUsageData." + elementType,
                    value,
                    AttributeModifier.Operation.ADDITION
                );
                
                // 添加属性修饰符到事件
                event.addModifier(attribute, modifier);
            }
        }
    }
    
    /**
     * 应用物理元素到实体属性
     * @param event 物品属性修饰符事件
     * @param stack 物品堆栈
     * @param physicalElements 物理元素数据
     */
    private static void applyPhysicalElements(ItemAttributeModifierEvent event, ItemStack stack, Map<String, Double> physicalElements) {
        for (Map.Entry<String, Double> entry : physicalElements.entrySet()) {
            String elementType = entry.getKey();
            double value = entry.getValue();
            
            // 根据元素类型名称映射到对应的实体属性
            Attribute attribute = getAttributeByElementType(elementType);
            if (attribute != null) {
                // 创建唯一ID的属性修饰符，基于物品堆栈哈希值、元素类型和槽位
                UUID modifierUUID = createUniqueUUID(stack, elementType, event.getSlotType());
                
                // 创建属性修饰符
                AttributeModifier modifier = new AttributeModifier(
                    modifierUUID,
                    "ElementUsageData." + elementType,
                    value,
                    AttributeModifier.Operation.ADDITION
                );
                
                // 添加属性修饰符到事件
                event.addModifier(attribute, modifier);
            }
        }
    }
    
    /**
     * 应用派系元素到实体属性
     * @param event 物品属性修饰符事件
     * @param stack 物品堆栈
     * @param factionElements 派系元素数据
     */
    private static void applyFactionElements(ItemAttributeModifierEvent event, ItemStack stack, Map<String, Double> factionElements) {
        for (Map.Entry<String, Double> entry : factionElements.entrySet()) {
            String elementType = entry.getKey();
            double value = entry.getValue();
            
            // 根据元素类型名称映射到对应的实体属性
            Attribute attribute = getAttributeByElementType(elementType);
            if (attribute != null) {
                // 创建唯一ID的属性修饰符，基于物品堆栈哈希值、元素类型和槽位
                UUID modifierUUID = createUniqueUUID(stack, elementType, event.getSlotType());
                
                // 创建属性修饰符
                AttributeModifier modifier = new AttributeModifier(
                    modifierUUID,
                    "ElementUsageData." + elementType,
                    value,
                    AttributeModifier.Operation.ADDITION
                );
                
                // 添加属性修饰符到事件
                event.addModifier(attribute, modifier);
            }
        }
    }
    
    /**
     * 应用复合元素到实体属性
     * @param event 物品属性修饰符事件
     * @param stack 物品堆栈
     * @param combinedElements 复合元素数据
     */
    private static void applyCombinedElements(ItemAttributeModifierEvent event, ItemStack stack, Map<String, Double> combinedElements) {
        for (Map.Entry<String, Double> entry : combinedElements.entrySet()) {
            String elementType = entry.getKey();
            double value = entry.getValue();
            
            // 根据元素类型名称映射到对应的实体属性
            Attribute attribute = getAttributeByElementType(elementType);
            if (attribute != null) {
                // 创建唯一ID的属性修饰符，基于物品堆栈哈希值、元素类型和槽位
                UUID modifierUUID = createUniqueUUID(stack, elementType, event.getSlotType());
                
                // 创建属性修饰符
                AttributeModifier modifier = new AttributeModifier(
                    modifierUUID,
                    "ElementUsageData." + elementType,
                    value,
                    AttributeModifier.Operation.ADDITION
                );
                
                // 添加属性修饰符到事件
                event.addModifier(attribute, modifier);
            }
        }
    }
    
    /**
     * 根据元素类型名称获取对应的实体属性
     * @param elementType 元素类型名称
     * @return 对应的实体属性，如果未找到则返回null
     */
    private static Attribute getAttributeByElementType(String elementType) {
        switch (elementType) {
            // 物理元素
            case "impact":
                return EntityAttributeRegistry.IMPACT.get();
            case "puncture":
                return EntityAttributeRegistry.PUNCTURE.get();
            case "slash":
                return EntityAttributeRegistry.SLASH.get();
            // 基础元素
            case "cold":
                return EntityAttributeRegistry.COLD.get();
            case "electricity":
                return EntityAttributeRegistry.ELECTRICITY.get();
            case "heat":
                return EntityAttributeRegistry.HEAT.get();
            case "toxin":
                return EntityAttributeRegistry.TOXIN.get();
            // 复合元素
            case "blast":
                return EntityAttributeRegistry.BLAST.get();
            case "corrosive":
                return EntityAttributeRegistry.CORROSIVE.get();
            case "gas":
                return EntityAttributeRegistry.GAS.get();
            case "magnetic":
                return EntityAttributeRegistry.MAGNETIC.get();
            case "radiation":
                return EntityAttributeRegistry.RADIATION.get();
            case "viral":
                return EntityAttributeRegistry.VIRAL.get();
            // 特殊属性
            case "critical_chance":
                return EntityAttributeRegistry.CRITICAL_CHANCE.get();
            case "critical_damage":
                return EntityAttributeRegistry.CRITICAL_DAMAGE.get();
            case "trigger_chance":
                return EntityAttributeRegistry.TRIGGER_CHANCE.get();
            // 派系元素
            case "grineer":
                return EntityAttributeRegistry.GRINEER.get();
            case "infested":
                return EntityAttributeRegistry.INFESTED.get();
            case "corpus":
                return EntityAttributeRegistry.CORPUS.get();
            case "orokin":
                return EntityAttributeRegistry.OROKIN.get();
            case "sentient":
                return EntityAttributeRegistry.SENTIENT.get();
            case "murmur":
                return EntityAttributeRegistry.MURMUR.get();
            default:
                return null;
        }
    }
    
    /**
     * 为属性修饰符创建唯一UUID
     * @param stack 物品堆栈
     * @param elementType 元素类型名称
     * @param slot 装备槽位
     * @return 基于物品堆栈、元素类型和槽位的唯一UUID
     */
    private static UUID createUniqueUUID(ItemStack stack, String elementType, EquipmentSlot slot) {
        // 使用物品堆栈的哈希值、元素类型和槽位来生成唯一ID
        long mostSigBits = ELEMENT_MODIFIER_UUID.getMostSignificantBits();
        long leastSigBits = ELEMENT_MODIFIER_UUID.getLeastSignificantBits() ^ stack.getItem().hashCode() ^ elementType.hashCode() ^ slot.hashCode() ^ stack.getOrCreateTag().hashCode();
        return new UUID(mostSigBits, leastSigBits);
    }
}