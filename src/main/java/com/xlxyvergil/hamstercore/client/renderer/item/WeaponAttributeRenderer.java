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
import net.minecraft.network.chat.MutableComponent;
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
        
        // 添加"Weapon Attributes"标题
        tooltipElements.add(Component.literal("Weapon Attributes").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
        
        // 从usage层显示计算后的基础元素和复合元素数据
        addUsageAttributes(stack, tooltipElements);
        
        // 从属性修饰符中显示计算后的特殊元素、派系元素和物理元素
        addAttributeModifierAttributes(stack, tooltipElements);
    }
    
    /**
     * 从usage层显示计算后的基础元素和复合元素数据
     * 无需额外判断或属性修饰符查找，有数据就显示
     */
    private void addUsageAttributes(ItemStack stack, List<Component> tooltipElements) {
        // 获取武器数据
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        if (weaponData == null) {
            return;
        }
        
        // 获取usage层元素
        Map<String, Double> usageElements = weaponData.getUsageElements();
        if (usageElements.isEmpty()) {
            return;
        }
        
        // 遍历所有元素类型
        for (ElementType elementType : ElementType.getAllTypes()) {
            // 只处理基础元素和复合元素
            if (!elementType.isBasic() && !elementType.isComplex()) {
                continue;
            }
            
            // 获取元素值
            Double value = usageElements.get(elementType.getName());
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
     * 从属性修饰符中显示计算后的特殊元素、派系元素和物理元素
     * 无需额外判断，有数据就显示
     */
    private void addAttributeModifierAttributes(ItemStack stack, List<Component> tooltipElements) {
        // 遍历所有元素类型
        for (ElementType elementType : ElementType.getAllTypes()) {
            // 只处理特殊元素、派系元素和物理元素
            if (!elementType.isSpecial() && !elementType.isPhysical()) {
                continue;
            }
            
            // 从属性修饰符中获取元素值
            double value = ElementHelper.getElementValueFromItem(stack, elementType);
            if (value <= 0) {
                continue;
            }
            
            // 格式化并添加到工具提示
            String formattedValue;
            if (elementType.isSpecial() && (elementType == ElementType.CRITICAL_CHANCE || 
                                           elementType == ElementType.CRITICAL_DAMAGE || 
                                           elementType == ElementType.TRIGGER_CHANCE)) {
                // 特殊属性显示为百分比
                formattedValue = String.format("%.1f%%", value * 100);
            } else {
                // 其他属性显示为普通数值
                formattedValue = String.format("%.1f", value);
            }
            
            MutableComponent component = Component.literal("  ")
                    .append(elementType.getColoredName())
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue).withStyle(ChatFormatting.WHITE));
            tooltipElements.add(component);
        }
    }
}