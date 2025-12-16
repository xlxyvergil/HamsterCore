package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ColdElementEnchantment extends ElementEnchantment {
    public ColdElementEnchantment() {
        super(Rarity.UNCOMMON, ElementType.COLD, 5, "enchantment_cold");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
