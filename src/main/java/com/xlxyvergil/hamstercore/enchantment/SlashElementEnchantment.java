package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class SlashElementEnchantment extends ElementEnchantment {
    public SlashElementEnchantment() {
        super(Rarity.COMMON, ElementType.SLASH, 5, "enchantment_slash");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.1 per level
        return 0.1 * level;
    }
}
