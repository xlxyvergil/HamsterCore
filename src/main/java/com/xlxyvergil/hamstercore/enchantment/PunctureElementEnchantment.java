package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class PunctureElementEnchantment extends ElementEnchantment {
    public PunctureElementEnchantment() {
        super(Rarity.COMMON, ElementType.PUNCTURE, 5, "enchantment_puncture");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.1 per level
        return 0.1 * level;
    }
}
