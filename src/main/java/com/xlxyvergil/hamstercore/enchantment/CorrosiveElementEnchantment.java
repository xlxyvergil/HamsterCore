package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class CorrosiveElementEnchantment extends ElementEnchantment {
    public CorrosiveElementEnchantment() {
        super(Rarity.RARE, ElementType.CORROSIVE, 5, "enchantment_corrosive");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.1 per level
        return 0.1 * level;
    }
}
