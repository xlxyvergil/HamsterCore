package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementAttribute;
import net.minecraftforge.registries.RegistryObject;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class HeatElementEnchantment extends ElementEnchantment {
    public HeatElementEnchantment() {
        super(Rarity.UNCOMMON, ElementType.HEAT, 5, "enchantment_heat");
    }
    
    @Override
    public java.util.Collection<AttributeModifier> getEntityAttributes(ItemStack stack, EquipmentSlot slot, int level) {
        if (slot == EquipmentSlot.MAINHAND && this.elementType != null) {
            // 获取元素属性
            RegistryObject<ElementAttribute> attributeRegistry = ElementRegistry.getAttribute(this.elementType);
            if (attributeRegistry != null && attributeRegistry.isPresent()) {
                // 计算基于等级的数值：每级0.3
                double value = 0.3 * level;
                // 使用ElementUUIDManager生成UUID
                UUID modifierId = ElementUUIDManager.getOrCreateUUID(stack, this.elementType, level);
                // 确保元素类型名称不为null
                String elementName = this.elementType.getName() != null ? this.elementType.getName() : "unknown";
                AttributeModifier modifier = new AttributeModifier(
                    modifierId, 
                    "hamstercore:" + elementName, 
                    value, 
                    AttributeModifier.Operation.ADDITION
                );
                
                // 返回包含修饰符的集合
                java.util.Collection<AttributeModifier> modifiers = new java.util.ArrayList<>();
                modifiers.add(modifier);
                return modifiers;
            }
        }
        
        return java.util.Collections.emptyList();
    }
}
