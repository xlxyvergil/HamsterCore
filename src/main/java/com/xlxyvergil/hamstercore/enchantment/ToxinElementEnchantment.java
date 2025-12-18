package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class ToxinElementEnchantment extends ElementEnchantment {
    public ToxinElementEnchantment() {
        super(Rarity.UNCOMMON, ElementType.TOXIN, 5, "enchantment_toxin");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.1 per level
        return 0.1 * level;
    }
}
