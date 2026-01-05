package com.xlxyvergil.hamstercore.modification;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.shadowsoffire.placebo.tabs.ITabFiller;

/**
 * 打孔符文物品类 - Tool Augmenter
 */
public class ToolAugmenterItem extends Item implements ITabFiller {

    public ToolAugmenterItem(Properties properties) {
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
