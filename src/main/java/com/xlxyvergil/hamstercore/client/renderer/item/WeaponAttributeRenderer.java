package com.xlxyvergil.hamstercore.client.renderer.item;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.element.ElementHelper;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

@OnlyIn(Dist.CLIENT)
public class WeaponAttributeRenderer {
    
    public static void registerEvents() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new WeaponAttributeRenderer());
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        
        // 检查物品是否可以应用元素属性
        if (!canApplyElementAttributes(stack)) {
            return;
        }
        
        // 获取或创建武器配置
        WeaponConfig config = WeaponConfig.createWeaponConfig(stack);
        double baseDamage = WeaponConfig.getBaseAttackDamage(stack);
        
        // 获取现有的工具提示行
        List<Component> tooltipElements = event.getToolTip();
        
        // 添加基础属性到工具提示
        addBasicAttributesToTooltip(tooltipElements, config);
        
        // 添加元素配比到工具提示
        addElementRatiosToTooltip(tooltipElements, config, baseDamage);
    }
    
    /**
     * 棜查物品是否可以应用元素属性
     */
    private static boolean canApplyElementAttributes(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 使用ElementHelper中的方法检查
        return ElementHelper.canApplyElementAttributes(stack);
    }
    
    /**
     * 添加基础属性到工具提示
     */
    private static void addBasicAttributesToTooltip(List<Component> tooltipElements, WeaponConfig config) {
        // 添加空行分隔
        tooltipElements.add(Component.literal(""));
        
        // 添加暴击率
        String criticalChanceText = String.format("%s: %.1f%%", 
            Component.translatable("hamstercore.ui.critical_chance").getString(), 
            config.getCriticalChance() * 100);
        tooltipElements.add(Component.literal(criticalChanceText));
        
        // 添加暴击伤害
        String criticalDamageText = String.format("%s: %.0f%%", 
            Component.translatable("hamstercore.ui.critical_damage").getString(), 
            config.getCriticalDamage() * 100);
        tooltipElements.add(Component.literal(criticalDamageText));
        
        // 添加触发率
        String triggerChanceText = String.format("%s: %.1f%%", 
            Component.translatable("hamstercore.ui.trigger_chance").getString(), 
            config.getTriggerChance() * 100);
        tooltipElements.add(Component.literal(triggerChanceText));
    }
    
    /**
     * 添加元素倍率到工具提示
     */
    private static void addElementRatiosToTooltip(List<Component> tooltipElements, WeaponConfig config, double baseDamage) {
        Map<String, Double> elementRatios = config.getElementRatios();
        
        if (elementRatios.isEmpty()) {
            return;
        }
        
        // 添加空行分隔
        tooltipElements.add(Component.literal(""));
        
        // 添加元素倍率标题
        tooltipElements.add(
            Component.translatable("hamstercore.ui.element_ratios").append(":")
        );
        
        // 添加每个元素的倍率
        for (Map.Entry<String, Double> entry : elementRatios.entrySet()) {
            String elementName = entry.getKey();
            double ratio = entry.getValue();
            
            // 获取元素类型
            ElementType elementType = ElementType.byName(elementName);
            if (elementType == null) {
                continue;
            }
            
            // 创建元素名称和倍率的文本组件，使用元素颜色
            String elementText = String.format("  %s: %.0f%%", 
                Component.translatable("element." + elementName + ".name").getString(), 
                ratio * 100);
            
            Component elementComponent = Component.literal(elementText)
                .withStyle(style -> style.withColor(elementType.getColor().getColor()));
            
            tooltipElements.add(elementComponent);
        }
    }
}