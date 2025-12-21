package com.xlxyvergil.hamstercore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.xlxyvergil.hamstercore.content.capability.PlayerCapabilityAttacher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 护盾命令类
 * 用于重新初始化玩家的护盾能力
 */
public class ShieldCommand {
    
    /**
     * 注册命令
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 主命令 /hamstershield
        dispatcher.register(Commands.literal("hamstershield")
            .requires(source -> source.hasPermission(2)) // 需要权限等级2，管理员级别
            .then(Commands.literal("reinit")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> reinitializeShield(
                            context.getSource(),
                            EntityArgument.getPlayer(context, "player")
                    ))
                )
            )
        );
    }
    
    /**
     * 重新初始化护盾命令
     */
    private static int reinitializeShield(CommandSourceStack source, ServerPlayer player) {
        // 重新初始化玩家的护盾能力
        // 注意：这里我们需要获取玩家的等级来调用正确的方法
        player.getCapability(com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapabilityProvider.CAPABILITY)
            .ifPresent(levelCap -> {
                PlayerCapabilityAttacher.initializePlayerCapabilities(player, levelCap.getPlayerLevel());
            });
        
        source.sendSuccess(() -> Component.literal("成功为 " + player.getName().getString() + " 重新初始化了护盾能力"), true);
        
        return 1;
    }
}