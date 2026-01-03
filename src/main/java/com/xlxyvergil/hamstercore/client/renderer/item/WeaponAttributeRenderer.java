package com.xlxyvergil.hamstercore.client.renderer.item;

import java.util.List;

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
        
        // 显示特殊元素属性（暴击率、暴击伤害、触发率等）
        addSpecialAttributesToTooltip(stack, tooltipElements);
        
        // 显示派系元素
        addFactionAttributesToTooltip(stack, tooltipElements);
    }
    
    /**
     * 显示物理元素
     */
    private static void addPhysicalElementsToTooltip(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        // 使用ElementNBTUtils检查是否有物理元素
        if (ElementNBTUtils.readPhysicalElements(stack).isEmpty()) {
            return;
        }
        
        // 遍历所有物理元素类型
        for (ElementType elementType : ElementType.getPhysicalElements()) {
            // 获取元素值
            double value = ElementNBTUtils.readPhysicalElementValue(stack, elementType.getName());
            if (value <= 0) {
                continue;
            }
            
            // 格式化并添加到工具提示
            String formattedValue = String.format("%.1f", value);
            MutableComponent component = Component.literal("  ")
                    .append(elementType.getColoredName())
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(Either.left(component));
        }
    }
    
    /**
     * 显示基础元素和复合元素
     */
    private static void addBasicAndComplexElementsToTooltip(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        // 使用ElementNBTUtils检查是否有基础或复合元素
        if (ElementNBTUtils.readCombinedElements(stack).isEmpty()) {
            return;
        }
        
        // 遍历所有基础元素类型
        for (ElementType elementType : ElementType.getBasicElements()) {
            // 获取元素值
            double value = ElementNBTUtils.readBasicElementValue(stack, elementType.getName());
            if (value <= 0) {
                continue;
            }
            
            // 格式化并添加到工具提示
            String formattedValue = String.format("%.1f", value);
            MutableComponent component = Component.literal("  ")
                    .append(elementType.getColoredName())
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(Either.left(component));
        }
        
        // 遍历所有复合元素类型
        for (ElementType elementType : ElementType.getComplexElements()) {
            // 获取元素值
            double value = ElementNBTUtils.readCombinedElementValue(stack, elementType.getName());
            if (value <= 0) {
                continue;
            }
            
            // 格式化并添加到工具提示
            String formattedValue = String.format("%.1f", value);
            MutableComponent component = Component.literal("  ")
                    .append(elementType.getColoredName())
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(Either.left(component));
        }
    }
    
    /**
     * 显示特殊元素属性（暴击率、暴击伤害、触发率等）
     */
    private static void addSpecialAttributesToTooltip(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        // 检查是否有特殊元素属性
        double critChance = ElementNBTUtils.readCriticalChance(stack);
        if (critChance > 0) {
            String formattedValue = String.format("%.1f%%", critChance * 100);
            MutableComponent component = Component.literal("  ")
                    .append(Component.translatable("element.critical_chance.name").withStyle(ChatFormatting.RED))
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(Either.left(component));
        }
        
        // 处理暴击伤害
        double critDamage = ElementNBTUtils.readCriticalDamage(stack);
        if (critDamage > 0) {
            String formattedValue = String.format("%.1f%%", critDamage * 100);
            MutableComponent component = Component.literal("  ")
                    .append(Component.translatable("element.critical_damage.name").withStyle(ChatFormatting.RED))
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(Either.left(component));
        }
        
        // 处理触发率
        double triggerRate = ElementNBTUtils.readTriggerChance(stack);
        if (triggerRate > 0) {
            String formattedValue = String.format("%.1f%%", triggerRate * 100);
            MutableComponent component = Component.literal("  ")
                    .append(Component.translatable("element.trigger_chance.name").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(Either.left(component));
        }
    }
    
    /**
     * 显示派系元素
     */
    private static void addFactionAttributesToTooltip(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltipElements) {
        // 使用ElementNBTUtils检查是否有派系元素
        if (ElementNBTUtils.readFactionElements(stack).isEmpty()) {
            return;
        }
        
        // 遍历所有派系元素类型
        for (ElementType elementType : ElementType.getSpecialElements()) {
            // 获取元素值
            double value = ElementNBTUtils.readFactionElementValue(stack, elementType.getName());
            if (value <= 0) {
                continue;
            }
            
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