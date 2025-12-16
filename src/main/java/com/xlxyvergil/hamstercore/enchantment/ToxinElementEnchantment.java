package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ToxinElementEnchantment extends ElementEnchantment {
    public ToxinElementEnchantment() {
        super(Rarity.UNCOMMON, ElementType.TOXIN, 5, "enchantment_toxin");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
