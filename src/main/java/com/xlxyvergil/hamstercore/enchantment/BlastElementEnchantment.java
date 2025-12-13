package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class BlastElementEnchantment extends ElementEnchantment {
    public BlastElementEnchantment() {
        super(Rarity.RARE, ElementType.BLAST, 5, "enchantment_blast");
    }
    
    @Override
    public Collection<AttributeModifier> getEntityAttributes(ItemStack stack, EquipmentSlot slot, int level) {
        if (slot == EquipmentSlot.MAINHAND) {
            // 获取元素属性
            ElementAttribute elementAttribute = ElementRegistry.getAttribute(this.elementType);
            if (elementAttribute != null) {
                // 计算基于等级的数值：每级0.3
                double value = 0.3 * level;
                // 使用ElementUUIDManager生成UUID
                UUID modifierId = ElementUUIDManager.getOrCreateUUID(stack, this.elementType, level);
                AttributeModifier modifier = new AttributeModifier(
                    modifierId, 
                    "hamstercore:" + elementType.getName(), 
                    value, 
                    elementAttribute.getOperation()
                );
                
                // 返回包含修饰符的集合
                Collection<AttributeModifier> modifiers = new ArrayList<>();
                modifiers.add(modifier);
                return modifiers;
            }
        }
        
        return Collections.emptyList();
    }
}