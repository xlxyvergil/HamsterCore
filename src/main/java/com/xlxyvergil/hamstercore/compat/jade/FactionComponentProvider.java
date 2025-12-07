package com.xlxyvergil.hamstercore.compat.jade;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.faction.Faction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class FactionComponentProvider implements IEntityComponentProvider {
    
    public static final ResourceLocation ID = new ResourceLocation(HamsterCore.MODID, "faction");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(factionCap -> {
                Faction faction = factionCap.getFaction();
                if (faction != null && faction != Faction.NONE) {
                    // 获取实体等级
                    int[] levelHolder = {1};
                    livingEntity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(levelCap -> {
                        levelHolder[0] = levelCap.getLevel();
                    });
                    
                    // 获取实体护甲值
                    double[] armorHolder = {0};
                    livingEntity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(armorCap -> {
                        armorHolder[0] = armorCap.getArmor();
                    });
                    
                    // 创建等级组件
                    MutableComponent levelComponent = Component.literal("Lv." + levelHolder[0] + " ");
                    
                    // 使用本地化的前缀和派系名称
                    Component factionPrefix = Component.translatable("hamstercore.hud.faction_prefix");
                    Component factionName = Component.translatable("hamstercore.faction." + faction.name().toLowerCase());
                    
                    // 创建护甲值组件
                    Component armorComponent = Component.literal(" (" + String.format("%.1f", armorHolder[0]) + ")");
                    
                    // 组合等级、前缀、派系名称和护甲值
                    Component fullComponent = levelComponent.append(factionPrefix).append(factionName).append(armorComponent);
                    tooltip.add(fullComponent);
                }
            });
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}