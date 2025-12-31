package com.xlxyvergil.hamstercore.modification.recipe;

import com.xlxyvergil.hamstercore.modification.Modification;
import com.xlxyvergil.hamstercore.modification.ModificationHelper;
import com.xlxyvergil.hamstercore.modification.ModificationItem;
import com.xlxyvergil.hamstercore.modification.ModificationItems;
import com.xlxyvergil.hamstercore.modification.ModificationRegistry;
import com.google.gson.JsonObject;
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

import java.util.Optional;

public class ModificationSocketingRecipe extends SmithingTransformRecipe {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("hamstercore", "socketing");
    private static final int BASE = 1;
    private static final int ADDITION = 2;

    public ModificationSocketingRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(ModificationItems.MODIFICATION.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack input = inv.getItem(BASE);
        ItemStack modificationStack = inv.getItem(ADDITION);

        if (!modificationStack.hasTag() || !modificationStack.getTag().contains(ModificationItem.TAG_MODIFICATION_ID)) {
            return false;
        }

        String id = modificationStack.getTag().getString(ModificationItem.TAG_MODIFICATION_ID);
        Optional<Modification> mod = ModificationRegistry.getInstance().getModification(ResourceLocation.tryParse(id));
        if (mod.isEmpty()) {
            return false;
        }

        Modification modification = mod.get();
        // 使用ModificationHelper检查是否可以添加改装件
        return ModificationHelper.hasEmptySockets(input) && modification.canApplyTo(input, modificationStack);
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess regs) {
        ItemStack input = inv.getItem(BASE);
        ItemStack modificationStack = inv.getItem(ADDITION);

        ItemStack result = input.copy();
        result.setCount(1);

        String id = modificationStack.getTag().getString(ModificationItem.TAG_MODIFICATION_ID);
        Optional<Modification> mod = ModificationRegistry.getInstance().getModification(ResourceLocation.tryParse(id));
        if (mod.isPresent()) {
            Modification modification = mod.get();
            // 使用ModificationHelper添加改装件
            ModificationHelper.addModification(result, modification);
        }

        return result;
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

    public static class Serializer implements RecipeSerializer<ModificationSocketingRecipe> {
        public static Serializer INSTANCE = new Serializer();

        @Override
        public ModificationSocketingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new ModificationSocketingRecipe();
        }

        @Override
        public ModificationSocketingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new ModificationSocketingRecipe();
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ModificationSocketingRecipe recipe) {
        }
    }
}
