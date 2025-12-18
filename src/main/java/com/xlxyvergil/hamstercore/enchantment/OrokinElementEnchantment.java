package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class OrokinElementEnchantment extends ElementEnchantment {
    public OrokinElementEnchantment() {
        super(Rarity.VERY_RARE, ElementType.OROKIN, 5, "enchantment_orokin");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.05 per level
        return 0.05 * level;
    }
}
