package com.xlxyvergil.hamstercore.content.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerLevelCapability implements INBTSerializable<CompoundTag> {
    private int playerlevel = 0;     // 当前玩家等级，默认为1级
    private int experience = 0;      // 当前经验值
    
    // Getters and setters
    public int getPlayerLevel() { return playerlevel; }
    public void setPlayerLevel(int playerlevel) { 
        this.playerlevel = Math.min(playerlevel, 30); // 等级最高为30级
    }
    public int getExperience() { 
        // 当玩家达到最高等级时，不再存储经验
        return playerlevel >= 30 ? getTotalExperienceForLevel(30) : experience; 
    }
    public void setExperience(int experience) { 
        // 当玩家达到最高等级时，不再存储经验
        if (playerlevel < 30) {
            this.experience = experience;
        }
    }
    
    // 计算升级到下一级所需的经验值（相对于当前等级）
    public int getNextLevelExperience() {
        if (playerlevel >= 30) return 0; // 已达到最高等级
        return 1000 * (2 * playerlevel + 1); // 1000 × (2 × 玩家等级 + 1)
    }
    
    // 计算总经验值需求（到达某等级需要的总经验）
    public int getTotalExperienceForLevel(int targetLevel) {
        int total = 0;
        for (int i = 0; i < targetLevel && i < 30; i++) { // 最多计算到30级
            total += 1000 * (2 * i + 1);
        }
        return total;
    }
    
    // 计算当前等级已获得的经验值
    public int getCurrentLevelExperience() {
        if (playerlevel <= 0) return getExperience();
        // 当达到最高等级时，经验条应显示为满
        if (playerlevel >= 30) return getNextLevelExperience();
        return getExperience() - getTotalExperienceForLevel(playerlevel);
    }
    
    // 计算当前等级升级所需的经验值
    public int getExperienceToNextLevel() {
        if (playerlevel >= 30) return 0; // 已达到最高等级
        return getNextLevelExperience();
    }
    
    // INBTSerializable实现
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("PlayerLevel", playerlevel);
        // 当玩家达到最高等级时，存储的经验值为达到30级所需的经验值
        tag.putInt("Experience", getExperience());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        playerlevel = tag.getInt("PlayerLevel");
        experience = tag.getInt("Experience");
    }
}