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
    
    /** 基础护甲属性 - 实体的基础护甲值 */
    public static final RegistryObject<Attribute> BASE_ARMOR = ATTRIBUTES.register("base_armor", 
        () -> new RangedAttribute("attribute.name.hamstercore.base_armor", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    
    /** 护甲属性 - 实体的总护甲值 */
    public static final RegistryObject<Attribute> ARMOR = ATTRIBUTES.register("armor", 
        () -> new RangedAttribute("attribute.name.hamstercore.armor", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    
    /** 最大护盾属性 - 实体的最大护盾值 */
    public static final RegistryObject<Attribute> MAX_SHIELD = ATTRIBUTES.register("max_shield", 
        () -> new RangedAttribute("attribute.name.hamstercore.max_shield", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    
    /** 护盾恢复速率属性 - 实体的护盾每秒恢复值 */
    public static final RegistryObject<Attribute> REGEN_RATE = ATTRIBUTES.register("regen_rate", 
        () -> new RangedAttribute("attribute.name.hamstercore.regen_rate", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    
    /** 护盾恢复延迟属性 - 实体受到伤害后护盾开始恢复的延迟时间 */
    public static final RegistryObject<Attribute> REGEN_DELAY = ATTRIBUTES.register("regen_delay", 
        () -> new RangedAttribute("attribute.name.hamstercore.regen_delay", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    
    /**
     * 将所有自定义属性绑定到实体上
     */
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeModificationEvent event) {
        // 按照TACZ的方式：直接对所有实体类型添加属性
        event.getTypes().forEach(type -> {
            // 添加所有护甲和护盾相关属性
            event.add(type, BASE_ARMOR.get());
            event.add(type, ARMOR.get());
            event.add(type, MAX_SHIELD.get());
            event.add(type, REGEN_RATE.get());
            event.add(type, REGEN_DELAY.get());
        });
    }
}