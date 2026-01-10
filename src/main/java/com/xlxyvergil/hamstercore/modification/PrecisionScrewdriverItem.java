package com.xlxyvergil.hamstercore.modification;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

import dev.shadowsoffire.placebo.tabs.ITabFiller;

/**
 *  Precision Screwdriver
 */
public class PrecisionScrewdriverItem extends Item implements ITabFiller {

    public PrecisionScrewdriverItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.hamstercore.precision_screwdriver.tooltip"));
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, CreativeModeTab.Output out) {
        if (group == ModificationItems.MODIFICATION_TAB.get()) {
            ItemStack stack = new ItemStack(this);
            out.accept(stack);
        }
    }
}
