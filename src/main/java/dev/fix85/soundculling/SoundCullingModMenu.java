package dev.fix85.soundculling;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SoundCullingModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("title.soundculling.config"));

            ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.soundculling.general"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // Master switch to block all sounds
            general.addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("option.soundculling.block_all"),
                    Config.get().blockAll
            )
                    .setDefaultValue(false)
                    .setTooltip(Component.translatable("option.soundculling.block_all.tooltip"))
                    .setSaveConsumer(newValue -> Config.get().blockAll = newValue)
                    .build());

            general.addEntry(entryBuilder.startTextDescription(Component.literal("§7--------------------------------------§r")).build());

            List<Map.Entry<ResourceKey<SoundEvent>, SoundEvent>> sounds = new ArrayList<>(
                    BuiltInRegistries.SOUND_EVENT.entrySet()
            );
            sounds.sort(Comparator.comparing(entry -> entry.getKey().identifier().getPath()));

            // Individual sound toggles (logically overridden when blockAll is active)
            for (Map.Entry<ResourceKey<SoundEvent>, SoundEvent> entry : sounds) {
                String idStr = entry.getKey().identifier().toString();
                String path = entry.getKey().identifier().getPath();

                String name = path.replace('_', ' ').replace('.', ' ');
                if (!name.isEmpty()) {
                    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                }

                general.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal(name),
                        Config.get().soundStates.getOrDefault(idStr, false)
                )
                        .setDefaultValue(false)
                        .setTooltip(Component.literal("Block sound: " + idStr))
                        .setSaveConsumer(newValue -> Config.get().setSoundBlocked(idStr, newValue))
                        .build());
            }

            builder.setSavingRunnable(Config::save);
            return builder.build();
        };
    }
}
