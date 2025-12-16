package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class InfestedElementEnchantment extends ElementEnchantment {
    public InfestedElementEnchantment() {
        super(Rarity.VERY_RARE, ElementType.INFESTED, 5, "enchantment_infested");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
