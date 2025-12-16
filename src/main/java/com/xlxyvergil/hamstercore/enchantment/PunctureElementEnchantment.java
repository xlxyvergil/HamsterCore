package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class PunctureElementEnchantment extends ElementEnchantment {
    public PunctureElementEnchantment() {
        super(Rarity.COMMON, ElementType.PUNCTURE, 5, "enchantment_puncture");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
