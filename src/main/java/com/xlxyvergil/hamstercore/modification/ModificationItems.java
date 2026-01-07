package com.xlxyvergil.hamstercore.modification;

import com.xlxyvergil.hamstercore.HamsterCore;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

/**
 * 改装件物品注册类
 */
public class ModificationItems {

    public static final DeferredHelper R = DeferredHelper.create(HamsterCore.MODID);

    public static final Item.Properties MODIFICATION_PROPS = new Item.Properties();

    public static final Item.Properties TOOL_AUGMENTER_PROPS = new Item.Properties()
        .stacksTo(1);

    public static final Item.Properties PRECISION_SCREWDRIVER_PROPS = new Item.Properties()
        .stacksTo(1);

    public static final RegistryObject<Item> MODIFICATION = R.item("modification", () -> new ModificationItem(MODIFICATION_PROPS));

    public static final RegistryObject<Item> TOOL_AUGMENTER = R.item("tool_augmenter", () -> new ToolAugmenterItem(TOOL_AUGMENTER_PROPS));

    public static final RegistryObject<Item> SPECIALIZED_TOOL_AUGMENTER = R.item("specialized_tool_augmenter", () -> new SpecializedToolAugmenterItem(TOOL_AUGMENTER_PROPS));

    public static final RegistryObject<Item> PRECISION_SCREWDRIVER = R.item("precision_screwdriver", () -> new PrecisionScrewdriverItem(PRECISION_SCREWDRIVER_PROPS));

    public static final RegistryObject<CreativeModeTab> MODIFICATION_TAB = R.tab("modifications", () -> {
        return CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.hamstercore.modifications"))
            .icon(() -> new ItemStack(MODIFICATION.get()))
            .build();
    });


    public static void bootstrap() {
        // 使用 TabFillingRegistry 将物品注册到对应的创造模式标签页
        TabFillingRegistry.register(MODIFICATION, MODIFICATION_TAB.getKey());
        TabFillingRegistry.register(TOOL_AUGMENTER, MODIFICATION_TAB.getKey());
        TabFillingRegistry.register(SPECIALIZED_TOOL_AUGMENTER, MODIFICATION_TAB.getKey());
        TabFillingRegistry.register(PRECISION_SCREWDRIVER, MODIFICATION_TAB.getKey());
    }
}
