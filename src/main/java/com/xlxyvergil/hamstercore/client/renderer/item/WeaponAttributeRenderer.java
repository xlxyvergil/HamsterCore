package com.xlxyvergil.hamstercore.client.renderer.item;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import com.xlxyvergil.hamstercore.util.ElementNBTUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 武器属性渲染器
 * 负责在物品栏界面显示武器的各种属性
 */
public class WeaponAttributeRenderer {
    
    public static void registerEvents() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new WeaponAttributeRenderer());
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        
        // 检查物品是否有任何元素
        if (!ElementNBTUtils.hasAnyElements(stack)) {
            return;
        }
        
        // 获取现有的工具提示行
        List<Component> tooltipElements = event.getToolTip();
        
        // 检查是否应该显示Usage层数据
        if (shouldShowUsageLayer(stack)) {
            // 添加Usage层属性到工具提示（特殊属性+元素属性+派系增伤）
             addUsageAttributes(tooltipElements, stack);             
             addBasicAttributes(tooltipElements, stack);           
        } else {
            // 添加Basic层属性到工具提示（特殊属性+元素属性）


        }
    }
    
    /**
     * 判断是否应该显示Usage层数据
     * 如果Usage层有任何有效数据（大于0或不等于0），则显示Usage层，否则显示Basic层
     */
    private static boolean shouldShowUsageLayer(ItemStack stack) {
        // 加载武器数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        
        // 计算Usage层数据
        WeaponDataManager.computeUsageData(stack, data);
        
        // 检查Usage层的特殊属性（暴击率、暴击伤害、触发率）
        Double criticalChance = data.getUsageValue("critical_chance");
        Double criticalDamage = data.getUsageValue("critical_damage");
        Double triggerChance = data.getUsageValue("trigger_chance");
        
        if ((criticalChance != null && criticalChance > 0) || 
            (criticalDamage != null && criticalDamage > 0) || 
            (triggerChance != null && triggerChance > 0)) {
            return true;
        }
        
        // 检查Usage层的元素属性（物理、基础、复合元素）
        for (Map.Entry<String, Double> entry : data.getAllUsageValues().entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            
            // 排除特殊属性，只检查元素属性
            if (value != null && value > 0 && 
                !"critical_chance".equals(key) && 
                !"critical_damage".equals(key) && 
                !"trigger_chance".equals(key)) {
                return true;
            }
        }
        
        // 检查Extra层的派系增伤属性
        for (Map.Entry<String, com.xlxyvergil.hamstercore.element.ExtraEntry> entry : data.getAllExtraFactions().entrySet()) {
            com.xlxyvergil.hamstercore.element.ExtraEntry extraEntry = entry.getValue();
            if (extraEntry != null) {
                double value = "add".equals(extraEntry.getOperation()) ? extraEntry.getValue() : 
                             ("sub".equals(extraEntry.getOperation()) ? -extraEntry.getValue() : 0.0);
                if (value != 0) {
                    return true;
                }
            }
        }
        
        // 如果以上都没有有效数据，则显示Basic层
        return false;
    }
    
    /**
     * 添加Usage层属性到工具提示
     * 包括：特殊属性（暴击率、暴击伤害、触发率）+ 元素属性 + 派系增伤
     */
    private static void addUsageAttributes(List<Component> tooltipElements, ItemStack stack) {
        // 先调用一遍计算，确保缓存是最新的
        ElementDamageManager.getActiveElements(stack);
        
        // 从缓存中获取元素列表
        List<Map.Entry<ElementType, Double>> cachedElements = ElementDamageManager.getActiveElements(stack);
        
        if (cachedElements == null || cachedElements.isEmpty()) {
            return;
        }
        
        // 添加空行分隔
        tooltipElements.add(Component.literal(""));
        
        // 添加Usage层标题
        tooltipElements.add(Component.literal("Usage").withStyle(ChatFormatting.YELLOW));
        
        // 分离特殊属性和元素属性
        Double criticalChance = null;
        Double criticalDamage = null;
        Double triggerChance = null;
        
        // 添加元素属性标题
        boolean hasElements = false;
        for (Map.Entry<ElementType, Double> entry : cachedElements) {
            ElementType elementType = entry.getKey();
            double value = entry.getValue();
            
            if (value <= 0) {
                continue;
            }
            
            // 检查是否为特殊属性
            if (elementType == ElementType.CRITICAL_CHANCE) {
                criticalChance = value;
            } else if (elementType == ElementType.CRITICAL_DAMAGE) {
                criticalDamage = value;
            } else if (elementType == ElementType.TRIGGER_CHANCE) {
                triggerChance = value;
            } else {
                hasElements = true;
            }
        }
        
        // 添加特殊属性
        if (criticalChance != null && criticalChance > 0) {
            String criticalChanceText = String.format("%s: %.1f%%", 
                Component.translatable("hamstercore.ui.critical_chance").getString(), 
                criticalChance * 100);
            tooltipElements.add(Component.literal(criticalChanceText));
        }
        
        if (criticalDamage != null && criticalDamage > 0) {
            String criticalDamageText = String.format("%s: %.1f", 
                Component.translatable("hamstercore.ui.critical_damage").getString(), 
                criticalDamage);
            tooltipElements.add(Component.literal(criticalDamageText));
        }
        
        if (triggerChance != null && triggerChance > 0) {
            String triggerChanceText = String.format("%s: %.1f%%", 
                Component.translatable("hamstercore.ui.trigger_chance").getString(), 
                triggerChance * 100);
            tooltipElements.add(Component.literal(triggerChanceText));
        }
        
        // 添加元素属性
        if (hasElements) {
            tooltipElements.add(
                Component.translatable("hamstercore.ui.element_ratios").append(":")
            );
            
            for (Map.Entry<ElementType, Double> entry : cachedElements) {
                ElementType elementType = entry.getKey();
                double value = entry.getValue();
                
                // 跳过特殊属性和无效值
                if (value <= 0 || 
                    elementType == ElementType.CRITICAL_CHANCE || 
                    elementType == ElementType.CRITICAL_DAMAGE || 
                    elementType == ElementType.TRIGGER_CHANCE) {
                    continue;
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
        }
        
        // 添加派系增伤属性到工具提示
        addFactionAttributes(tooltipElements, stack);
    }
    
    /**
     * 添加Basic层属性到工具提示
     * 包括：特殊属性（暴击率、暴击伤害、触发率）+ 元素属性
     */
    private static void addBasicAttributes(List<Component> tooltipElements, ItemStack stack) {
        // 获取Basic层数据
        WeaponElementData data = WeaponDataManager.loadElementData(stack);
        
        // 添加空行分隔
        tooltipElements.add(Component.literal(""));
        
        // 添加Basic层标题
        tooltipElements.add(Component.literal("Basic").withStyle(ChatFormatting.YELLOW));
        
        // 添加特殊属性
        Double criticalChance = null;
        Double criticalDamage = null;
        Double triggerChance = null;
        
        // 获取Basic层特殊属性
        com.xlxyvergil.hamstercore.element.BasicEntry criticalChanceEntry = data.getBasicElement("critical_chance");
        com.xlxyvergil.hamstercore.element.BasicEntry criticalDamageEntry = data.getBasicElement("critical_damage");
        com.xlxyvergil.hamstercore.element.BasicEntry triggerChanceEntry = data.getBasicElement("trigger_chance");
        
        if (criticalChanceEntry != null) {
            criticalChance = criticalChanceEntry.getValue();
        }
        
        if (criticalDamageEntry != null) {
            criticalDamage = criticalDamageEntry.getValue();
        }
        
        if (triggerChanceEntry != null) {
            triggerChance = triggerChanceEntry.getValue();
        }
        
        // 添加特殊属性
        if (criticalChance != null && criticalChance > 0) {
            String criticalChanceText = String.format("%s: %.1f%%", 
                Component.translatable("hamstercore.ui.critical_chance").getString(), 
                criticalChance * 100);
            tooltipElements.add(Component.literal(criticalChanceText));
        }
        
        if (criticalDamage != null && criticalDamage > 0) {
            String criticalDamageText = String.format("%s: %.1f", 
                Component.translatable("hamstercore.ui.critical_damage").getString(), 
                criticalDamage);
            tooltipElements.add(Component.literal(criticalDamageText));
        }
        
        if (triggerChance != null && triggerChance > 0) {
            String triggerChanceText = String.format("%s: %.1f%%", 
                Component.translatable("hamstercore.ui.trigger_chance").getString(), 
                triggerChance * 100);
            tooltipElements.add(Component.literal(triggerChanceText));
        }
        
        // 添加元素属性
        boolean hasElements = false;
        for (Map.Entry<String, com.xlxyvergil.hamstercore.element.BasicEntry> entry : data.getAllBasicElements().entrySet()) {
            String key = entry.getKey();
            com.xlxyvergil.hamstercore.element.BasicEntry basicEntry = entry.getValue();
            
            // 排除特殊属性
            if (!"critical_chance".equals(key) && 
                !"critical_damage".equals(key) && 
                !"trigger_chance".equals(key) &&
                basicEntry != null && 
                basicEntry.getValue() > 0) {
                hasElements = true;
                break;
            }
        }
        
        if (hasElements) {
            tooltipElements.add(
                Component.translatable("hamstercore.ui.element_ratios").append(":")
            );
            
            for (Map.Entry<String, com.xlxyvergil.hamstercore.element.BasicEntry> entry : data.getAllBasicElements().entrySet()) {
                String key = entry.getKey();
                com.xlxyvergil.hamstercore.element.BasicEntry basicEntry = entry.getValue();
                
                // 排除特殊属性
                if ("critical_chance".equals(key) || 
                    "critical_damage".equals(key) || 
                    "trigger_chance".equals(key) ||
                    basicEntry == null || 
                    basicEntry.getValue() <= 0) {
                    continue;
                }
                
                // 获取元素类型
                ElementType elementType = ElementType.byName(key);
                if (elementType == null) {
                    continue;
                }
                
                // 创建元素名称和数值的文本组件，使用元素颜色
                String elementName = Component.translatable("element." + elementType.getName() + ".name").getString();
                String elementText = String.format("  %s: %.2f", elementName, basicEntry.getValue());
                
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
        }
    }
    
    /**
     * 添加派系增伤属性到工具提示
     */
    private static void addFactionAttributes(List<Component> tooltipElements, ItemStack stack) {
        // 获取Extra层派系数据
        Set<String> factions = ElementNBTUtils.getAllExtraFactions(stack);
        
        // 如果没有派系增伤数据，则直接返回
        if (factions.isEmpty()) {
            return;
        }
        
        // 添加空行分隔
        tooltipElements.add(Component.literal(""));
        
        // 添加派系增伤标题
        tooltipElements.add(
            Component.translatable("hamstercore.ui.faction_damage_bonus").append(":")
        );
        
        // 添加每个派系的增伤数值
        for (String faction : factions) {
            double modifier = ElementNBTUtils.getExtraFactionModifier(stack, faction);
            
            // 只显示非零的派系增伤
            if (modifier != 0) {
                String factionText = String.format("  %s: %.1f%%", 
                    Component.translatable("faction." + faction.toLowerCase() + ".name").getString(), 
                    modifier * 100);
                
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