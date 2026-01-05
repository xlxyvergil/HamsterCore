package com.xlxyvergil.hamstercore.modification;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.shadowsoffire.placebo.tabs.ITabFiller;

/**
 * 特殊打孔符文物品类 - Specialized Tool Augmenter
 */
public class SpecializedToolAugmenterItem extends Item implements ITabFiller {

    public SpecializedToolAugmenterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, CreativeModeTab.Output out) {
        if (group == ModificationItems.TOOLS_TAB.get()) {
            ItemStack stack = new ItemStack(this);
            out.accept(stack);
        }
    }
}