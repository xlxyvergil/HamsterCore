package com.xlxyvergil.hamstercore.handler;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import com.xlxyvergil.hamstercore.enchantment.ElementEnchantment;
import com.xlxyvergil.hamstercore.enchantment.ModEnchantments;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 元素修饰符事件处理器
 * 处理ItemAttributeModifierEvent事件，负责将配置的元素属性应用到物品上
 */
@Mod.EventBusSubscriber(modid = "hamstercore")
public class ElementModifierEventHandler {
    
    /**
     * 应用元素修饰符到物品
     * 该方法从武器配置中获取真实的元素值，而不是使用默认值
     * 并使用ElementUUIDManager为每个元素实例生成唯一的UUID
     */
    public static void applyElementModifiers(ItemAttributeModifierEvent event, Map<String, List<WeaponData.BasicEntry>> basicElements, Map<String, Double> usageElements) {
        // 获取所有元素附魔类型，避免与附魔系统冲突
        Set<ElementType> enchantedElementTypes = event.getItemStack().getAllEnchantments().keySet().stream()
            .filter(enchantment -> enchantment instanceof ElementEnchantment)
            .map(enchantment -> ((ElementEnchantment) enchantment).getElementType())
            .collect(Collectors.toSet());

        // 遍历所有基础元素并应用对应的属性修饰符
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : basicElements.entrySet()) {
            String elementTypeName = entry.getKey();
            List<WeaponData.BasicEntry> elements = entry.getValue();
            
            // 根据元素类型名称获取元素类型
            ElementType elementType = ElementType.byName(elementTypeName);
            if (elementType == null) {
                continue; // 未知元素类型，跳过
            }
            
            // 如果该元素类型已经有附魔，则跳过，避免重复添加
            if (enchantedElementTypes.contains(elementType)) {
                continue;
            }
            
            // 获取注册的元素属性
            ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
            if (elementAttribute == null) {
                continue; // 未注册的元素属性，跳过
            }
            
            // 获取物品的武器配置数据以获取元素值
            ItemStack stack = event.getItemStack();
            double elementValue = 0.0; // 默认值
            
            // 如果有使用层数据，则使用计算后的值
            if (usageElements != null && usageElements.containsKey(elementTypeName)) {
                elementValue = usageElements.get(elementTypeName);
            }
            
            // 确保元素值不小于0
            elementValue = Math.max(0, elementValue);
            
            // 为每个元素实例创建修饰符
            for (int i = 0; i < elements.size(); i++) {
                // 使用ElementUUIDManager为每个元素实例生成唯一的UUID
                UUID modifierId = ElementUUIDManager.getOrCreateUUID(stack, elementType, i);
                AttributeModifier modifier = new AttributeModifier(modifierId, "hamstercore:" + elementType.getName(), elementValue, AttributeModifier.Operation.ADDITION);
                
                // 应用修饰符到物品的攻击伤害属性上
                event.addModifier(Attributes.ATTACK_DAMAGE, modifier);
            }
        }
    }

    /**
     * 监听物品属性修饰符事件
     * 当物品属性被查询时，应用元素修饰符
     */
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        
        // 检查物品是否有元素数据
        if (stack.hasTag() && stack.getTag().contains("element_data")) {
            // 从NBT加载武器数据
            WeaponData weaponData = WeaponDataManager.loadElementData(stack);
            if (weaponData != null && weaponData.getBasicElements() != null) {
                // 应用元素修饰符
                applyElementModifiers(event, weaponData.getBasicElements(), weaponData.getUsageElements());
            }
        }
    }
}