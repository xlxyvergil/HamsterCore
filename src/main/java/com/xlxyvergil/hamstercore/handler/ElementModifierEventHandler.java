package com.xlxyvergil.hamstercore.handler;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public static void applyElementModifiers(ItemAttributeModifierEvent event, Map<String, List<WeaponData.BasicEntry>> basicElements) {
        // 遍历所有基础元素并应用对应的属性修饰符
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : basicElements.entrySet()) {
            String elementTypeName = entry.getKey();
            List<WeaponData.BasicEntry> elements = entry.getValue();
            
            // 根据元素类型名称获取元素类型
            ElementType elementType = ElementType.byName(elementTypeName);
            if (elementType == null) {
                continue; // 未知元素类型，跳过
            }
            
            // 获取注册的元素属性
            ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
            if (elementAttribute == null) {
                continue; // 未注册的元素属性，跳过
            }
            
            // 获取物品的武器配置数据以获取元素值
            ItemStack stack = event.getItemStack();
            WeaponData weaponData = WeaponConfig.getWeaponConfig(stack);
            double elementValue = 1.0; // 默认值
            
            // 如果有武器配置且包含该元素的使用层数据，则使用配置的值
            if (weaponData != null && weaponData.getUsageElements().containsKey(elementTypeName)) {
                elementValue = weaponData.getUsageElements().get(elementTypeName);
            }
            
            // 为每个元素实例创建修饰符
            for (int i = 0; i < elements.size(); i++) {
                // 使用ElementUUIDManager为每个元素实例生成唯一的UUID
                UUID modifierId = ElementUUIDManager.getOrCreateUUID(stack, elementType, i);
                AttributeModifier modifier = new AttributeModifier(modifierId, "hamstercore:" + elementType.getName(), elementValue, elementAttribute.getOperation());
                
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
                applyElementModifiers(event, weaponData.getBasicElements());
            }
        }
    }
}