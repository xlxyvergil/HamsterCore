package com.xlxyvergil.hamstercore.content.capability.entity;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.FactionConfig;
import com.xlxyvergil.hamstercore.faction.Faction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public class EntityFactionCapability implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation(HamsterCore.MODID, "entity_faction");
    public static final Capability<EntityFactionCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private Faction faction = null;
    private EntityType<?> entityType = null;
    private static FactionConfig factionConfig = null;

    public Faction getFaction() {
        // 如果还没有分配派系，则根据实体类型获取派系
        if (faction == null && entityType != null) {
            // 初始化配置
            if (factionConfig == null) {
                factionConfig = FactionConfig.load();
            }
            
            // 使用配置文件中的设置
            if (factionConfig != null) {
                faction = factionConfig.getFactionForEntity(entityType);
            } else {
                // 返回默认派系
                faction = Faction.OROKIN;
            }
        }
        return faction != null ? faction : Faction.NONE;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }
    
    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (faction != null && faction != Faction.NONE) {
            tag.putString("Faction", faction.name());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Faction")) {
            try {
                faction = Faction.valueOf(tag.getString("Faction"));
            } catch (IllegalArgumentException e) {
                faction = Faction.NONE;
            }
        } else {
            faction = Faction.NONE;
        }
    }
}