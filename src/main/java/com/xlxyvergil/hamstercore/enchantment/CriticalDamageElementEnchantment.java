package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class CriticalDamageElementEnchantment extends ElementEnchantment {
    public CriticalDamageElementEnchantment() {
        super(Rarity.RARE, ElementType.CRITICAL_DAMAGE, 5, "enchantment_critical_damage");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.05 per level
        return 0.05 * level;
    }
}
