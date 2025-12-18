package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class CriticalChanceElementEnchantment extends ElementEnchantment {
    public CriticalChanceElementEnchantment() {
        super(Rarity.RARE, ElementType.CRITICAL_CHANCE, 5, "enchantment_critical_chance");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.05 per level
        return 0.05 * level;
    }
}
