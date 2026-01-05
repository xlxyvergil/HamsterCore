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
        // 只在工具标签页中显示
        if (group == com.xlxyvergil.hamstercore.modification.ModificationItems.TOOLS_TAB.get()) {
            ItemStack stack = new ItemStack(this);
            out.accept(stack);
        }
    }
}
