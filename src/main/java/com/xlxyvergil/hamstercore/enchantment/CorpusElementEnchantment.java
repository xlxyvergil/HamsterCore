package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class CorpusElementEnchantment extends ElementEnchantment {
    public CorpusElementEnchantment() {
        super(Rarity.VERY_RARE, ElementType.CORPUS, 5, "enchantment_corpus");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.05 per level
        return 0.05 * level;
    }
}
