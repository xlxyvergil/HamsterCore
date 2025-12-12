package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ElementEnchantment extends Enchantment {
    private final ElementType elementType;
    private final int maxLevel;
    private final String enchantmentId;

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
}