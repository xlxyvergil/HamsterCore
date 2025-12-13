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

    // 物理元素附魔
    public static final RegistryObject<SlashElementEnchantment> SLASH_ELEMENT = ENCHANTMENTS.register(
            "slash_element", SlashElementEnchantment::new);
            
    public static final RegistryObject<PunctureElementEnchantment> PUNCTURE_ELEMENT = ENCHANTMENTS.register(
            "puncture_element", PunctureElementEnchantment::new);
            
    public static final RegistryObject<ImpactElementEnchantment> IMPACT_ELEMENT = ENCHANTMENTS.register(
            "impact_element", ImpactElementEnchantment::new);

    // 基础元素附魔
    public static final RegistryObject<HeatElementEnchantment> HEAT_ELEMENT = ENCHANTMENTS.register(
            "heat_element", HeatElementEnchantment::new);
            
    public static final RegistryObject<ColdElementEnchantment> COLD_ELEMENT = ENCHANTMENTS.register(
            "cold_element", ColdElementEnchantment::new);
            
    public static final RegistryObject<ElectricityElementEnchantment> ELECTRICITY_ELEMENT = ENCHANTMENTS.register(
            "electricity_element", ElectricityElementEnchantment::new);
            
    public static final RegistryObject<ChemicalElementEnchantment> CHEMICAL_ELEMENT = ENCHANTMENTS.register(
            "chemical_element", ChemicalElementEnchantment::new);
            
    public static final RegistryObject<ToxinElementEnchantment> TOXIN_ELEMENT = ENCHANTMENTS.register(
            "toxin_element", ToxinElementEnchantment::new);

    // 复合元素附魔
    public static final RegistryObject<BlastElementEnchantment> BLAST_ELEMENT = ENCHANTMENTS.register(
            "blast_element", BlastElementEnchantment::new);
            
    public static final RegistryObject<RadiationElementEnchantment> RADIATION_ELEMENT = ENCHANTMENTS.register(
            "radiation_element", RadiationElementEnchantment::new);
            
    public static final RegistryObject<GasElementEnchantment> GAS_ELEMENT = ENCHANTMENTS.register(
            "gas_element", GasElementEnchantment::new);
            
    public static final RegistryObject<MagneticElementEnchantment> MAGNETIC_ELEMENT = ENCHANTMENTS.register(
            "magnetic_element", MagneticElementEnchantment::new);
            
    public static final RegistryObject<ViralElementEnchantment> VIRAL_ELEMENT = ENCHANTMENTS.register(
            "viral_element", ViralElementEnchantment::new);
            
    public static final RegistryObject<CorrosiveElementEnchantment> CORROSIVE_ELEMENT = ENCHANTMENTS.register(
            "corrosive_element", CorrosiveElementEnchantment::new);

    // 派系元素附魔
    public static final RegistryObject<GrineerElementEnchantment> GRINEER_ELEMENT = ENCHANTMENTS.register(
            "grineer_element", GrineerElementEnchantment::new);
            
    public static final RegistryObject<InfestedElementEnchantment> INFESTED_ELEMENT = ENCHANTMENTS.register(
            "infested_element", InfestedElementEnchantment::new);
            
    public static final RegistryObject<CorpusElementEnchantment> CORPUS_ELEMENT = ENCHANTMENTS.register(
            "corpus_element", CorpusElementEnchantment::new);
            
    public static final RegistryObject<OrokinElementEnchantment> OROKIN_ELEMENT = ENCHANTMENTS.register(
            "orokin_element", OrokinElementEnchantment::new);
            
    public static final RegistryObject<SentientElementEnchantment> SENTIENT_ELEMENT = ENCHANTMENTS.register(
            "sentient_element", SentientElementEnchantment::new);
            
    public static final RegistryObject<MurmurElementEnchantment> MURMUR_ELEMENT = ENCHANTMENTS.register(
            "murmur_element", MurmurElementEnchantment::new);

    // 特殊属性附魔
    public static final RegistryObject<CriticalChanceElementEnchantment> CRITICAL_CHANCE_ELEMENT = ENCHANTMENTS.register(
            "critical_chance_element", CriticalChanceElementEnchantment::new);
            
    public static final RegistryObject<CriticalDamageElementEnchantment> CRITICAL_DAMAGE_ELEMENT = ENCHANTMENTS.register(
            "critical_damage_element", CriticalDamageElementEnchantment::new);
            
    public static final RegistryObject<TriggerChanceElementEnchantment> TRIGGER_CHANCE_ELEMENT = ENCHANTMENTS.register(
            "trigger_chance_element", TriggerChanceElementEnchantment::new);

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}