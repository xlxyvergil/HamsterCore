package com.xlxyvergil.hamstercore.modification.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModificationRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = 
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, "hamstercore");

    public static final RegistryObject<RecipeSerializer<?>> MODIFICATION_SOCKETING = 
        SERIALIZERS.register("socketing", ModificationSocketingRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<?>> MODIFICATION_WITHDRAWAL = 
        SERIALIZERS.register("withdrawal", ModificationWithdrawalRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<?>> ADD_MODIFICATION_SOCKETS = 
        SERIALIZERS.register("add_sockets", AddModificationSocketsRecipe.Serializer::new);
}
