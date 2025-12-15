package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;


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

    // 元素附魔映射
    public static final Map<ElementType, RegistryObject<? extends ElementEnchantment>> ELEMENT_ENCHANTMENTS = new HashMap<>();
    
    static {
        // 物理元素
        ELEMENT_ENCHANTMENTS.put(ElementType.SLASH, SLASH_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.PUNCTURE, PUNCTURE_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.IMPACT, IMPACT_ELEMENT);
        
        // 基础元素
        ELEMENT_ENCHANTMENTS.put(ElementType.HEAT, HEAT_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.COLD, COLD_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.ELECTRICITY, ELECTRICITY_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.TOXIN, TOXIN_ELEMENT);
        
        // 复合元素
        ELEMENT_ENCHANTMENTS.put(ElementType.BLAST, BLAST_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.RADIATION, RADIATION_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.GAS, GAS_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.MAGNETIC, MAGNETIC_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.VIRAL, VIRAL_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.CORROSIVE, CORROSIVE_ELEMENT);
        
        // 派系元素
        ELEMENT_ENCHANTMENTS.put(ElementType.GRINEER, GRINEER_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.INFESTED, INFESTED_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.CORPUS, CORPUS_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.OROKIN, OROKIN_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.SENTIENT, SENTIENT_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.MURMUR, MURMUR_ELEMENT);
        
        // 特殊属�?
        ELEMENT_ENCHANTMENTS.put(ElementType.CRITICAL_CHANCE, CRITICAL_CHANCE_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.CRITICAL_DAMAGE, CRITICAL_DAMAGE_ELEMENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.TRIGGER_CHANCE, TRIGGER_CHANCE_ELEMENT);
    }
    
    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
