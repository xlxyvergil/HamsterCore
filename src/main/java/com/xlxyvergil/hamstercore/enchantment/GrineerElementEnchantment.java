package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class GrineerElementEnchantment extends ElementEnchantment {
    public GrineerElementEnchantment() {
        super(Rarity.VERY_RARE, ElementType.GRINEER, 5, "enchantment_grineer");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
