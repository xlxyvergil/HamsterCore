package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ElectricityElementEnchantment extends ElementEnchantment {
    public ElectricityElementEnchantment() {
        super(Rarity.UNCOMMON, ElementType.ELECTRICITY, 5, "enchantment_electricity");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
