package com.xlxyvergil.hamstercore.modification;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModificationItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "hamstercore");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "hamstercore");

    public static final RegistryObject<Item> MODIFICATION = ITEMS.register("modification",
        () -> new ModificationItem(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SIGIL_OF_SOCKETING = ITEMS.register("sigil_of_socketing",
        () -> new Item(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SIGIL_OF_WITHDRAWAL = ITEMS.register("sigil_of_withdrawal",
        () -> new Item(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    public static final RegistryObject<CreativeModeTab> MODIFICATION_TAB = CREATIVE_MODE_TABS.register("modification",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.hamstercore.modification"))
            .icon(() -> new ItemStack(MODIFICATION.get()))
            .displayItems((parameters, output) -> {
                output.acceptAll(ModificationItem.fillItemCategory());
                output.accept(SIGIL_OF_SOCKETING.get());
                output.accept(SIGIL_OF_WITHDRAWAL.get());
            })
            .build());
}
