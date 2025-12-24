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
                
                // 主动保存玩家等级数据到存档
                savePlayerLevelData(player);
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
                
                // 保存玩家等级数据到存档
                savePlayerLevelData(player);
                
                // 等级提升后回满生命值和护盾值
                restorePlayerHealthAndShield(player);
                
                // 继续检查是否还能升级（一次获得大量经验的情况）
                checkLevelUp(player, cap);
            }
        }
    }
    
    // 主动保存玩家等级数据到存档
    private static void savePlayerLevelData(Player player) {
        // 手动触发玩家数据保存
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            // 获取玩家存档目录
            java.io.File playerDataDir = serverPlayer.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_DATA_DIR).toFile();
            // 在玩家数据目录下创建自定义等级数据文件
            java.io.File customDir = new java.io.File(playerDataDir, "hamstercore");
            if (!customDir.exists()) {
                customDir.mkdirs();
            }
            java.io.File levelFile = new java.io.File(customDir, serverPlayer.getStringUUID() + "_hamstercore_player_level.dat");
            
            // 获取玩家能力数据并保存
            player.getCapability(com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                try {
                    net.minecraft.nbt.CompoundTag data = cap.serializeNBT();
                    net.minecraft.nbt.NbtIo.writeCompressed(data, levelFile);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
    // 等级提升后回满玩家的生命值和护盾值
    private static void restorePlayerHealthAndShield(Player player) {
        // 回满生命值到最大值
        player.setHealth(player.getMaxHealth());
        
        // 回满护盾值到最大值
        player.getCapability(com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
            shieldCap.setCurrentShield(shieldCap.getMaxShield());
            
            // 同步护盾数据到客户端
            com.xlxyvergil.hamstercore.network.EntityShieldSyncToClient.sync(player);
        });
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