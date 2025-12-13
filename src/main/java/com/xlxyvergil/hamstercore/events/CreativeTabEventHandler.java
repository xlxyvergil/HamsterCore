package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.enchantment.ElementEnchantment;
import com.xlxyvergil.hamstercore.enchantment.ModEnchantments;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

@Mod.EventBusSubscriber(modid = "hamstercore", bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTabEventHandler {

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SEARCH) {
            // 将所有元素附魔书添加到搜索标签页
            for (Map.Entry<ElementType, RegistryObject<? extends ElementEnchantment>> entry : 
                 ModEnchantments.ELEMENT_ENCHANTMENTS.entrySet()) {
                
                ElementType elementType = entry.getKey();
                RegistryObject<? extends ElementEnchantment> enchantmentRegistryObject = entry.getValue();
                
                if (enchantmentRegistryObject.isPresent()) {
                    ElementEnchantment enchantment = enchantmentRegistryObject.get();
                    
                    // 为每个附魔等级创建附魔书
                    for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                        EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentInstance(enchantment, level));
                        event.accept(enchantedBook);
                    }
                }
            }
        }
    }
}