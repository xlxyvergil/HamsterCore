package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(Registries.ENCHANTMENT, HamsterCore.MODID);

    // 使用 EnumMap 存储所有元素附魔
    public static final Map<ElementType, RegistryObject<ElementEnchantment>> ELEMENT_ENCHANTMENTS = new EnumMap<>(ElementType.class);

    static {
        // 为所有物理元素创建附魔
        for (ElementType type : ElementType.getPhysicalElements()) {
            ELEMENT_ENCHANTMENTS.put(type, ENCHANTMENTS.register(
                    type.getName() + "_element",
                    () -> new ElementEnchantment(Enchantment.Rarity.COMMON, type, 1, "enchantment_" + type.getName())
            ));
        }

        // 为所有基础元素创建附魔
        for (ElementType type : ElementType.getBasicElements()) {
            ELEMENT_ENCHANTMENTS.put(type, ENCHANTMENTS.register(
                    type.getName() + "_element",
                    () -> new ElementEnchantment(Enchantment.Rarity.UNCOMMON, type, 1, "enchantment_" + type.getName())
            ));
        }

        // 为所有复合元素创建附魔
        for (ElementType type : ElementType.getComplexElements()) {
            ELEMENT_ENCHANTMENTS.put(type, ENCHANTMENTS.register(
                    type.getName() + "_element",
                    () -> new ElementEnchantment(Enchantment.Rarity.RARE, type, 1, "enchantment_" + type.getName())
            ));
        }

        // 为派系元素创建附魔
        ELEMENT_ENCHANTMENTS.put(ElementType.GRINEER, ENCHANTMENTS.register(
                "grineer_element",
                () -> new ElementEnchantment(Enchantment.Rarity.VERY_RARE, ElementType.GRINEER, 1, "enchantment_grineer")
        ));

        ELEMENT_ENCHANTMENTS.put(ElementType.INFESTED, ENCHANTMENTS.register(
                "infested_element",
                () -> new ElementEnchantment(Enchantment.Rarity.VERY_RARE, ElementType.INFESTED, 1, "enchantment_infested")
        ));

        ELEMENT_ENCHANTMENTS.put(ElementType.CORPUS, ENCHANTMENTS.register(
                "corpus_element",
                () -> new ElementEnchantment(Enchantment.Rarity.VERY_RARE, ElementType.CORPUS, 1, "enchantment_corpus")
        ));

        ELEMENT_ENCHANTMENTS.put(ElementType.OROKIN, ENCHANTMENTS.register(
                "orokin_element",
                () -> new ElementEnchantment(Enchantment.Rarity.VERY_RARE, ElementType.OROKIN, 1, "enchantment_orokin")
        ));

        ELEMENT_ENCHANTMENTS.put(ElementType.SENTIENT, ENCHANTMENTS.register(
                "sentient_element",
                () -> new ElementEnchantment(Enchantment.Rarity.VERY_RARE, ElementType.SENTIENT, 1, "enchantment_sentient")
        ));

        ELEMENT_ENCHANTMENTS.put(ElementType.MURMUR, ENCHANTMENTS.register(
                "murmur_element",
                () -> new ElementEnchantment(Enchantment.Rarity.VERY_RARE, ElementType.MURMUR, 1, "enchantment_murmur")
        ));

        // 为其他特殊属性创建附魔
        ELEMENT_ENCHANTMENTS.put(ElementType.CRITICAL_CHANCE, ENCHANTMENTS.register(
                "critical_chance_element",
                () -> new ElementEnchantment(Enchantment.Rarity.RARE, ElementType.CRITICAL_CHANCE, 1, "enchantment_critical_chance")
        ));

        ELEMENT_ENCHANTMENTS.put(ElementType.CRITICAL_DAMAGE, ENCHANTMENTS.register(
                "critical_damage_element",
                () -> new ElementEnchantment(Enchantment.Rarity.RARE, ElementType.CRITICAL_DAMAGE, 1, "enchantment_critical_damage")
        ));

        ELEMENT_ENCHANTMENTS.put(ElementType.TRIGGER_CHANCE, ENCHANTMENTS.register(
                "trigger_chance_element",
                () -> new ElementEnchantment(Enchantment.Rarity.RARE, ElementType.TRIGGER_CHANCE, 1, "enchantment_trigger_chance")
        ));
    }

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}