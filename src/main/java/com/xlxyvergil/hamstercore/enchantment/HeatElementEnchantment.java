package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class HeatElementEnchantment extends ElementEnchantment {
    public HeatElementEnchantment() {
        super(Rarity.UNCOMMON, ElementType.HEAT, 5, "enchantment_heat");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.1 per level
        return 0.1 * level;
    }
}
