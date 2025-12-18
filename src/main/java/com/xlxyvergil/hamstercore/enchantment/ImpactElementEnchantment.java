package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class ImpactElementEnchantment extends ElementEnchantment {
    public ImpactElementEnchantment() {
        super(Rarity.COMMON, ElementType.IMPACT, 5, "enchantment_impact");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.1 per level
        return 0.1 * level;
    }
}
