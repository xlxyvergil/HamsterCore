package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class SentientElementEnchantment extends ElementEnchantment {
    public SentientElementEnchantment() {
        super(Rarity.VERY_RARE, ElementType.SENTIENT, 5, "enchantment_sentient");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.05 per level
        return 0.05 * level;
    }
}
