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
        
        // 显示其他数据
        addOtherAttributesToTooltip(stack, tooltipElements);
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
        String[] criticalAttributes = {"crit_chance", "crit_damage", "trigger_chance"};
        for (String attribute : criticalAttributes) {
            // 查找匹配的元素类型（支持完整匹配或后缀匹配）
            for (Map.Entry<String, Double> entry : allElements.entrySet()) {
                String elementType = entry.getKey();
                double value = entry.getValue();
                
                if ((elementType.equals(attribute) || elementType.endsWith(":" + attribute)) && value > 0) {
                    // 格式化数值
                    String formattedValue = String.format("%.1f%%", value * 100);
                    
                    // 使用包含命名空间的elementType获取显示名称
                    MutableComponent component = Component.literal("  ")
                            .append(Component.translatable("attribute.name." + attribute).withStyle(ChatFormatting.RED))
                            .append(Component.literal(": "))
                            .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
                    tooltipElements.add(Either.left(component));
                    break;
                }
            }
        }
        
        // 处理攻击速度和攻击范围
        String[] speedRangeAttributes = {"generic.attack_speed", "player.block_interaction_range"};
        for (String attribute : speedRangeAttributes) {
            // 查找匹配的元素类型（支持完整匹配或后缀匹配）
            for (Map.Entry<String, Double> entry : allElements.entrySet()) {
                String elementType = entry.getKey();
                double value = entry.getValue();
                
                if ((elementType.equals(attribute) || elementType.endsWith(":" + attribute)) && value != 0) {
                    // 格式化数值
                    String formattedValue;
                    if (attribute.equals("player.block_interaction_range")) {
                        formattedValue = String.format("%.2f m", value);
                    } else {
                        formattedValue = String.format("%.2f", value);
                    }
                    
                    // 使用包含命名空间的elementType获取显示名称
                    MutableComponent component = Component.literal("  ")
                            .append(Component.translatable("attribute.name." + attribute).withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(": "))
                            .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
                    tooltipElements.add(Either.left(component));
                    break;
                }
            }
        }
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
    
    /**
     * 显示其他数据（除了已经显示的属性之外的数据）
     */
    private static void addOtherAttributesToTooltip(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        // 获取所有元素数据
        Map<String, Double> allElements = ElementNBTUtils.readAllElementValues(stack);
        if (allElements.isEmpty()) {
            return;
        }
        
        // 已经显示过的属性集合（使用后缀形式，如"crit_chance"）
        Set<String> displayedAttributes = new HashSet<>();
        
        // 添加物理元素到已显示集合
        String[] physicalTypes = {"impact", "puncture", "slash"};
        for (String type : physicalTypes) {
            displayedAttributes.add(type);
        }
        
        // 添加基础元素到已显示集合
        String[] basicTypes = {"cold", "electricity", "heat", "toxin"};
        for (String type : basicTypes) {
            displayedAttributes.add(type);
        }
        
        // 添加复合元素到已显示集合
        String[] complexTypes = {"blast", "corrosive", "gas", "magnetic", "radiation", "viral"};
        for (String type : complexTypes) {
            displayedAttributes.add(type);
        }
        
        // 添加派系元素到已显示集合
        String[] factionTypes = {"grineer", "infested", "corpus", "orokin", "sentient", "murmum"};
        for (String type : factionTypes) {
            displayedAttributes.add(type);
        }
        
        // 添加特殊属性到已显示集合
        String[] specialTypes = {"crit_chance", "crit_damage", "trigger_chance", "generic.attack_speed", "player.block_interaction_range"};
        for (String type : specialTypes) {
            displayedAttributes.add(type);
        }
        
        // 过滤出未显示的属性
        boolean hasOtherAttributes = false;
        Map<String, Double> otherAttributes = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : allElements.entrySet()) {
            String elementType = entry.getKey();
            double value = entry.getValue();
            
            // 跳过零值
            if (value == 0) {
                continue;
            }
            
            // 获取元素类型的后缀（不含命名空间）
            String attributeSuffix = elementType.substring(elementType.lastIndexOf(":") + 1);
            
            // 检查是否是已显示属性
            boolean isDisplayed = displayedAttributes.contains(attributeSuffix);
            
            if (!isDisplayed) {
                hasOtherAttributes = true;
                otherAttributes.put(elementType, value);
            }
        }
        
        // 如果没有其他属性，直接返回
        if (!hasOtherAttributes) {
            return;
        }
        
        // 添加其他属性标题
        tooltipElements.add(Either.left(Component.translatable("hamstercore.ui.other_attributes").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY)));
        
        // 显示其他属性
        for (Map.Entry<String, Double> entry : otherAttributes.entrySet()) {
            String elementType = entry.getKey();
            double value = entry.getValue();
            
            // 格式化数值
            String formattedValue;
            if (elementType.contains("chance") || elementType.contains("rate")) {
                // 对于百分比类型的属性，乘以100并添加百分号
                formattedValue = String.format("%.1f%%", value * 100);
            } else {
                // 其他数值类型，保留两位小数
                formattedValue = String.format("%.2f", value);
            }
            
            // 尝试使用翻译键获取显示名称
            MutableComponent displayName;
            try {
                // 首先尝试完整的elementType作为翻译键
                displayName = Component.translatable("attribute.name." + elementType);
                // 如果翻译失败，尝试使用属性名的最后一部分
                if (displayName.getString().equals("attribute.name." + elementType)) {
                    // 生成显示名称（使用elementType的最后一部分）
                    String simpleName = elementType.substring(elementType.lastIndexOf(":") + 1);
                    simpleName = simpleName.replace('.', ' ');
                    simpleName = java.util.regex.Pattern.compile("(?<!^)(?=[A-Z])").matcher(simpleName).replaceAll(" ");
                    simpleName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
                    displayName = Component.literal(simpleName);
                }
            } catch (Exception e) {
                // 如果翻译失败，生成默认显示名称
                String simpleName = elementType.substring(elementType.lastIndexOf(":") + 1);
                simpleName = simpleName.replace('.', ' ');
                simpleName = java.util.regex.Pattern.compile("(?<!^)(?=[A-Z])").matcher(simpleName).replaceAll(" ");
                simpleName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
                displayName = Component.literal(simpleName);
            }
            
            // 添加到工具提示
            MutableComponent component = Component.literal("  ")
                    .append(displayName.withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(Either.left(component));
        }
    }
}