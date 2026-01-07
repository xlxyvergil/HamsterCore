package com.xlxyvergil.hamstercore.modification.recipe;

import com.xlxyvergil.hamstercore.modification.ModificationItems;
import com.xlxyvergil.hamstercore.modification.SocketHelper;
import com.xlxyvergil.hamstercore.weapon.WeaponCategory;
import com.xlxyvergil.hamstercore.weapon.WeaponType;
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
 * 打孔配方
 */
public class AddSocketsRecipe extends SmithingTransformRecipe implements ReactiveSmithingRecipe {

    public static final ResourceLocation RECIPE_ID = new ResourceLocation("hamstercore", "add_sockets");
    
    // Forge smithing slot indices
    public static final int TEMPLATE = 0, BASE = 1, ADDITION = 2;

    private final Ingredient input;
    private final int maxSockets;

    public AddSocketsRecipe(ResourceLocation id, Ingredient input, int maxSockets) {
        super(id, Ingredient.EMPTY, Ingredient.EMPTY, input, ItemStack.EMPTY);
        this.input = input;
        this.maxSockets = maxSockets;
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack base = container.getItem(BASE);
        ItemStack add = container.getItem(ADDITION);

        // 检查添加物品是否是打孔符文
        if (!this.getInput().test(add)) {
            return false;
        }

        // 检查是普通Tool Augmenter还是Specialized Tool Augmenter
        boolean isSpecialized = add.getItem() == ModificationItems.SPECIALIZED_TOOL_AUGMENTER.get();
        
        if (isSpecialized) {
            // Specialized Tool Augmenter：检查是否已达到最大特殊槽位（1个）
            return SocketHelper.getSpecialSockets(base) < 1;
        } else {
            // 普通Tool Augmenter：检查是否已达到最大通用槽位（8个）
            return SocketHelper.getSockets(base) < 8;
        }
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack stack = container.getItem(BASE).copy();
        ItemStack add = container.getItem(ADDITION);
        
        // 检查是普通Tool Augmenter还是Specialized Tool Augmenter
        boolean isSpecialized = add.getItem() == ModificationItems.SPECIALIZED_TOOL_AUGMENTER.get();
        
        if (isSpecialized) {
            // Specialized Tool Augmenter：添加1个特殊槽位（最多1个）
            if (SocketHelper.getSpecialSockets(stack) < 1) {
                SocketHelper.setSpecialSockets(stack, 1);
            }
        } else {
            // 普通Tool Augmenter：添加8个通用槽位（最多8个）
            if (SocketHelper.getSockets(stack) < 8) {
                SocketHelper.setSockets(stack, 8);
            }
        }
        
        return stack;
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

    public Ingredient getInput() {
        return this.input;
    }

    public int getMaxSockets() {
        return this.maxSockets;
    }

    @Override
    public void onCraft(Container inv, Player player, ItemStack output) {
        // 槽位添加已经在assemble方法中完成，这里不需要额外操作
    }

    public static class Serializer implements RecipeSerializer<AddSocketsRecipe> {
        public static Serializer INSTANCE = new Serializer();

        @Override
        public AddSocketsRecipe fromJson(ResourceLocation pRecipeId, com.google.gson.JsonObject pJson) {
            com.google.gson.JsonObject inputObj = pJson.getAsJsonObject("input");
            Ingredient item = net.minecraftforge.common.crafting.CraftingHelper.getIngredient(inputObj, false);
            int maxSockets = pJson.get("max_sockets").getAsInt();
            return new AddSocketsRecipe(pRecipeId, item, maxSockets);
        }

        @Override
        public AddSocketsRecipe fromNetwork(ResourceLocation pRecipeId, net.minecraft.network.FriendlyByteBuf pBuffer) {
            return new AddSocketsRecipe(pRecipeId, Ingredient.fromNetwork(pBuffer), pBuffer.readInt());
        }

        @Override
        public void toNetwork(net.minecraft.network.FriendlyByteBuf pBuffer, AddSocketsRecipe pRecipe) {
            pRecipe.input.toNetwork(pBuffer);
            pBuffer.writeInt(pRecipe.getMaxSockets());
        }
    }
}

