package com.xlxyvergil.hamstercore.content.capability.entity;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public class EntityShieldCapability implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation(HamsterCore.MODID, "entity_shield");
    
    public static final Capability<EntityShieldCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    
    private float currentShield = 0.0f;
    private float maxShield = 0.0f;
    private long lastHurtTime = 0;
    private boolean gatingActive = false;
    private int gatingDuration = 0;
    private float regenRate = 0.0f;          // 护盾恢复速率
    private int regenDelay = 0;              // 护盾未耗尽时的恢复延迟
    private int regenDelayDepleted = 0;      // 护盾耗尽时的恢复延迟
    private int immunityTime = 0;            // 护盾保险时间
    private boolean insuranceAvailable = true; // 护盾保险是否可用
    private EntityType<?> entityType; // 实体类型，用于可能的未来扩展
    
    public float getCurrentShield() {
        return currentShield;
    }

    public void setCurrentShield(float currentShield) {
        this.currentShield = currentShield;
    }

    public float getMaxShield() {
        return maxShield;
    }

    public void setMaxShield(float maxShield) {
        this.maxShield = maxShield;
    }

    public long getLastHurtTime() {
        return lastHurtTime;
    }

    public void setLastHurtTime(long lastHurtTime) {
        this.lastHurtTime = lastHurtTime;
    }

    public boolean isGatingActive() {
        return gatingActive;
    }

    public void setGatingActive(boolean gatingActive) {
        this.gatingActive = gatingActive;
    }

    public int getGatingDuration() {
        return gatingDuration;
    }

    public void setGatingDuration(int gatingDuration) {
        this.gatingDuration = gatingDuration;
    }
    
    public float getRegenRate() {
        return regenRate;
    }
    
    public void setRegenRate(float regenRate) {
        this.regenRate = regenRate;
    }
    
    public int getRegenDelay() {
        return regenDelay;
    }
    
    public void setRegenDelay(int regenDelay) {
        this.regenDelay = regenDelay;
    }
    
    public int getRegenDelayDepleted() {
        return regenDelayDepleted;
    }
    
    public void setRegenDelayDepleted(int regenDelayDepleted) {
        this.regenDelayDepleted = regenDelayDepleted;
    }
    
    public int getImmunityTime() {
        return immunityTime;
    }
    
    public void setImmunityTime(int immunityTime) {
        this.immunityTime = immunityTime;
    }
    
    public boolean isInsuranceAvailable() {
        return insuranceAvailable;
    }
    
    public void setInsuranceAvailable(boolean insuranceAvailable) {
        this.insuranceAvailable = insuranceAvailable;
    }
    
    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("currentShield", currentShield);
        tag.putFloat("maxShield", maxShield);
        tag.putLong("lastHurtTime", lastHurtTime);
        tag.putBoolean("gatingActive", gatingActive);
        tag.putInt("gatingDuration", gatingDuration);
        tag.putFloat("regenRate", regenRate);
        tag.putInt("regenDelay", regenDelay);
        tag.putInt("regenDelayDepleted", regenDelayDepleted);
        tag.putInt("immunityTime", immunityTime);
        tag.putBoolean("insuranceAvailable", insuranceAvailable);
        return tag;
    }
    @Override
    public void deserializeNBT(CompoundTag tag) {
        currentShield = tag.getFloat("currentShield");
        maxShield = tag.getFloat("maxShield");
        lastHurtTime = tag.getLong("lastHurtTime");
        gatingActive = tag.getBoolean("gatingActive");
        gatingDuration = tag.getInt("gatingDuration");
        regenRate = tag.getFloat("regenRate");
        regenDelay = tag.getInt("regenDelay");
        regenDelayDepleted = tag.getInt("regenDelayDepleted");
        immunityTime = tag.getInt("immunityTime");
        insuranceAvailable = tag.getBoolean("insuranceAvailable");
    }
}