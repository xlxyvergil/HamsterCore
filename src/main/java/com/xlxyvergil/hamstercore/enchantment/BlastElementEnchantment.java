package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class BlastElementEnchantment extends ElementEnchantment {
    public BlastElementEnchantment() {
        super(Rarity.RARE, ElementType.BLAST, 5, "enchantment_blast");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
