package com.xlxyvergil.hamstercore.level;

import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapability;
import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.network.PlayerLevelSyncToClient;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public class PlayerLevelManager {
    
    // 添加经验值
    public static void addExperience(Player player, int amount) {
        player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            // 当玩家达到最高等级时，不再增加经验
            if (cap.getPlayerLevel() < 30) {
                int newExperience = cap.getExperience() + amount;
                cap.setExperience(newExperience);
                
                // 检查是否升级
                checkLevelUp(player, cap);
                
                // 同步玩家等级数据到客户端
                PlayerLevelSyncToClient.sync(player);
            }
        });
    }
    
    // 检查并处理升级
    private static void checkLevelUp(Player player, PlayerLevelCapability cap) {
        int currentPlayerLevel = cap.getPlayerLevel();
        int currentExp = cap.getExperience();
        
        // 检查是否达到下一级的经验要求
        if (currentPlayerLevel < 30) { // 最高等级为30级
            int expNeeded = cap.getTotalExperienceForLevel(currentPlayerLevel + 1);
            if (currentExp >= expNeeded) {
                cap.setPlayerLevel(currentPlayerLevel + 1);
                // 发布自定义的等级升级事件
                MinecraftForge.EVENT_BUS.post(new PlayerLevelUpEvent(player, cap.getPlayerLevel()));
                
                // 继续检查是否还能升级（一次获得大量经验的情况）
                checkLevelUp(player, cap);
            }
        }
    }
    
    // 获取生命值加成 (从1级开始每级+1生命值)
    public static int getHealthBonus(int playerLevel) {
        return (playerLevel >= 1) ? playerLevel : 0;
    }
    
    // 获取护盾值加成 (从1级开始每级+20护盾值)
    public static int getShieldBonus(int playerLevel) {
        return (playerLevel >= 1) ? playerLevel * 20 : 0;
    }
    
    // 获取基础护甲加成 (从1级开始每级+20基础护甲)
    public static int getBaseArmorBonus(int playerLevel) {
        return (playerLevel >= 1) ? playerLevel * 20 : 0;
    }
}