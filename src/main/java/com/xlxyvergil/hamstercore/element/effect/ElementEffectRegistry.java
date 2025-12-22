package com.xlxyvergil.hamstercore.element.effect;

import com.xlxyvergil.hamstercore.element.effect.effects.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.xlxyvergil.hamstercore.HamsterCore;

/**
 * 元素效果注册类
 * 注册所有元素效果
 */
public class ElementEffectRegistry {
    
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HamsterCore.MODID);
    
    // 物理元素效果
    public static final RegistryObject<MobEffect> IMPACT = EFFECTS.register("impact", ImpactEffect::new);
    public static final RegistryObject<MobEffect> SLASH = EFFECTS.register("slash", SlashEffect::new);
    public static final RegistryObject<MobEffect> PUNCTURE = EFFECTS.register("puncture", PunctureEffect::new);
    
    // 基础元素效果
    public static final RegistryObject<MobEffect> COLD = EFFECTS.register("cold", ColdEffect::new);
    public static final RegistryObject<MobEffect> ELECTRICITY = EFFECTS.register("electricity", ElectricityEffect::new);
    public static final RegistryObject<MobEffect> HEAT = EFFECTS.register("heat", HeatEffect::new);
    public static final RegistryObject<MobEffect> TOXIN = EFFECTS.register("toxin", ToxinEffect::new);
    
    // 复合元素效果
    public static final RegistryObject<MobEffect> BLAST = EFFECTS.register("blast", BlastEffect::new);
    public static final RegistryObject<MobEffect> CORROSIVE = EFFECTS.register("corrosive", CorrosiveEffect::new);
    public static final RegistryObject<MobEffect> GAS = EFFECTS.register("gas", GasEffect::new);
    public static final RegistryObject<MobEffect> MAGNETIC = EFFECTS.register("magnetic", MagneticEffect::new);
    public static final RegistryObject<MobEffect> RADIATION = EFFECTS.register("radiation", RadiationEffect::new);
    public static final RegistryObject<MobEffect> VIRAL = EFFECTS.register("viral", ViralEffect::new);
    
    public static void register() {
        EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}