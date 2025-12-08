package com.xlxyvergil.hamstercore.client.renderer.item;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementNBTUtils;
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
        
        // 检查物品是否具有元素数据
        if (!ElementNBTUtils.hasAnyElements(stack)) {
            return;
        }
        
        // 获取现有的工具提示行
        List<Component> tooltipElements = event.getToolTip();
        
        // 只使用ElementNBTUtils获取元素数据
        // 添加基础属性到工具提示
        addBasicAttributesFromNBT(tooltipElements, stack);
        
        // 添加元素属性到工具提示
        addElementAttributesFromNBT(tooltipElements, stack);
    }
    

    
    /**
     * 添加基础属性到工具提示
     */
    private static void addBasicAttributesFromNBT(List<Component> tooltipElements, ItemStack stack) {
        // 添加空行分隔
        tooltipElements.add(Component.literal(""));
        
        // 添加暴击率
        double criticalChance = ElementNBTUtils.getCriticalChance(stack);
        String criticalChanceText = String.format("%s: %.1f%%", 
            Component.translatable("hamstercore.ui.critical_chance").getString(), 
            criticalChance * 100);
        tooltipElements.add(Component.literal(criticalChanceText));
        
        // 添加暴击伤害
        double criticalDamage = ElementNBTUtils.getCriticalDamage(stack);
        String criticalDamageText = String.format("%s: %.1f", 
            Component.translatable("hamstercore.ui.critical_damage").getString(), 
            criticalDamage);
        tooltipElements.add(Component.literal(criticalDamageText));
        
        // 添加触发率
        double triggerChance = ElementNBTUtils.getTriggerChance(stack);
        String triggerChanceText = String.format("%s: %.1f%%", 
            Component.translatable("hamstercore.ui.trigger_chance").getString(), 
            triggerChance * 100);
        tooltipElements.add(Component.literal(triggerChanceText));
    }
    
    /**
     * 添加元素属性到工具提示
     */
    private static void addElementAttributesFromNBT(List<Component> tooltipElements, ItemStack stack) {
        // 直接从NBT获取元素类型
        java.util.Set<ElementType> elementTypes = ElementNBTUtils.getAllElementTypes(stack);
        
        if (elementTypes.isEmpty()) {
            return;
        }
        
        // 添加空行分隔
        tooltipElements.add(Component.literal(""));
        
        // 添加元素属性标题
        tooltipElements.add(
            Component.translatable("hamstercore.ui.element_ratios").append(":")
        );
        
        // 添加每个元素的属性值（不以百分比形式展示）
        for (ElementType elementType : elementTypes) {
            // 获取元素值
            double elementValue = ElementNBTUtils.getElementValue(stack, elementType);
            
            // 只显示物理元素、基础元素和复合元素，不显示特殊属性
            if (elementType.isPhysical() || elementType.isBasic() || elementType.isComplex()) {
                // 创建元素名称和数值的文本组件，使用元素颜色
                String elementText = String.format("  %s: %.2f", 
                    Component.translatable("element." + elementType.getName() + ".name").getString(), 
                    elementValue);
                
                Component elementComponent = Component.literal(elementText)
                    .withStyle(style -> style.withColor(elementType.getColor().getColor()));
                
                tooltipElements.add(elementComponent);
            }
        }
    }
}