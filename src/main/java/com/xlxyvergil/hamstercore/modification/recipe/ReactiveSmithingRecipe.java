package com.xlxyvergil.hamstercore.modification.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ReactiveSmithingRecipe {
    void onCraft(Container inv, Player player, ItemStack output);
}
