package com.xlxyvergil.hamstercore.modification.recipe;

import com.google.gson.JsonObject;
import com.xlxyvergil.hamstercore.modification.ModificationHelper;
import com.xlxyvergil.hamstercore.modification.ModificationItems;
import com.xlxyvergil.hamstercore.util.ItemAffixApplicableUtil;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;

public class AddModificationSocketsRecipe extends SmithingTransformRecipe {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("hamstercore", "add_sockets");
    private static final int BASE = 1;
    private static final int ADDITION = 2;

    public AddModificationSocketsRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(ModificationItems.SIGIL_OF_SOCKETING.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base = inv.getItem(BASE);
        ItemStack sigil = inv.getItem(ADDITION);
        
        if (base.getCount() != 1) {
            return false;
        }
        
        if (sigil.getItem() != ModificationItems.SIGIL_OF_SOCKETING.get()) {
            return false;
        }
        
        if (!ItemAffixApplicableUtil.isItemApplicableForAffix(base)) {
            return false;
        }
        
        // 使用ModificationHelper检查当前槽位数量，只有当普通槽位小于8或者没有特殊槽位时才可以继续添加
        int currentSockets = ModificationHelper.getSockets(base);
        int currentSpecialSockets = ModificationHelper.getSpecialSockets(base);
        
        return currentSockets < 8 || currentSpecialSockets < 1;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess regs) {
        ItemStack out = inv.getItem(BASE).copy();
        if (out.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        // 使用ModificationHelper直接设置为8个普通槽位和1个特殊槽位
        ModificationHelper.setSockets(out, 8);
        ModificationHelper.setSpecialSockets(out, 1);
        
        return out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<AddModificationSocketsRecipe> {
        public static Serializer INSTANCE = new Serializer();

        @Override
        public AddModificationSocketsRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new AddModificationSocketsRecipe();
        }

        @Override
        public AddModificationSocketsRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new AddModificationSocketsRecipe();
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AddModificationSocketsRecipe recipe) {
        }
    }
}
