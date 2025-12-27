package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.content.capability.EntityCapabilityAttacher;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EntityEffectEvents {

    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        // 当实体获得新的状态效果时，同步到客户端
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // 同步实体的状态效果到客户端
        EntityCapabilityAttacher.syncEntityEffects(event.getEntity());
    }

    @SubscribeEvent
    public static void onMobEffectRemoved(MobEffectEvent.Remove event) {
        // 当实体失去状态效果时，同步到客户端
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // 同步实体的状态效果到客户端
        EntityCapabilityAttacher.syncEntityEffects(event.getEntity());
    }

    @SubscribeEvent
    public static void onMobEffectExpired(MobEffectEvent.Expired event) {
        // 当实体的状态效果过期时，同步到客户端
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // 同步实体的状态效果到客户端
        EntityCapabilityAttacher.syncEntityEffects(event.getEntity());
    }
}