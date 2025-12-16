package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class HeatElementEnchantment extends ElementEnchantment {
    public HeatElementEnchantment() {
        super(Rarity.UNCOMMON, ElementType.HEAT, 5, "enchantment_heat");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
