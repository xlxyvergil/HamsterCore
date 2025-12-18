package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;


public class TriggerChanceElementEnchantment extends ElementEnchantment {
    public TriggerChanceElementEnchantment() {
        super(Rarity.RARE, ElementType.TRIGGER_CHANCE, 3, "enchantment_trigger_chance");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.05 per level
        return 0.05 * level;
    }
}
