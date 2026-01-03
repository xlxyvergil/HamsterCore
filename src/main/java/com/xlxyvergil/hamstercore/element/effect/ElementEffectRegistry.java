package com.xlxyvergil.hamstercore.element.effect;

import com.xlxyvergil.hamstercore.element.effect.effects.*;
import com.xlxyvergil.hamstercore.HamsterCore;
import dev.shadowsoffire.placebo.registry.RegObjHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.RegistryObject;

import static dev.shadowsoffire.placebo.registry.RegObjHelper.*;

/**
 * 元素效果注册类
 * 注册所有元素效果
 */
public class ElementEffectRegistry {

    // 物理元素效果
    public static final RegistryObject<MobEffect> IMPACT = mobEffect(HamsterCore.MODID, "impact", ImpactEffect::new);
    public static final RegistryObject<MobEffect> SLASH = mobEffect(HamsterCore.MODID, "slash", SlashEffect::new);
    public static final RegistryObject<MobEffect> PUNCTURE = mobEffect(HamsterCore.MODID, "puncture", PunctureEffect::new);

    // 基础元素效果
    public static final RegistryObject<MobEffect> COLD = mobEffect(HamsterCore.MODID, "cold", ColdEffect::new);
    public static final RegistryObject<MobEffect> ELECTRICITY = mobEffect(HamsterCore.MODID, "electricity", ElectricityEffect::new);
    public static final RegistryObject<MobEffect> ELECTRIC_CLOUD = mobEffect(HamsterCore.MODID, "electric_cloud", ElectricCloudEffect::new);
    public static final RegistryObject<MobEffect> HEAT = mobEffect(HamsterCore.MODID, "heat", HeatEffect::new);
    public static final RegistryObject<MobEffect> TOXIN = mobEffect(HamsterCore.MODID, "toxin", ToxinEffect::new);

    // 复合元素效果
    public static final RegistryObject<MobEffect> BLAST = mobEffect(HamsterCore.MODID, "blast", BlastEffect::new);
    public static final RegistryObject<MobEffect> CORROSIVE = mobEffect(HamsterCore.MODID, "corrosive", CorrosiveEffect::new);
    public static final RegistryObject<MobEffect> GAS = mobEffect(HamsterCore.MODID, "gas", GasEffect::new);
    public static final RegistryObject<MobEffect> GAS_CLOUD = mobEffect(HamsterCore.MODID, "gas_cloud", GasCloudEffect::new);
    public static final RegistryObject<MobEffect> MAGNETIC = mobEffect(HamsterCore.MODID, "magnetic", MagneticEffect::new);
    public static final RegistryObject<MobEffect> RADIATION = mobEffect(HamsterCore.MODID, "radiation", RadiationEffect::new);
    public static final RegistryObject<MobEffect> VIRAL = mobEffect(HamsterCore.MODID, "viral", ViralEffect::new);
}