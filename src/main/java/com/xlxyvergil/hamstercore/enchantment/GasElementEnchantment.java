package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class GasElementEnchantment extends ElementEnchantment {
    public GasElementEnchantment() {
        super(Rarity.RARE, ElementType.GAS, 5, "enchantment_gas");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
