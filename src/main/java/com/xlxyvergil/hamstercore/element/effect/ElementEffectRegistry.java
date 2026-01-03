package com.xlxyvergil.hamstercore.element.effect;

import com.xlxyvergil.hamstercore.element.effect.effects.BlastEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.ColdEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.CorrosiveEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.ElectricCloudEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.ElectricityEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.GasCloudEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.GasEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.HeatEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.ImpactEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.MagneticEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.PunctureEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.RadiationEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.SlashEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.ToxinEffect;
import com.xlxyvergil.hamstercore.element.effect.effects.ViralEffect;
import com.xlxyvergil.hamstercore.HamsterCore;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.minecraftforge.registries.RegistryObject;

import static com.xlxyvergil.hamstercore.HamsterCore.R;

/**
 * 元素效果注册类
 * 注册所有元素效果
 */
public class ElementEffectRegistry {
    
    public static final class Effects {
        // 物理元素效果
        public static final RegistryObject<ImpactEffect> IMPACT = R.effect("impact", ImpactEffect::new);
        public static final RegistryObject<SlashEffect> SLASH = R.effect("slash", SlashEffect::new);
        public static final RegistryObject<PunctureEffect> PUNCTURE = R.effect("puncture", PunctureEffect::new);

        // 基础元素效果
        public static final RegistryObject<ColdEffect> COLD = R.effect("cold", ColdEffect::new);
        public static final RegistryObject<ElectricityEffect> ELECTRICITY = R.effect("electricity", ElectricityEffect::new);
        public static final RegistryObject<ElectricCloudEffect> ELECTRIC_CLOUD = R.effect("electric_cloud", ElectricCloudEffect::new);
        public static final RegistryObject<HeatEffect> HEAT = R.effect("heat", HeatEffect::new);
        public static final RegistryObject<ToxinEffect> TOXIN = R.effect("toxin", ToxinEffect::new);

        // 复合元素效果
        public static final RegistryObject<BlastEffect> BLAST = R.effect("blast", BlastEffect::new);
        public static final RegistryObject<CorrosiveEffect> CORROSIVE = R.effect("corrosive", CorrosiveEffect::new);
        public static final RegistryObject<GasEffect> GAS = R.effect("gas", GasEffect::new);
        public static final RegistryObject<GasCloudEffect> GAS_CLOUD = R.effect("gas_cloud", GasCloudEffect::new);
        public static final RegistryObject<MagneticEffect> MAGNETIC = R.effect("magnetic", MagneticEffect::new);
        public static final RegistryObject<RadiationEffect> RADIATION = R.effect("radiation", RadiationEffect::new);
        public static final RegistryObject<ViralEffect> VIRAL = R.effect("viral", ViralEffect::new);
    }
}