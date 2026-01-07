package com.xlxyvergil.hamstercore.modification.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 反应式锻造配方接口
 * 允许配方在完成后执行额外操作
 */
public interface ReactiveSmithingRecipe {

    /**
     * 在配方完成后调用
     * @param inv 配方容器
     * @param player 执行配方的玩家
     * @param output 配方输出
     */
    void onCraft(Container inv, Player player, ItemStack output);

}
