package com.xlxyvergil.hamstercore.modification.recipe;

import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.ModificationItem;
import com.xlxyvergil.hamstercore.modification.ModificationItems;
import com.xlxyvergil.hamstercore.modification.SocketHelper;
import com.xlxyvergil.hamstercore.modification.SocketedModifications;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 安装配方 - 模仿 Apotheosis 的 SocketingRecipe
 */
public class ModificationSocketingRecipe extends SmithingTransformRecipe {

    public static final ResourceLocation RECIPE_ID = new ResourceLocation("hamstercore", "socketing");
    
    // Forge smithing slot indices
    public static final int TEMPLATE = 0, BASE = 1, ADDITION = 2;

    public ModificationSocketingRecipe(ResourceLocation id) {
        super(id, Ingredient.EMPTY, 
            Ingredient.EMPTY, 
            Ingredient.of(ModificationItems.MODIFICATION.get()), 
            ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack base = container.getItem(BASE);
        ItemStack add = container.getItem(ADDITION);

        // 基础物品必须有槽位
        if (!SocketHelper.hasEmptySockets(base)) {
            return false;
        }

        // 添加物品必须是改装件
        if (!isModification(add)) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否是改装件
     */
    private boolean isModification(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == ModificationItems.MODIFICATION.get();
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack base = container.getItem(BASE).copy();
        ItemStack add = container.getItem(ADDITION).copy();

        // 找到第一个空槽位
        int slot = SocketHelper.getFirstEmptySocket(base);
        if (slot < 0) {
            return ItemStack.EMPTY;
        }

        // 获取当前改装件列表
        SocketedModifications mods = SocketHelper.getModifications(base);
        List<ModificationInstance> modificationList = new java.util.ArrayList<>(mods.modifications());

        // 获取改装件ID
        String modId = ModificationItem.getModificationId(add);
        if (modId == null || modId.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 创建新的改装件实例（保持原base的NBT）
        ModificationInstance newInst = new ModificationInstance(modId, java.util.UUID.randomUUID());
        
        // 替换指定槽位的改装件
        modificationList.set(slot, newInst);

        // 应用新改装件的词缀
        SocketHelper.setModifications(base, modificationList);
        new SocketedModifications(modificationList).applyAllModifications(base, null);

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

    public static class Serializer implements RecipeSerializer<ModificationSocketingRecipe> {
        public static Serializer INSTANCE = new Serializer();

        @Override
        public ModificationSocketingRecipe fromJson(ResourceLocation pRecipeId, com.google.gson.JsonObject pJson) {
            return new ModificationSocketingRecipe(pRecipeId);
        }

        @Override
        public ModificationSocketingRecipe fromNetwork(ResourceLocation pRecipeId, net.minecraft.network.FriendlyByteBuf pBuffer) {
            return new ModificationSocketingRecipe(pRecipeId);
        }

        @Override
        public void toNetwork(net.minecraft.network.FriendlyByteBuf pBuffer, ModificationSocketingRecipe pRecipe) {
        }
    }
}


