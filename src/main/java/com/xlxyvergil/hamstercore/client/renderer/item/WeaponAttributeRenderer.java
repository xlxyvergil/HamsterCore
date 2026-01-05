package com.xlxyvergil.hamstercore.client.renderer.item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.datafixers.util.Either;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.util.ElementNBTUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;


/**
 * 武器属性渲染器
 * 负责在物品栏界面显示武器的各种属性
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WeaponAttributeRenderer {

    // 武器属性渲染方法，用于RenderTooltipEvent.GatherComponents事件
    public static void addWeaponAttributesToTooltip(List<Either<FormattedText, TooltipComponent>> tooltipElements, ItemStack stack) {
        // 检查是否有任何元素数据
        if (!ElementNBTUtils.hasNonZeroElements(stack)) {
            return;
        }
        
        // 添加"Weapon Attributes"标题
        tooltipElements.add(Either.left(Component.translatable("hamstercore.ui.weapon_attributes").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD)));
        
        // 显示物理元素
        addPhysicalElementsToTooltip(stack, tooltipElements);
        
        // 显示基础元素和复合元素
        addBasicAndComplexElementsToTooltip(stack, tooltipElements);
        
        // 显示特殊元素属性（暴击率、暴击伤害、触发率、攻击速度、攻击范围等）
        addSpecialAttributesToTooltip(stack, tooltipElements);
        
        // 显示派系元素
        addFactionAttributesToTooltip(stack, tooltipElements);
    }
    
    /**
     * 显示物理元素
     */
    private static void addPhysicalElementsToTooltip(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        Map<String, Double> physicalElements = ElementNBTUtils.readPhysicalElements(stack);
        
        // 如果没有物理元素，直接返回
        if (physicalElements.isEmpty()) {
            return;
        }
        
        // 遍历所有物理元素类型
        String[] physicalTypes = {"impact", "puncture", "slash"};
        for (String type : physicalTypes) {
            if (physicalElements.containsKey(type)) {
                double value = physicalElements.get(type);
                if (value <= 0) {
                    continue;
                }
                
                // 查找对应的ElementType以获取颜色和名称
                ElementType elementType = ElementType.byName(type);
                if (elementType != null) {
                    // 格式化并添加到工具提示
                    String formattedValue = String.format("%.1f", value);
                    MutableComponent component = Component.literal("  ")
                            .append(elementType.getColoredName())
                            .append(Component.literal(": "))
                            .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
                    tooltipElements.add(Either.left(component));
                }
            }
        }
    }
    
    /**
     * 显示基础元素和复合元素
     */
    private static void addBasicAndComplexElementsToTooltip(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        Map<String, Double> combinedElements = ElementNBTUtils.readCombinedElements(stack);
        
        // 如果没有基础或复合元素，直接返回
        if (combinedElements.isEmpty()) {
            return;
        }
        
        // 基础元素类型顺序
        String[] basicTypes = {"cold", "electricity", "heat", "toxin"};
        for (String type : basicTypes) {
            if (combinedElements.containsKey(type)) {
                double value = combinedElements.get(type);
                if (value <= 0) {
                    continue;
                }
                
                // 查找对应的ElementType以获取颜色和名称
                ElementType elementType = ElementType.byName(type);
                if (elementType != null) {
                    // 格式化并添加到工具提示
                    String formattedValue = String.format("%.1f", value);
                    MutableComponent component = Component.literal("  ")
                            .append(elementType.getColoredName())
                            .append(Component.literal(": "))
                            .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
                    tooltipElements.add(Either.left(component));
                }
            }
        }
        
        // 复合元素类型顺序
        String[] complexTypes = {"blast", "corrosive", "gas", "magnetic", "radiation", "viral"};
        for (String type : complexTypes) {
            if (combinedElements.containsKey(type)) {
                double value = combinedElements.get(type);
                if (value <= 0) {
                    continue;
                }
                
                // 查找对应的ElementType以获取颜色和名称
                ElementType elementType = ElementType.byName(type);
                if (elementType != null) {
                    // 格式化并添加到工具提示
                    String formattedValue = String.format("%.1f", value);
                    MutableComponent component = Component.literal("  ")
                            .append(elementType.getColoredName())
                            .append(Component.literal(": "))
                            .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
                    tooltipElements.add(Either.left(component));
                }
            }
        }
    }
    
    /**
     * 显示特殊元素属性（暴击率、暴击伤害、触发率、攻击速度、攻击范围等）
     */
    private static void addSpecialAttributesToTooltip(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        Map<String, Double> allElements = ElementNBTUtils.readAllElementValues(stack);
        
        // 处理暴击相关属性
        Map<String, Double> criticalStats = ElementNBTUtils.readCriticalStats(stack);
        String[] criticalAttributes = {"crit_chance", "crit_damage", "trigger_chance"};
        for (String attribute : criticalAttributes) {
            if (criticalStats.containsKey(attribute)) {
                double value = criticalStats.get(attribute);
                if (value > 0) {
                    // 查找匹配的元素类型（用于获取完整的elementType以生成翻译键）
                    String fullElementType = findMatchingElementType(allElements, attribute);
                    if (fullElementType == null) {
                        fullElementType = attribute;
                    }
                    
                    // 格式化数值
                    String formattedValue = String.format("%.1f%%", value * 100);
                    
                    // 获取显示名称，使用正确的翻译键格式（如attribute.name.attributeslib.crit_chance）
                    String translationKey;
                    if (fullElementType.contains(":")) {
                        // 将elementType按冒号分割为命名空间和属性名，转换为点号格式
                        String[] parts = fullElementType.split(":");
                        translationKey = "attribute.name." + parts[0] + "." + parts[1];
                    } else {
                        // 对于没有命名空间的属性，直接使用
                        translationKey = "attribute.name." + fullElementType;
                    }
                    MutableComponent displayName = Component.translatable(translationKey);
                    
                    // 设置颜色
                    ChatFormatting color = ChatFormatting.RED;
                    if (attribute.equals("trigger_chance")) {
                        color = ChatFormatting.YELLOW;
                    }
                    
                    MutableComponent component = Component.literal("  ")
                            .append(displayName.withStyle(color))
                            .append(Component.literal(": "))
                            .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
                    tooltipElements.add(Either.left(component));
                }
            }
        }
        
        // 处理攻击速度和攻击范围
        String[] speedRangeAttributes = {"generic.attack_speed", "player.block_interaction_range"};
        for (String attribute : speedRangeAttributes) {
            // 查找匹配的元素类型
            String fullElementType = findMatchingElementType(allElements, attribute);
            if (fullElementType != null) {
                double value = allElements.get(fullElementType);
                if (value != 0) {
                    // 格式化数值
                    String formattedValue;
                    if (attribute.equals("player.block_interaction_range")) {
                        formattedValue = String.format("%.2f m", value);
                    } else {
                        formattedValue = String.format("%.2f", value);
                    }
                    
                    // 获取显示名称，使用正确的翻译键格式（如attribute.name.minecraft.generic.attack_speed）
                    String translationKey;
                    if (fullElementType.contains(":")) {
                        // 将elementType按冒号分割为命名空间和属性名，转换为点号格式
                        String[] parts = fullElementType.split(":");
                        translationKey = "attribute.name." + parts[0] + "." + parts[1];
                    } else {
                        // 对于没有命名空间的属性，直接使用
                        translationKey = "attribute.name." + fullElementType;
                    }
                    MutableComponent displayName = Component.translatable(translationKey);
                    
                    MutableComponent component = Component.literal("  ")
                            .append(displayName.withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(": "))
                            .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
                    tooltipElements.add(Either.left(component));
                }
            }
        }
    }
    
    /**
     * 查找匹配的元素类型（支持完整匹配或后缀匹配）
     * @param allElements 所有元素数据
     * @param attribute 属性名称
     * @return 匹配的完整元素类型，如果没有匹配则返回null
     */
    private static String findMatchingElementType(Map<String, Double> allElements, String attribute) {
        for (String elementType : allElements.keySet()) {
            if (elementType.equals(attribute) || elementType.endsWith(":" + attribute)) {
                return elementType;
            }
        }
        return null;
    }
    
    /**
     * 显示派系元素
     */
    private static void addFactionAttributesToTooltip(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        Map<String, Double> factionElements = ElementNBTUtils.readFactionElements(stack);
        
        // 如果没有派系元素，直接返回
        if (factionElements.isEmpty()) {
            return;
        }
        
        // 派系元素类型顺序
        String[] factionTypes = {"grineer", "infested", "corpus", "orokin", "sentient", "murmum"};
        for (String type : factionTypes) {
            if (factionElements.containsKey(type)) {
                double value = factionElements.get(type);
                if (value <= 0) {
                    continue;
                }
                
                // 查找对应的ElementType以获取颜色和名称
                ElementType elementType = ElementType.byName(type);
                if (elementType != null) {
                    // 格式化并添加到工具提示
                    String formattedValue = String.format("%.1f%%", value * 100);
                    MutableComponent component = Component.literal("  ")
                            .append(elementType.getColoredName())
                            .append(Component.literal(": "))
                            .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
                    tooltipElements.add(Either.left(component));
                }
            }
        }
    }
}