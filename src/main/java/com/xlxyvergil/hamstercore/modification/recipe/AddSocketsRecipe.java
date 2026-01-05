package com.xlxyvergil.hamstercore.modification.recipe;

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
 * 打孔配方 - 模仿 Apotheosis 的 AddSocketsRecipe
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

        // 物品必须有有效的类别
        WeaponType type = com.xlxyvergil.hamstercore.weapon.WeaponTypeDetector.detectWeaponType(base);
        if (type == null) {
            return false;
        }
        // 使用 category 变量来验证武器分类（可根据需要添加具体逻辑）
        WeaponCategory category = type.getCategory();
        if (category == null) {
            return false;
        }

        // 检查是否已达到最大槽位
        if (SocketHelper.getSockets(base) >= this.getMaxSockets()) {
            return false;
        }

        // 检查添加物品是否是打孔符文
        return this.getInput().test(add);
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack stack = container.getItem(BASE).copy();
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
        ItemStack stack = inv.getItem(BASE).copy();

        // 增加槽位：1 个特殊槽位 + 8 个通用槽位
        int currentSockets = SocketHelper.getSockets(stack);
        int currentSpecialSockets = SocketHelper.getSpecialSockets(stack);

        SocketHelper.setSpecialSockets(stack, currentSpecialSockets + 1);
        SocketHelper.setSockets(stack, currentSockets + 8);
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

