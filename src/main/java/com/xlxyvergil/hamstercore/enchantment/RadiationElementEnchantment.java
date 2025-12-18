package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class RadiationElementEnchantment extends ElementEnchantment {
    public RadiationElementEnchantment() {
        super(Rarity.RARE, ElementType.RADIATION, 5, "enchantment_radiation");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.1 per level
        return 0.1 * level;
    }
}
