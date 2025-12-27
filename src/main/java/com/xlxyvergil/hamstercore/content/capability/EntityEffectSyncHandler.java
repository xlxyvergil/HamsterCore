package com.xlxyvergil.hamstercore.content.capability;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityEffectManager;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import com.xlxyvergil.hamstercore.network.SyncEntityEffectsPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class EntityEffectSyncHandler {

    /**
     * 同步实体状态效果到客户端
     */
    public static void syncEntityEffects(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }

        // 获取实体的状态效果
        List<MobEffectInstance> effects = new ArrayList<>(entity.getActiveEffects());
        
        // 更新本地缓存
        EntityEffectManager.updateEntityEffects(entity);
        
        // 发送网络包到追踪此实体的客户端
        SyncEntityEffectsPacket packet = new SyncEntityEffectsPacket(entity.getId(), effects);
        PacketHandler.NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }
}