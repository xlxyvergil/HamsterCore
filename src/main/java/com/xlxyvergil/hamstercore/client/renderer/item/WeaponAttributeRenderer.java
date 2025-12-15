package com.xlxyvergil.hamstercore.client.renderer.item;

import java.util.List;
import java.util.Map;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.modifier.ElementCombinationModifier;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import com.xlxyvergil.hamstercore.util.ElementHelper;
import com.xlxyvergil.hamstercore.util.ElementNBTUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 武器属性渲染器
 * 负责在物品栏界面显示武器的各种属性
 */
public class WeaponAttributeRenderer {
    
    public static void registerEvents() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new WeaponAttributeRenderer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        
        // 检查物品是否有任何元素
        if (!ElementNBTUtils.hasAnyElements(stack)) {
            return;
        }
        
        // 获取现有的工具提示行
        List<Component> tooltipElements = event.getToolTip();
        
        // 移除Minecraft自动生成的属性修饰符文本和Curios添加的槽位文本
        // 这些文本通常包含"attribute.modifier."、"attribute.name.hamstercore."或"curios.modifiers."等关键字
        tooltipElements.removeIf(component -> {
            String text = component.getString();
            return text.contains("attribute.modifier.") || 
                   text.contains("attribute.name.hamstercore.") ||
                   text.contains("curios.modifiers.") ||
                   text.contains("在身上") || 
                   text.contains("在任意") || 
                   text.contains("在头上") || 
                   text.contains("在腿上") || 
                   text.contains("在脚上");
        });
        
        // 添加Usage层属性到工具提示（仅包含基础元素和复合元素）
        addUsageAttributes(tooltipElements, stack);
    }
    
    /**
     * 添加Usage层属性到工具提示
     * 仅包含：基础元素和复合元素
     */
    private static void addUsageAttributes(List<Component> tooltipElements, ItemStack stack) {
        // 确保Usage层数据是最新的
        WeaponData weaponData = WeaponDataManager.loadElementData(stack);
        
        // 直接从ElementHelper获取最新的基础元素和复合元素值
        // 这里使用ElementCombinationModifier直接计算usage层值，避免递归
        if (weaponData != null) {
            ElementHelper.ElementCategoryData elementData = ElementHelper.getAllElementValuesByCategory(stack);
            Map<String, Double> basicAndComplexValues = elementData.getBasicAndComplexValues();
            
            // ElementCombinationModifier只处理基础元素和复合元素
            // 物理元素、特殊元素和派系元素直接通过修饰符获取值
            
            // 在安全的上下文中进行复合计算
            ElementCombinationModifier.computeElementCombinationsWithValues(weaponData, basicAndComplexValues);
        }
        
        // 先调用一遍计算，确保缓存是最新的
        ElementDamageManager.getActiveElements(stack);
        
        // 从缓存中获取元素列表
        List<Map.Entry<ElementType, Double>> cachedElements = ElementDamageManager.getActiveElements(stack);
        
        // 添加空行分隔
        tooltipElements.add(Component.literal(""));
        
        // 添加Usage层标题
        tooltipElements.add(Component.literal("Weapon Attributes").withStyle(ChatFormatting.YELLOW));
        
        // 从ElementHelper获取特殊元素和派系元素的计算值
        Map<String, Double> specialAndFactionValues = ElementHelper.getAllSpecialAndFactionValues(stack);
        
        // 添加特殊属性（从ElementHelper获取计算后的值）
        String[] specialElements = {"critical_chance", "critical_damage", "trigger_chance"};
        for (String specialElement : specialElements) {
            Double value = specialAndFactionValues.get(specialElement);
            if (value != null && value > 0) {
                String translationKey = "hamstercore.ui." + specialElement.replace("_", "");
                String formattedText;
                
                switch (specialElement) {
                    case "critical_chance":
                        formattedText = String.format("%s: %.1f%%", 
                            Component.translatable("hamstercore.ui.critical_chance").getString(), 
                            value * 100);
                        break;
                    case "critical_damage":
                        formattedText = String.format("%s: %.1f%%", 
                            Component.translatable("hamstercore.ui.critical_damage").getString(), 
                            value * 100);
                        break;
                    case "trigger_chance":
                        formattedText = String.format("%s: %.1f%%", 
                            Component.translatable("hamstercore.ui.trigger_chance").getString(), 
                            value * 100);
                        break;
                    default:
                        continue;
                }
                tooltipElements.add(Component.literal(formattedText));
            }
        }
        
        // 添加元素属性（从WeaponData获取，包括基础元素、复合元素和物理元素）
        boolean hasElements = false;
        
        // 遍历所有基础元素和复合元素类型
        String[] allElementTypes = {"heat", "cold", "electricity", "toxin", 
                                   "blast", "corrosive", "gas", "magnetic", "radiation", "viral"};
        
        for (String elementTypeName : allElementTypes) {
            ElementType elementType = ElementType.byName(elementTypeName);
            if (elementType == null) {
                continue;
            }
            
            double value = 0.0;
            if (weaponData != null) {
                // 从WeaponData获取元素值
                Double weaponValue = weaponData.getUsageValue(elementTypeName);
                if (weaponValue != null) {
                    value = weaponValue;
                }
            }
            
            // 如果WeaponData中没有值，尝试从ElementHelper获取
            if (value <= 0) {
                value = ElementHelper.getElementValueFromItem(stack, elementType);
            }
            
            // 跳过无效值
            if (value <= 0) {
                continue;
            }
            
            // 如果有有效的元素，添加标题（只添加一次）
            if (!hasElements) {
                tooltipElements.add(
                    Component.translatable("hamstercore.ui.element_ratios").append(":")
                );
                hasElements = true;
            }
            
            // 创建元素名称和数值的文本组件，使用元素颜色
            String elementName = Component.translatable("element." + elementType.getName() + ".name").getString();
            String elementText = String.format("  %s: %.2f", elementName, value);
            
            // 检查颜色是否有效，避免NullPointerException
            Integer colorValue = elementType.getColor().getColor();
            if (colorValue != null) {
                Component elementComponent = Component.literal(elementText)
                    .withStyle(style -> style.withColor(colorValue));
                tooltipElements.add(elementComponent);
            } else {
                // 如果没有有效颜色，使用默认样式
                tooltipElements.add(Component.literal(elementText));
            }
        }
        
        // 处理物理元素（slash, impact, puncture）- 直接从修饰符获取值
        String[] physicalElements = {"slash", "impact", "puncture"};
        for (String physicalType : physicalElements) {
            ElementType elementType = ElementType.byName(physicalType);
            if (elementType == null) {
                continue;
            }
            
            // 直接从修饰符获取物理元素值
            double value = ElementHelper.getElementValueFromItem(stack, elementType);
            
            // 跳过无效值
            if (value <= 0) {
                continue;
            }
            
            // 如果有有效的元素，添加标题（只添加一次）
            if (!hasElements) {
                tooltipElements.add(
                    Component.translatable("hamstercore.ui.element_ratios").append(":")
                );
                hasElements = true;
            }
            
            // 创建元素名称和数值的文本组件，使用元素颜色
            String elementName = Component.translatable("element." + elementType.getName() + ".name").getString();
            String elementText = String.format("  %s: %.2f", elementName, value);
            
            // 检查颜色是否有效，避免NullPointerException
            Integer colorValue = elementType.getColor().getColor();
            if (colorValue != null) {
                Component elementComponent = Component.literal(elementText)
                    .withStyle(style -> style.withColor(colorValue));
                tooltipElements.add(elementComponent);
            } else {
                // 如果没有有效颜色，使用默认样式
                tooltipElements.add(Component.literal(elementText));
            }
        }
        
        // 添加派系增伤属性到工具提示（从属性修饰符系统获取Forge计算后的值）
        addFactionAttributes(tooltipElements, stack);
    }
    
    /**
     * 添加派系增伤属性到工具提示（从ElementHelper获取Forge计算后的值）
     */
    private static void addFactionAttributes(List<Component> tooltipElements, ItemStack stack) {
        // 从ElementHelper获取特殊元素和派系元素的计算值
        Map<String, Double> specialAndFactionValues = ElementHelper.getAllSpecialAndFactionValues(stack);
        
        // 定义所有可能的派系类型
        String[] factionTypes = {"grineer", "infested", "corpus", "orokin", "sentient", "murmur"};
        
        boolean hasFactionBonuses = false;
        
        // 先检查是否有任何派系增伤
        for (String faction : factionTypes) {
            Double factionModifier = specialAndFactionValues.get(faction);
            if (factionModifier != null && factionModifier > 0) {
                hasFactionBonuses = true;
                break;
            }
        }
        
        // 只有在有派系增伤时才添加标题和空行
        if (hasFactionBonuses) {
            // 添加空行分隔
            tooltipElements.add(Component.literal(""));
            
            // 添加派系增伤标题
            tooltipElements.add(
                Component.translatable("hamstercore.ui.faction_damage_bonus").append(":")
            );
            
            // 添加每个派系的增伤数值（从ElementHelper获取计算后的值）
            for (String faction : factionTypes) {
                Double factionModifier = specialAndFactionValues.get(faction);
                
                // 只显示非零的派系增伤
                if (factionModifier != null && factionModifier > 0) {
                    String factionText = String.format("  %s: %.1f%%", 
                        Component.translatable("element." + faction + ".name").getString(), 
                        factionModifier * 100);
                    
                    // 根据派系设置颜色
                    ChatFormatting color = ChatFormatting.WHITE; // 默认颜色
                    switch (faction.toUpperCase()) {
                        case "GRINEER":
                            color = ChatFormatting.RED;
                            break;
                        case "INFESTED":
                            color = ChatFormatting.GREEN;
                            break;
                        case "CORPUS":
                            color = ChatFormatting.BLUE;
                            break;
                        case "OROKIN":
                            color = ChatFormatting.LIGHT_PURPLE;
                            break;
                        case "SENTIENT":
                            color = ChatFormatting.DARK_RED;
                            break;
                        case "MURMUR":
                            color = ChatFormatting.AQUA;
                            break;
                    }
                    
                    Component factionComponent = Component.literal(factionText).withStyle(color);
                    tooltipElements.add(factionComponent);
                }
            }
        }
    }
}