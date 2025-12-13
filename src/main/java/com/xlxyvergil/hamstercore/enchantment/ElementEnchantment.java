package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementAttribute;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;

public class ElementEnchantment extends Enchantment {
    private final ElementType elementType;
    private final int maxLevel;
    private final String enchantmentId;
    private static final UUID ELEMENT_MODIFIER_ID = UUID.fromString("CB3F5740-CA39-4346-9A85-4F558D338D8B");

    public ElementEnchantment(Rarity rarity, ElementType elementType, int maxLevel, String enchantmentId) {
        super(rarity, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        this.elementType = elementType;
        this.maxLevel = maxLevel;
        this.enchantmentId = enchantmentId;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    public ElementType getElementType() {
        return elementType;
    }
    
    public String getEnchantmentId() {
        return enchantmentId;
    }
    
    @Override
    public boolean canEnchant(ItemStack stack) {
        // 确保可以应用于武器类型物品
        return this.category.canEnchant(stack.getItem());
    }
    
    @Override
    public boolean isDiscoverable() {
        // 确保可以在附魔台发现
        return true;
    }
    
    @Override
    public boolean isTradeable() {
        // 确保可以在交易中获得
        return true;
    }
    
    @Override
    public boolean isTreasureOnly() {
        // 不设置为宝藏附魔，以便更容易获得
        return false;
    }
    
    @Override
    public java.util.Collection<AttributeModifier> getEntityAttributes(EquipmentSlot slot, int level) {
        if (slot == EquipmentSlot.MAINHAND) {
            // 获取元素属性
            ElementAttribute elementAttribute = ElementRegistry.getAttribute(this.elementType);
            if (elementAttribute != null) {
                // 创建属性修饰符
                double value = elementAttribute.getDefaultValue() * level;
                AttributeModifier modifier = new AttributeModifier(
                    ELEMENT_MODIFIER_ID, 
                    "hamstercore:" + elementType.getName(), 
                    value, 
                    elementAttribute.getOperation()
                );
                
                // 返回包含修饰符的集合
                java.util.Collection<AttributeModifier> modifiers = new java.util.ArrayList<>();
                modifiers.add(modifier);
                return modifiers;
            }
        }
        
        return super.getEntityAttributes(slot, level);
    }
}