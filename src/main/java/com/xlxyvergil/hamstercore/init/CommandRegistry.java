package com.xlxyvergil.hamstercore.init;

import com.xlxyvergil.hamstercore.command.AffixCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public final class CommandRegistry {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        AffixCommand.register(event.getDispatcher());
    }
}