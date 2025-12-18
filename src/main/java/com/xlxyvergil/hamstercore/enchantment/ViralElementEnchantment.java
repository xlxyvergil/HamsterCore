package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class ViralElementEnchantment extends ElementEnchantment {
    public ViralElementEnchantment() {
        super(Rarity.UNCOMMON, ElementType.VIRAL, 5, "enchantment_viral");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.1 per level
        return 0.1 * level;
    }
}
