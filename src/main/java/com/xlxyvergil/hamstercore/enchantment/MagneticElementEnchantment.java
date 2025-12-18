package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class MagneticElementEnchantment extends ElementEnchantment {
    public MagneticElementEnchantment() {
        super(Rarity.RARE, ElementType.MAGNETIC, 5, "enchantment_magnetic");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.1 per level
        return 0.1 * level;
    }
}
