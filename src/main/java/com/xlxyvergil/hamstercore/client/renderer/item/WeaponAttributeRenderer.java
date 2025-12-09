package com.xlxyvergil.hamstercore.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import com.xlxyvergil.hamstercore.util.ElementNBTUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
        
        // 添加缓存中的属性到工具提示
        addCachedAttributes(tooltipElements, stack);
    }
    
    /**
     * 添加缓存中的属性到工具提示
     */
    private static void addCachedAttributes(List<Component> tooltipElements, ItemStack stack) {
        // 先调用一遍计算，确保缓存是最新的
        ElementDamageManager.getActiveElements(stack);
        
        // 从缓存中获取元素列表
        List<Map.Entry<ElementType, Double>> cachedElements = ElementDamageManager.getActiveElements(stack);
        
        if (cachedElements == null || cachedElements.isEmpty()) {
            return;
        }
        
        // 添加空行分隔
        tooltipElements.add(Component.literal(""));
        
        // 分别存储特殊属性值
        Double criticalChance = null;
        Double criticalDamage = null;
        Double triggerChance = null;
        
        // 检查是否有元素属性（除了特殊属性之外）
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
                // 不是特殊属性，说明有元素属性
                hasElements = true;
            }
        }
        
        // 添加特殊属性（暴击率、暴击伤害、触发率）
        boolean hasSpecialAttributes = (criticalChance != null && criticalChance > 0) ||
                                      (criticalDamage != null && criticalDamage > 0) ||
                                      (triggerChance != null && triggerChance > 0);
                                      
        if (hasSpecialAttributes) {
            // 添加特殊属性标题
            tooltipElements.add(Component.translatable("hamstercore.ui.special_attributes").withStyle(ChatFormatting.YELLOW));
            
            // 添加暴击率
            if (criticalChance != null && criticalChance > 0) {
                String text = String.format("%s: %.1f%%", 
                    Component.translatable("hamstercore.ui.critical_chance").getString(), 
                    criticalChance * 100);
                tooltipElements.add(Component.literal("  " + text).withStyle(ChatFormatting.GRAY));
            }
            
            // 添加暴击伤害
            if (criticalDamage != null && criticalDamage > 0) {
                String text = String.format("%s: %.1f", 
                    Component.translatable("hamstercore.ui.critical_damage").getString(), 
                    criticalDamage);
                tooltipElements.add(Component.literal("  " + text).withStyle(ChatFormatting.GRAY));
            }
            
            // 添加触发率
            if (triggerChance != null && triggerChance > 0) {
                String text = String.format("%s: %.1f%%", 
                    Component.translatable("hamstercore.ui.trigger_chance").getString(), 
                    triggerChance * 100);
                tooltipElements.add(Component.literal("  " + text).withStyle(ChatFormatting.GRAY));
            }
        }
        
        // 添加元素属性
        if (hasElements) {
            if (hasSpecialAttributes) {
                // 如果已经有特殊属性，添加一个空行作为分隔
                tooltipElements.add(Component.literal(""));
            }
            
            // 添加元素属性标题
            tooltipElements.add(Component.translatable("hamstercore.ui.element_ratios").withStyle(ChatFormatting.DARK_GREEN));
            
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
        tooltipElements.add(Component.translatable("hamstercore.ui.faction_damage_bonus").withStyle(ChatFormatting.GOLD));
        
        // 添加每个派系的增伤数值
        for (String faction : factions) {
            double modifier = ElementNBTUtils.getExtraFactionModifier(stack, faction);
            
            // 只显示非零的派系增伤
            if (modifier != 0) {
                String factionText = String.format("  %s: %.1f%%", 
                    Component.translatable("faction." + faction.toLowerCase() + ".name").getString(), 
                    modifier * 100);
                
                // 根据派系设置颜色
                ChatFormatting color = getFactionColor(faction);
                Component factionComponent = Component.literal(factionText).withStyle(color);
                tooltipElements.add(factionComponent);
            }
        }
    }
    
    /**
     * 获取派系颜色
     */
    private static ChatFormatting getFactionColor(String faction) {
        // 根据派系设置颜色
        switch (faction.toUpperCase()) {
            case "GRINEER":
                return ChatFormatting.RED;
            case "INFESTED":
                return ChatFormatting.GREEN;
            case "CORPUS":
                return ChatFormatting.BLUE;
            case "OROKIN":
                return ChatFormatting.LIGHT_PURPLE;
            case "SENTIENT":
                return ChatFormatting.DARK_RED;
            case "MURMUR":
                return ChatFormatting.AQUA;
            default:
                return ChatFormatting.WHITE;
        }
    }
}