package com.xlxyvergil.hamstercore.registry;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityAttributeRegistry {
    public static final DeferredRegister<net.minecraft.world.entity.ai.attributes.Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, HamsterCore.MODID);

    // 基础属性
    public static final RegistryObject<net.minecraft.world.entity.ai.attributes.Attribute> ARMOR = ATTRIBUTES.register("armor", 
        () -> new RangedAttribute("attribute.name.hamstercore.armor", 0.0D, 0.0D, 10000.0D).setSyncable(true));

    public static final RegistryObject<net.minecraft.world.entity.ai.attributes.Attribute> SHIELD = ATTRIBUTES.register("shield", 
        () -> new RangedAttribute("attribute.name.hamstercore.shield", 0.0D, 0.0D, 10000.0D).setSyncable(true));

    // 衍生属性
    public static final RegistryObject<net.minecraft.world.entity.ai.attributes.Attribute> REGEN_RATE = ATTRIBUTES.register("regen_rate", 
        () -> new RangedAttribute("attribute.name.hamstercore.regen_rate", 0.0D, 0.0D, 10000.0D).setSyncable(true));

    public static final RegistryObject<net.minecraft.world.entity.ai.attributes.Attribute> REGEN_DELAY = ATTRIBUTES.register("regen_delay", 
        () -> new RangedAttribute("attribute.name.hamstercore.regen_delay", 0.0D, 0.0D, 10000.0D).setSyncable(true));
        
    public static final RegistryObject<net.minecraft.world.entity.ai.attributes.Attribute> IMMUNITY_TIME = ATTRIBUTES.register("immunity_time", 
        () -> new RangedAttribute("attribute.name.hamstercore.immunity_time", 0.0D, 0.0D, 10000.0D).setSyncable(true));
}