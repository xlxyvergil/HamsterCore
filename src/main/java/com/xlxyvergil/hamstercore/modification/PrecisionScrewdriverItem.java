package com.xlxyvergil.hamstercore.modification;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.shadowsoffire.placebo.tabs.ITabFiller;

/**
 * 卸载符文物品类 - Precision Screwdriver
 */
public class PrecisionScrewdriverItem extends Item implements ITabFiller {

    public PrecisionScrewdriverItem(Properties properties) {
        super(properties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, CreativeModeTab.Output out) {
        if (group == ModificationItems.MODIFICATION_TAB.get()) {
            ItemStack stack = new ItemStack(this);
            out.accept(stack);
        }
    }
}
