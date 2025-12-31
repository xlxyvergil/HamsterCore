package com.xlxyvergil.hamstercore.modification;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "hamstercore")
public class ModificationRegistry extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIRECTORY = "modifications";

    private static ModificationRegistry INSTANCE;
    private final Map<ResourceLocation, Modification> modifications = new HashMap<>();

    public ModificationRegistry() {
        super(GSON, DIRECTORY);
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        INSTANCE = new ModificationRegistry();
        event.addListener(INSTANCE);
    }

    public static ModificationRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.modifications.clear();
        map.forEach((id, json) -> {
            Modification.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> {})
                .ifPresent(mod -> this.modifications.put(id, mod));
        });
    }

    public Optional<Modification> getModification(ResourceLocation id) {
        return Optional.ofNullable(this.modifications.get(id));
    }

    public Map<ResourceLocation, Modification> getModifications() {
        return new HashMap<>(this.modifications);
    }
}
