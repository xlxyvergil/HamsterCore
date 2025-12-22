package com.xlxyvergil.hamstercore.attribute;

import com.xlxyvergil.hamstercore.HamsterCore;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
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
    
    /** 护盾恢复延迟属性 - 实体受到伤害后护盾开始恢复的延迟时间 */
    public static final RegistryObject<Attribute> REGEN_DELAY = ATTRIBUTES.register("regen_delay", 
        () -> new RangedAttribute("attribute.name.hamstercore.regen_delay", 0.0D, 0.0D, 100.0D).setSyncable(true));
    
    /** 护盾耗尽恢复延迟属性 - 实体护盾耗尽后开始恢复的延迟时间 */
    public static final RegistryObject<Attribute> DEPLETED_REGEN_DELAY = ATTRIBUTES.register("depleted_regen_delay", 
        () -> new RangedAttribute("attribute.name.hamstercore.depleted_regen_delay", 0.0D, 0.0D, 100.0D).setSyncable(true));
    
    /** 护盾保险时间属性 - 实体护盾保险的持续时间（毫秒）*/
    public static final RegistryObject<Attribute> IMMUNITY_TIME = ATTRIBUTES.register("immunity_time", 
        () -> new RangedAttribute("attribute.name.hamstercore.immunity_time", 0.0D, 0.0D, 2500.0D).setSyncable(true));
    
    /**
     * 将所有自定义属性绑定到实体上
     */
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeModificationEvent event) {
        // 按照TACZ的方式：直接对所有实体类型添加属性
        event.getTypes().forEach(type -> {
            // 添加所有护甲和护盾相关属性
            event.add(type, ARMOR.get());
            event.add(type, SHIELD.get());
            event.add(type, REGEN_RATE.get());
            event.add(type, REGEN_DELAY.get());
            event.add(type, DEPLETED_REGEN_DELAY.get());
            event.add(type, IMMUNITY_TIME.get());
        });
    }
}