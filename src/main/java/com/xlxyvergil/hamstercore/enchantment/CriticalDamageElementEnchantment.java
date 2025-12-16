package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class CriticalDamageElementEnchantment extends ElementEnchantment {
    public CriticalDamageElementEnchantment() {
        super(Rarity.RARE, ElementType.CRITICAL_DAMAGE, 5, "enchantment_critical_damage");
    }
    
    @Override
    public double getElementValue(int level) {
        // Calculate level-based value: 0.3 per level
        return 0.3 * level;
    }
}
