package com.xlxyvergil.hamstercore.level;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class PlayerLevelUpEvent extends Event {
    private final Player player;
    private final int playerLevel;
    
    public PlayerLevelUpEvent(Player player, int playerLevel) {
        this.player = player;
        this.playerLevel = playerLevel;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public int getPlayerLevel() {
        return playerLevel;
    }
}