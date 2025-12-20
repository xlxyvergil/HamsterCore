package com.xlxyvergil.hamstercore.client.renderer.item;

import java.util.List;
import java.util.Map;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.handler.AffixCacheManager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 武器属性渲染器
 * 负责在物品栏界面显示武器的各种属性
 */
@OnlyIn(Dist.CLIENT)
public class WeaponAttributeRenderer {
    
    public static void registerEvents() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new WeaponAttributeRenderer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        
        // 获取缓存数据
        AffixCacheManager.AffixCacheData cacheData = AffixCacheManager.getOrCreateCache(stack);
        
        // 检查缓存中是否有任何元素数据
        if (cacheData.getPhysicalElements().isEmpty() && cacheData.getCriticalStats().isEmpty() && cacheData.getFactionElements().isEmpty() && cacheData.getCombinedElements().isEmpty()) {
            return;
        }
        
        // 获取现有的工具提示行
        List<Component> tooltipElements = event.getToolTip();
        
        
        // 添加"Weapon Attributes"标题
        tooltipElements.add(Component.translatable("hamstercore.ui.weapon_attributes").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
        
        // 显示物理元素
        addPhysicalElements(stack, tooltipElements, cacheData);
        
        // 显示基础元素和复合元素
        addBasicAndComplexElements(stack, tooltipElements, cacheData);
        
        // 显示特殊元素属性（暴击率、暴击伤害、触发率等）
        addSpecialAttributes(stack, tooltipElements, cacheData);
        
        // 显示派系元素
        addFactionAttributes(stack, tooltipElements, cacheData);
    }
    
    /**
     * 显示物理元素
     */
    private void addPhysicalElements(ItemStack stack, List<Component> tooltipElements, AffixCacheManager.AffixCacheData cacheData) {
        Map<String, Double> physicalElements = cacheData.getPhysicalElements();
        if (physicalElements.isEmpty()) {
            return;
        }
        
        // 遍历所有物理元素类型
        for (ElementType elementType : ElementType.getPhysicalElements()) {
            // 获取元素值
            Double value = physicalElements.get(elementType.getName());
            if (value == null || value <= 0) {
                continue;
            }
            
            // 格式化并添加到工具提示
            String formattedValue = String.format("%.1f", value);
            MutableComponent component = Component.literal("  ")
                    .append(elementType.getColoredName())
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(component);
        }
    }
    
    /**
     * 显示基础元素和复合元素
     */
    private void addBasicAndComplexElements(ItemStack stack, List<Component> tooltipElements, AffixCacheManager.AffixCacheData cacheData) {
        Map<String, Double> combinedElements = cacheData.getCombinedElements();
        if (combinedElements.isEmpty()) {
            return;
        }
        
        // 遍历所有基础元素类型
        for (ElementType elementType : ElementType.getBasicElements()) {
            // 获取元素值
            Double value = combinedElements.get(elementType.getName());
            if (value == null || value <= 0) {
                continue;
            }
            
            // 格式化并添加到工具提示
            String formattedValue = String.format("%.1f", value);
            MutableComponent component = Component.literal("  ")
                    .append(elementType.getColoredName())
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(component);
        }
        
        // 遍历所有复合元素类型
        for (ElementType elementType : ElementType.getComplexElements()) {
            // 获取元素值
            Double value = combinedElements.get(elementType.getName());
            if (value == null || value <= 0) {
                continue;
            }
            
            // 格式化并添加到工具提示
            String formattedValue = String.format("%.1f", value);
            MutableComponent component = Component.literal("  ")
                    .append(elementType.getColoredName())
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(component);
        }
    }
    
    /**
     * 显示特殊元素属性（暴击率、暴击伤害、触发率等）
     */
    private void addSpecialAttributes(ItemStack stack, List<Component> tooltipElements, AffixCacheManager.AffixCacheData cacheData) {
        Map<String, Double> criticalStats = cacheData.getCriticalStats();
        if (criticalStats.isEmpty()) {
            return;
        }
        
        // 处理暴击率
        Double critChance = criticalStats.get("critical_chance");
        if (critChance != null && critChance > 0) {
            String formattedValue = String.format("%.1f%%", critChance * 100);
            MutableComponent component = Component.literal("  ")
                    .append(Component.translatable("element.critical_chance.name").withStyle(ChatFormatting.RED))
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(component);
        }
        
        // 处理暴击伤害
        Double critDamage = criticalStats.get("critical_damage");
        if (critDamage != null && critDamage > 0) {
            String formattedValue = String.format("%.1f%%", critDamage * 100);
            MutableComponent component = Component.literal("  ")
                    .append(Component.translatable("element.critical_damage.name").withStyle(ChatFormatting.RED))
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(component);
        }
        
        // 处理触发率
        Double triggerRate = criticalStats.get("trigger_chance");
        if (triggerRate != null && triggerRate > 0) {
            String formattedValue = String.format("%.1f%%", triggerRate * 100);
            MutableComponent component = Component.literal("  ")
                    .append(Component.translatable("element.trigger_chance.name").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(component);
        }
    }
    
    /**
     * 显示派系元素
     */
    private void addFactionAttributes(ItemStack stack, List<Component> tooltipElements, AffixCacheManager.AffixCacheData cacheData) {
        Map<String, Double> factionElements = cacheData.getFactionElements();
        if (factionElements.isEmpty()) {
            return;
        }
        
        // 遍历所有元素类型
        for (ElementType elementType : ElementType.getAllTypes()) {
            // 只处理派系元素
            if (elementType.isSpecial()) {
                // 获取元素值
                Double value = factionElements.get(elementType.getName());
                if (value == null || value <= 0) {
                    continue;
                }
                
                // 格式化并添加到工具提示
                String formattedValue = String.format("%.1f%%", value * 100);
                MutableComponent component = Component.literal("  ")
                        .append(elementType.getColoredName())
                        .append(Component.literal(": "))
                        .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
                tooltipElements.add(component);
            }
        }
    }
}