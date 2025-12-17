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
    public static final RegistryObject<SlashElementEnchantment> SLASH = ENCHANTMENTS.register(
            "slash", SlashElementEnchantment::new);
            
    public static final RegistryObject<PunctureElementEnchantment> PUNCTURE = ENCHANTMENTS.register(
            "puncture", PunctureElementEnchantment::new);
            
    public static final RegistryObject<ImpactElementEnchantment> IMPACT = ENCHANTMENTS.register(
            "impact", ImpactElementEnchantment::new);

    // 基础元素附魔
    public static final RegistryObject<HeatElementEnchantment> HEAT = ENCHANTMENTS.register(
            "heat", HeatElementEnchantment::new);
            
    public static final RegistryObject<ColdElementEnchantment> COLD = ENCHANTMENTS.register(
            "cold", ColdElementEnchantment::new);
            
    public static final RegistryObject<ElectricityElementEnchantment> ELECTRICITY = ENCHANTMENTS.register(
            "electricity", ElectricityElementEnchantment::new);
            
    public static final RegistryObject<ToxinElementEnchantment> TOXIN = ENCHANTMENTS.register(
            "toxin", ToxinElementEnchantment::new);

    // 复合元素附魔
    public static final RegistryObject<BlastElementEnchantment> BLAST = ENCHANTMENTS.register(
            "blast", BlastElementEnchantment::new);
            
    public static final RegistryObject<RadiationElementEnchantment> RADIATION = ENCHANTMENTS.register(
            "radiation", RadiationElementEnchantment::new);
            
    public static final RegistryObject<GasElementEnchantment> GAS = ENCHANTMENTS.register(
            "gas", GasElementEnchantment::new);
            
    public static final RegistryObject<MagneticElementEnchantment> MAGNETIC = ENCHANTMENTS.register(
            "magnetic", MagneticElementEnchantment::new);
            
    public static final RegistryObject<ViralElementEnchantment> VIRAL = ENCHANTMENTS.register(
            "viral", ViralElementEnchantment::new);
            
    public static final RegistryObject<CorrosiveElementEnchantment> CORROSIVE = ENCHANTMENTS.register(
            "corrosive", CorrosiveElementEnchantment::new);

    // 派系元素附魔
    public static final RegistryObject<GrineerElementEnchantment> GRINEER = ENCHANTMENTS.register(
            "grineer", GrineerElementEnchantment::new);
            
    public static final RegistryObject<InfestedElementEnchantment> INFESTED = ENCHANTMENTS.register(
            "infested", InfestedElementEnchantment::new);
            
    public static final RegistryObject<CorpusElementEnchantment> CORPUS = ENCHANTMENTS.register(
            "corpus", CorpusElementEnchantment::new);
            
    public static final RegistryObject<OrokinElementEnchantment> OROKIN = ENCHANTMENTS.register(
            "orokin", OrokinElementEnchantment::new);
            
    public static final RegistryObject<SentientElementEnchantment> SENTIENT = ENCHANTMENTS.register(
            "sentient", SentientElementEnchantment::new);
            
    public static final RegistryObject<MurmurElementEnchantment> MURMUR = ENCHANTMENTS.register(
            "murmur", MurmurElementEnchantment::new);

    // 特殊属性附魔
    public static final RegistryObject<CriticalChanceElementEnchantment> CRITICAL_CHANCE = ENCHANTMENTS.register(
            "critical_chance", CriticalChanceElementEnchantment::new);
            
    public static final RegistryObject<CriticalDamageElementEnchantment> CRITICAL_DAMAGE = ENCHANTMENTS.register(
            "critical_damage", CriticalDamageElementEnchantment::new);
            
    public static final RegistryObject<TriggerChanceElementEnchantment> TRIGGER_CHANCE = ENCHANTMENTS.register(
            "trigger_chance", TriggerChanceElementEnchantment::new);

    // 元素附魔映射
    public static final Map<ElementType, RegistryObject<? extends ElementEnchantment>> ELEMENT_ENCHANTMENTS = new HashMap<>();
    
    static {
        // 物理元素
        ELEMENT_ENCHANTMENTS.put(ElementType.SLASH, SLASH);
        ELEMENT_ENCHANTMENTS.put(ElementType.PUNCTURE, PUNCTURE);
        ELEMENT_ENCHANTMENTS.put(ElementType.IMPACT, IMPACT);
        
        // 基础元素
        ELEMENT_ENCHANTMENTS.put(ElementType.HEAT, HEAT);
        ELEMENT_ENCHANTMENTS.put(ElementType.COLD, COLD);
        ELEMENT_ENCHANTMENTS.put(ElementType.ELECTRICITY, ELECTRICITY);
        ELEMENT_ENCHANTMENTS.put(ElementType.TOXIN, TOXIN);
        
        // 复合元素
        ELEMENT_ENCHANTMENTS.put(ElementType.BLAST, BLAST);
        ELEMENT_ENCHANTMENTS.put(ElementType.RADIATION, RADIATION);
        ELEMENT_ENCHANTMENTS.put(ElementType.GAS, GAS);
        ELEMENT_ENCHANTMENTS.put(ElementType.MAGNETIC, MAGNETIC);
        ELEMENT_ENCHANTMENTS.put(ElementType.VIRAL, VIRAL);
        ELEMENT_ENCHANTMENTS.put(ElementType.CORROSIVE, CORROSIVE);
        
        // 派系元素
        ELEMENT_ENCHANTMENTS.put(ElementType.GRINEER, GRINEER);
        ELEMENT_ENCHANTMENTS.put(ElementType.INFESTED, INFESTED);
        ELEMENT_ENCHANTMENTS.put(ElementType.CORPUS, CORPUS);
        ELEMENT_ENCHANTMENTS.put(ElementType.OROKIN, OROKIN);
        ELEMENT_ENCHANTMENTS.put(ElementType.SENTIENT, SENTIENT);
        ELEMENT_ENCHANTMENTS.put(ElementType.MURMUR, MURMUR);
        
        // 特殊属性
        ELEMENT_ENCHANTMENTS.put(ElementType.CRITICAL_CHANCE, CRITICAL_CHANCE);
        ELEMENT_ENCHANTMENTS.put(ElementType.CRITICAL_DAMAGE, CRITICAL_DAMAGE);
        ELEMENT_ENCHANTMENTS.put(ElementType.TRIGGER_CHANCE, TRIGGER_CHANCE);
    }
    
    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
