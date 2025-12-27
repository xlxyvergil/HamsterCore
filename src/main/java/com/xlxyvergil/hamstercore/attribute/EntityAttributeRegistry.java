package com.xlxyvergil.hamstercore.attribute;

import com.xlxyvergil.hamstercore.HamsterCore;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import dev.shadowsoffire.attributeslib.impl.PercentBasedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 实体属性注册类
 * 注册所有与护甲和护盾相关的实体属性
 */
@Mod.EventBusSubscriber(modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityAttributeRegistry {
    
    // 创建属性的DeferredRegister
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, HamsterCore.MODID);
    
    // 注册护甲和护盾相关属性
    
    /** 护甲属性 - 实体的总护甲值 */
    public static final RegistryObject<Attribute> ARMOR = ATTRIBUTES.register("armor", 
        () -> new RangedAttribute("attribute.name.hamstercore.armor", 0.0D, 0.0D, 2700.0D).setSyncable(true));
    
    /** 护盾属性 - 实体的总护盾值 */
    public static final RegistryObject<Attribute> SHIELD = ATTRIBUTES.register("shield", 
        () -> new RangedAttribute("attribute.name.hamstercore.shield", 0.0D, 0.0D, 99999.0D).setSyncable(true));
    
    /** 护盾恢复速率属性 - 实体的护盾每秒恢复值 */
    public static final RegistryObject<Attribute> REGEN_RATE = ATTRIBUTES.register("regen_rate", 
        () -> new RangedAttribute("attribute.name.hamstercore.regen_rate", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 护盾恢复延迟属性 - 实体受到伤害后护盾开始恢复的延迟时间（毫秒）*/
    public static final RegistryObject<Attribute> REGEN_DELAY = ATTRIBUTES.register("regen_delay", 
        () -> new RangedAttribute("attribute.name.hamstercore.regen_delay", 0.0D, 0.0D, 99999.0D).setSyncable(true));
    
    /** 护盾耗尽恢复延迟属性 - 实体护盾耗尽后开始恢复的延迟时间（毫秒）*/
    public static final RegistryObject<Attribute> DEPLETED_REGEN_DELAY = ATTRIBUTES.register("depleted_regen_delay", 
        () -> new RangedAttribute("attribute.name.hamstercore.depleted_regen_delay", 0.0D, 0.0D, 9999.0D).setSyncable(true));
    
    /** 护盾保险时间属性 - 实体护盾保险的持续时间（毫秒）*/
    public static final RegistryObject<Attribute> IMMUNITY_TIME = ATTRIBUTES.register("immunity_time", 
        () -> new RangedAttribute("attribute.name.hamstercore.immunity_time", 0.0D, 0.0D, 2500.0D).setSyncable(true));
    
    // ElementType相关属性，默认值为0
    
    // 物理元素
    /** 冲击属性 */
    public static final RegistryObject<Attribute> IMPACT = ATTRIBUTES.register("impact", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.impact", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 穿刺属性 */
    public static final RegistryObject<Attribute> PUNCTURE = ATTRIBUTES.register("puncture", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.puncture", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 切割属性 */
    public static final RegistryObject<Attribute> SLASH = ATTRIBUTES.register("slash", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.slash", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    // 基础元素
    /** 冰冻属性 */
    public static final RegistryObject<Attribute> COLD = ATTRIBUTES.register("cold", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.cold", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 电击属性 */
    public static final RegistryObject<Attribute> ELECTRICITY = ATTRIBUTES.register("electricity", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.electricity", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 火焰属性 */
    public static final RegistryObject<Attribute> HEAT = ATTRIBUTES.register("heat", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.heat", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 毒素属性 */
    public static final RegistryObject<Attribute> TOXIN = ATTRIBUTES.register("toxin", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.toxin", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    // 复合元素
    /** 爆炸属性 */
    public static final RegistryObject<Attribute> BLAST = ATTRIBUTES.register("blast", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.blast", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 腐蚀属性 */
    public static final RegistryObject<Attribute> CORROSIVE = ATTRIBUTES.register("corrosive", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.corrosive", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 毒气属性 */
    public static final RegistryObject<Attribute> GAS = ATTRIBUTES.register("gas", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.gas", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 磁力属性 */
    public static final RegistryObject<Attribute> MAGNETIC = ATTRIBUTES.register("magnetic", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.magnetic", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 辐射属性 */
    public static final RegistryObject<Attribute> RADIATION = ATTRIBUTES.register("radiation", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.radiation", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** 病毒属性 */
    public static final RegistryObject<Attribute> VIRAL = ATTRIBUTES.register("viral", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.viral", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    // 特殊属性
    /** 触发率属性 */
    public static final RegistryObject<Attribute> TRIGGER_CHANCE = ATTRIBUTES.register("trigger_chance", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.trigger_chance", 0.0D, 0.0D, 100.0D).setSyncable(true));
    
    // 派系元素
    /** Grineer派系属性 */
    public static final RegistryObject<Attribute> GRINEER = ATTRIBUTES.register("grineer", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.grineer", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** Infested派系属性 */
    public static final RegistryObject<Attribute> INFESTED = ATTRIBUTES.register("infested", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.infested", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** Corpus派系属性 */
    public static final RegistryObject<Attribute> CORPUS = ATTRIBUTES.register("corpus", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.corpus", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** Orokin派系属性 */
    public static final RegistryObject<Attribute> OROKIN = ATTRIBUTES.register("orokin", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.orokin", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** Sentient派系属性 */
    public static final RegistryObject<Attribute> SENTIENT = ATTRIBUTES.register("sentient", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.sentient", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /** Murmur派系属性 */
    public static final RegistryObject<Attribute> MURMUR = ATTRIBUTES.register("murmur", 
        () -> new PercentBasedAttribute("attribute.name.hamstercore.murmur", 0.0D, 0.0D, 1000.0D).setSyncable(true));
    
    /**
     * 将护甲、护盾和元素相关属性绑定到实体上
     * 所有自定义属性都必须绑定到实体上才能通过ItemAttributeModifierEvent应用修饰符
     */
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeModificationEvent event) {
        // 按照TACZ的方式：直接对所有实体类型添加护甲、护盾和元素相关属性
        event.getTypes().forEach(type -> {
            // 添加所有护甲和护盾相关属性
            event.add(type, ARMOR.get());
            event.add(type, SHIELD.get());
            event.add(type, REGEN_RATE.get());
            event.add(type, REGEN_DELAY.get());
            event.add(type, DEPLETED_REGEN_DELAY.get());
            event.add(type, IMMUNITY_TIME.get());
            
            // 添加所有元素相关属性
            event.add(type, IMPACT.get());
            event.add(type, PUNCTURE.get());
            event.add(type, SLASH.get());
            event.add(type, COLD.get());
            event.add(type, ELECTRICITY.get());
            event.add(type, HEAT.get());
            event.add(type, TOXIN.get());
            event.add(type, BLAST.get());
            event.add(type, CORROSIVE.get());
            event.add(type, GAS.get());
            event.add(type, MAGNETIC.get());
            event.add(type, RADIATION.get());
            event.add(type, VIRAL.get());
            event.add(type, TRIGGER_CHANCE.get());
            event.add(type, GRINEER.get());
            event.add(type, INFESTED.get());
            event.add(type, CORPUS.get());
            event.add(type, OROKIN.get());
            event.add(type, SENTIENT.get());
            event.add(type, MURMUR.get());
        });
    }
}