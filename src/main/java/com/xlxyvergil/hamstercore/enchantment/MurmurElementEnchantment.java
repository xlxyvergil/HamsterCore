package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class MurmurElementEnchantment extends ElementEnchantment {
    public MurmurElementEnchantment() {
        super(Rarity.VERY_RARE, ElementType.MURMUR, 5, "enchantment_murmur");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
