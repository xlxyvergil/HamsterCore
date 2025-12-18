package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class GrineerElementEnchantment extends ElementEnchantment {
    public GrineerElementEnchantment() {
        super(Rarity.VERY_RARE, ElementType.GRINEER, 5, "enchantment_grineer");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.05 per level
        return 0.05 * level;
    }
}
