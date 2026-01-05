package com.xlxyvergil.hamstercore.modification.recipe;

import dev.shadowsoffire.placebo.registry.RegistryEvent;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 配方序列化器
 */
public class ModificationRecipeSerializers {

    public static final RecipeSerializer<ModificationSocketingRecipe> SOCKETING = 
        ModificationSocketingRecipe.Serializer.INSTANCE;

    public static final RecipeSerializer<ModificationWithdrawalRecipe> WITHDRAWAL = 
        ModificationWithdrawalRecipe.Serializer.INSTANCE;

    public static final RecipeSerializer<AddSocketsRecipe> ADD_SOCKETS = 
        AddSocketsRecipe.Serializer.INSTANCE;

    public static void register(IEventBus modEventBus) {
        // 注册配方序列化器事件监听器
        modEventBus.register(ModificationRecipeSerializers.class);
    }

    @SubscribeEvent
    public static void registerSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        event.getRegistry().register(SOCKETING, "socketing");
        event.getRegistry().register(WITHDRAWAL, "withdrawal");
        event.getRegistry().register(ADD_SOCKETS, "add_sockets");
    }
}

