package dev.fix85.soundculling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SoundCullingScreen extends Screen {
    private final Screen parent;
    private EditBox searchBox;
    private SoundList soundList;
    private Button saveButton;
    private Button cancelButton;
    private Button blockAllButton;
    private Button unblockAllButton;
    private Button toggleMasterButton;

    // Temporary list of sound settings that will be committed to Config on Save
    private final List<SoundEntryData> allSounds = new ArrayList<>();
    private boolean tempBlockAll;

    private static class SoundEntryData {
        final String idStr;
        final String displayName;
        boolean blocked;
        final SoundEvent soundEvent;

        SoundEntryData(String idStr, String displayName, boolean blocked, SoundEvent soundEvent) {
            this.idStr = idStr;
            this.displayName = displayName;
            this.blocked = blocked;
            this.soundEvent = soundEvent;
        }
    }

    public SoundCullingScreen(Screen parent) {
        super(Component.translatable("title.soundculling.config"));
        this.parent = parent;
        this.tempBlockAll = Config.get().blockAll;

        // Fetch and cache all registered sounds
        List<Map.Entry<net.minecraft.resources.ResourceKey<SoundEvent>, SoundEvent>> registryEntries = new ArrayList<>(
                BuiltInRegistries.SOUND_EVENT.entrySet()
        );
        registryEntries.sort(Comparator.comparing(entry -> entry.getKey().location().getPath()));

        for (Map.Entry<net.minecraft.resources.ResourceKey<SoundEvent>, SoundEvent> entry : registryEntries) {
            String idStr = entry.getKey().location().toString();
            String path = entry.getKey().location().getPath();

            String name = path.replace('_', ' ').replace('.', ' ');
            if (!name.isEmpty()) {
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
            boolean isBlocked = Config.get().soundStates.getOrDefault(idStr, false);
            allSounds.add(new SoundEntryData(idStr, name, isBlocked, entry.getValue()));
        }
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        // Search Box
        searchBox = new EditBox(this.font, cx - 110, 22, 220, 18, Component.literal("Search"));
        searchBox.setResponder(text -> filterSounds());
        addRenderableWidget(searchBox);

        // Sound List - increased height per entry to 30 for 2-line layout.
        // 1.20.1 uses the (mc, width, height, top, bottom, itemHeight) constructor.
        soundList = new SoundList(this.minecraft, this.width, this.height, 45, this.height - 40, 30);
        addRenderableWidget(soundList);

        // Action Buttons at the bottom - positioned to avoid overlapping
        blockAllButton = Button.builder(Component.literal("Block All"), b -> setAllBlockedStates(true))
                .bounds(cx - 200, this.height - 32, 72, 20)
                .build();
        addRenderableWidget(blockAllButton);

        unblockAllButton = Button.builder(Component.literal("Unblock All"), b -> setAllBlockedStates(false))
                .bounds(cx - 124, this.height - 32, 72, 20)
                .build();
        addRenderableWidget(unblockAllButton);

        toggleMasterButton = Button.builder(getMasterButtonText(), b -> {
                    tempBlockAll = !tempBlockAll;
                    b.setMessage(getMasterButtonText());
                })
                .bounds(cx - 48, this.height - 32, 100, 20)
                .build();
        addRenderableWidget(toggleMasterButton);

        saveButton = Button.builder(Component.literal("Save"), b -> {
                    Config.get().blockAll = tempBlockAll;
                    for (SoundEntryData data : allSounds) {
                        Config.get().setSoundBlocked(data.idStr, data.blocked);
                    }
                    Config.save();
                    onClose();
                })
                .bounds(cx + 56, this.height - 32, 70, 20)
                .build();
        addRenderableWidget(saveButton);

        cancelButton = Button.builder(Component.literal("Cancel"), b -> onClose())
                .bounds(cx + 130, this.height - 32, 70, 20)
                .build();
        addRenderableWidget(cancelButton);

        filterSounds();
    }

    private Component getMasterButtonText() {
        String state = tempBlockAll ? "§aON" : "§cOFF";
        return Component.literal("Block All: " + state);
    }

    private void setAllBlockedStates(boolean blocked) {
        String query = searchBox.getValue().toLowerCase(Locale.ROOT);
        for (SoundEntryData data : allSounds) {
            if (query.isEmpty() || data.idStr.toLowerCase(Locale.ROOT).contains(query) || data.displayName.toLowerCase(Locale.ROOT).contains(query)) {
                data.blocked = blocked;
            }
        }
        filterSounds();
    }

    private void filterSounds() {
        String query = searchBox.getValue().toLowerCase(Locale.ROOT);
        soundList.clearEntries();
        for (SoundEntryData data : allSounds) {
            if (query.isEmpty() || data.idStr.toLowerCase(Locale.ROOT).contains(query) || data.displayName.toLowerCase(Locale.ROOT).contains(query)) {
                soundList.addEntry(new SoundListEntry(data));
            }
        }
        // Reset scroll position when filter changes to avoid empty list views
        soundList.setScrollAmount(0);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw the dark background for the screen (single-arg form in 1.20.1)
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private class SoundList extends ContainerObjectSelectionList<SoundListEntry> {
        public SoundList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, bottom, itemHeight);
        }

        public void clearEntries() {
            super.clearEntries();
        }

        @Override
        public int addEntry(SoundListEntry entry) {
            return super.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return 320;
        }

        @Override
        protected int getScrollbarPosition() {
            // The list spans the full screen width (left edge at 0), so the rows are
            // centred on width/2. Place the scrollbar just right of the rows.
            return this.width / 2 + this.getRowWidth() / 2 + 15;
        }
    }

    private class SoundListEntry extends ContainerObjectSelectionList.Entry<SoundListEntry> {
        private final SoundEntryData data;
        private final Button toggleBtn;
        private final Button playBtn;
        private final List<GuiEventListener> children = new ArrayList<>();

        public SoundListEntry(SoundEntryData data) {
            this.data = data;

            this.toggleBtn = Button.builder(getToggleText(), b -> {
                        data.blocked = !data.blocked;
                        b.setMessage(getToggleText());
                    })
                    .bounds(0, 0, 75, 20)
                    .build();

            this.playBtn = Button.builder(Component.literal("▶"), b -> {
                        if (SoundCullingScreen.this.minecraft != null) {
                            SoundCullingScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(data.soundEvent, 1.0F));
                        }
                    })
                    .bounds(0, 0, 20, 20)
                    .build();

            this.children.add(toggleBtn);
            this.children.add(playBtn);
        }

        private Component getToggleText() {
            return data.blocked ? Component.literal("§cBlocked") : Component.literal("§aAllowed");
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(toggleBtn, playBtn);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            // 2-line rendering: Display Name on top, raw ID below it (drawn with shadow)
            guiGraphics.drawString(SoundCullingScreen.this.font, data.displayName, left + 4, top + 2, 0xFFFFFF, true);
            
            String subText = data.idStr;
            if (subText.length() > 38) {
                subText = subText.substring(0, 35) + "...";
            }
            guiGraphics.drawString(SoundCullingScreen.this.font, "§8" + subText, left + 4, top + 14, 0x888888, true);

            this.playBtn.setX(left + width - 100);
            this.playBtn.setY(top + 5);
            this.playBtn.render(guiGraphics, mouseX, mouseY, partialTick);

            this.toggleBtn.setX(left + width - 75);
            this.toggleBtn.setY(top + 5);
            this.toggleBtn.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
}
