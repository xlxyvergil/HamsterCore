package com.xlxyvergil.hamstercore.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    public static final ForgeConfigSpec.BooleanValue SHOW_ENTITY_SHIELD_BAR;
    
    static {
        BUILDER.push("Client Settings");
        
        SHOW_ENTITY_SHIELD_BAR = BUILDER
                .comment("是否在实体头顶显示护盾条")
                .define("showEntityShieldBar", false);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}