package com.xlxyvergil.hamstercore.modification.recipe;

import com.google.common.collect.Lists;
import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.ModificationItems;
import com.xlxyvergil.hamstercore.modification.SocketHelper;
import com.xlxyvergil.hamstercore.modification.SocketedModifications;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;

/**
 * 卸载配方 - 模仿 Apotheosis 的 WithdrawalRecipe
 */
public class ModificationWithdrawalRecipe extends SmithingTransformRecipe implements ReactiveSmithingRecipe {

    public static final ResourceLocation RECIPE_ID = new ResourceLocation("hamstercore", "withdrawal");
    
    // Forge smithing slot indices
    public static final int TEMPLATE = 0, BASE = 1, ADDITION = 2;

    public ModificationWithdrawalRecipe(ResourceLocation id) {
        super(id, Ingredient.EMPTY, 
            Ingredient.EMPTY, 
            Ingredient.of(ModificationItems.PRECISION_SCREWDRIVER.get()), 
            ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack base = container.getItem(BASE);
        ItemStack add = container.getItem(ADDITION);

        // 添加物品必须是卸载工具（精密螺丝刀）
        if (!isWithdrawalTool(add)) {
            return false;
        }

        // 基础物品必须有已安装的改装件（特殊或通用）
        boolean hasValidNormalMods = SocketHelper.getModifications(base).stream().anyMatch(ModificationInstance::isValid);
        boolean hasValidSpecialMods = SocketHelper.getSpecialModifications(base).stream().anyMatch(ModificationInstance::isValid);
        
        return hasValidNormalMods || hasValidSpecialMods;
    }

    /**
     * 检查是否是卸载工具
     */
    private boolean isWithdrawalTool(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == com.xlxyvergil.hamstercore.modification.ModificationItems.PRECISION_SCREWDRIVER.get();
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack base = container.getItem(BASE).copy();

        // 移除所有词缀（通用和特殊改装件）
        SocketedModifications normalMods = SocketHelper.getModifications(base);
        normalMods.removeAllAffixes(base);
        
        // 移除特殊改装件的词缀
        List<ModificationInstance> specialMods = SocketHelper.getSpecialModifications(base);
        for (ModificationInstance specialMod : specialMods) {
            if (specialMod.isValid()) {
                specialMod.removeAffixes(base);
            }
        }

        // 清除 NBT 中的改装件数据
        SocketHelper.setModifications(base, Lists.newArrayList());
        SocketHelper.setSpecialModifications(base, Lists.newArrayList());

        return base;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return RECIPE_ID;
    }

    @Override
    public void onCraft(Container inv, Player player, ItemStack output) {
        // 使用output而不是base，因为base的NBT已经被assemble方法清空了
        // output是assemble返回的物品堆，其中包含原始的改装件数据
        
        // 获取所有已安装的改装件（通用和特殊）
        SocketedModifications normalMods = SocketHelper.getModifications(output);
        List<ModificationInstance> specialMods = SocketHelper.getSpecialModifications(output);

        // 将通用改装件返还给玩家
        for (int i = 0; i < normalMods.size(); i++) {
            ItemStack stack = normalMods.get(i).modificationStack();
            if (!stack.isEmpty()) {
                if (!player.addItem(stack)) {
                    // 如果玩家背包满了，掉落到地上
                    net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                        player.level(), player.getX(), player.getY(), player.getZ(), stack);
                    player.level().addFreshEntity(entity);
                }
            }
        }
        
        // 将特殊改装件返还给玩家
        for (ModificationInstance specialMod : specialMods) {
            ItemStack stack = specialMod.modificationStack();
            if (!stack.isEmpty()) {
                if (!player.addItem(stack)) {
                    // 如果玩家背包满了，掉落到地上
                    net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                        player.level(), player.getX(), player.getY(), player.getZ(), stack);
                    player.level().addFreshEntity(entity);
                }
            }
        }
    }

    public static class Serializer implements RecipeSerializer<ModificationWithdrawalRecipe> {
        public static Serializer INSTANCE = new Serializer();

        @Override
        public ModificationWithdrawalRecipe fromJson(ResourceLocation pRecipeId, com.google.gson.JsonObject pJson) {
            return new ModificationWithdrawalRecipe(pRecipeId);
        }

        @Override
        public ModificationWithdrawalRecipe fromNetwork(ResourceLocation pRecipeId, net.minecraft.network.FriendlyByteBuf pBuffer) {
            return new ModificationWithdrawalRecipe(pRecipeId);
        }

        @Override
        public void toNetwork(net.minecraft.network.FriendlyByteBuf pBuffer, ModificationWithdrawalRecipe pRecipe) {
        }
    }
}


