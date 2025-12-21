package com.xlxyvergil.hamstercore.content.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerLevelCapabilityProvider implements ICapabilityProvider {
    public static final Capability<PlayerLevelCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    
    private PlayerLevelCapability capability = null;
    private final LazyOptional<PlayerLevelCapability> lazyCapability = LazyOptional.of(this::createCapability);
    
    private PlayerLevelCapability createCapability() {
        if (capability == null) {
            capability = new PlayerLevelCapability();
        }
        return capability;
    }
    
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == CAPABILITY) {
            return lazyCapability.cast();
        }
        return LazyOptional.empty();
    }
    
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return getCapability(cap, null);
    }
}