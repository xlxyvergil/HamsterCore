package com.xlxyvergil.hamstercore.modification.recipe;

import com.xlxyvergil.hamstercore.modification.ModificationHelper;
import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.ModificationItem;
import com.xlxyvergil.hamstercore.modification.ModificationItems;
import com.xlxyvergil.hamstercore.modification.SocketedModifications;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;


public class ModificationWithdrawalRecipe extends SmithingTransformRecipe implements ReactiveSmithingRecipe {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("hamstercore", "withdrawal");
    private static final int BASE = 1;
    private static final int ADDITION = 2;

    public ModificationWithdrawalRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(ModificationItems.SIGIL_OF_WITHDRAWAL.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base = inv.getItem(BASE);
        ItemStack sigils = inv.getItem(ADDITION);
        return base.getCount() == 1 && 
               sigils.getItem() == ModificationItems.SIGIL_OF_WITHDRAWAL.get() && 
               !ModificationHelper.getModifications(base).streamValidModifications().toList().isEmpty();
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess regs) {
        ItemStack out = inv.getItem(BASE).copy();
        if (out.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        // 移除所有词缀并清除改装件
        SocketedModifications modifications = ModificationHelper.getModifications(out);
        for (ModificationInstance instance : modifications.streamValidModifications().toList()) {
            instance.removeAffixes(out);
        }
        
        ModificationHelper.clearModifications(out);
        return out;
    }

    @Override
    public void onCraft(Container inv, Player player, ItemStack output) {
        ItemStack base = inv.getItem(BASE);
        SocketedModifications modifications = ModificationHelper.getModifications(base);
        
        for (ModificationInstance instance : modifications.streamValidModifications().toList()) {
            // 已经在assemble方法中移除了词缀，这里只需要返回改装件
            ItemStack stack = ModificationItem.createModificationStack(instance.modification());
            if (!player.addItem(stack)) {
                Blocks.AIR.popResource(player.level(), player.blockPosition(), stack);
            }
        }
        
        // 已经在assemble方法中清除了改装件，这里不需要再次清除
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

    public static class Serializer implements RecipeSerializer<ModificationWithdrawalRecipe> {
        public static Serializer INSTANCE = new Serializer();

        @Override
        public ModificationWithdrawalRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new ModificationWithdrawalRecipe();
        }

        @Override
        public ModificationWithdrawalRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new ModificationWithdrawalRecipe();
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ModificationWithdrawalRecipe recipe) {
        }
    }
}
