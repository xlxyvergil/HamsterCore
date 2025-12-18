package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class MurmurElementEnchantment extends ElementEnchantment {
    public MurmurElementEnchantment() {
        super(Rarity.VERY_RARE, ElementType.MURMUR, 5, "enchantment_murmur");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.05 per level
        return 0.05 * level;
    }
}
