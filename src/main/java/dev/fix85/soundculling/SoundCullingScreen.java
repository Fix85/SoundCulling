package dev.fix85.soundculling;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
        // Localized, human-readable name (sound subtitle if available, otherwise a prettified id)
        final String displayName;
        // Lower-cased haystack used for the search box (id + display name)
        final String searchKey;
        boolean blocked;
        final SoundEvent soundEvent;

        SoundEntryData(String idStr, String displayName, boolean blocked, SoundEvent soundEvent) {
            this.idStr = idStr;
            this.displayName = displayName;
            this.blocked = blocked;
            this.soundEvent = soundEvent;
            this.searchKey = (idStr + ' ' + displayName).toLowerCase(Locale.ROOT);
        }
    }

    public SoundCullingScreen(Screen parent) {
        super(Component.translatable("title.soundculling.config"));
        this.parent = parent;
        this.tempBlockAll = Config.get().blockAll;

        // The sound manager lets us resolve localized subtitles (respects the active language).
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        // Fetch and cache all registered sounds
        List<Map.Entry<net.minecraft.resources.ResourceKey<SoundEvent>, SoundEvent>> registryEntries = new ArrayList<>(
                BuiltInRegistries.SOUND_EVENT.entrySet()
        );
        registryEntries.sort(Comparator.comparing(entry -> entry.getKey().identifier().getPath()));

        for (Map.Entry<net.minecraft.resources.ResourceKey<SoundEvent>, SoundEvent> entry : registryEntries) {
            Identifier location = entry.getKey().identifier();
            String idStr = location.toString();

            String displayName = resolveDisplayName(soundManager, location);
            boolean isBlocked = Config.get().soundStates.getOrDefault(idStr, false);
            allSounds.add(new SoundEntryData(idStr, displayName, isBlocked, entry.getValue()));
        }
    }

    // Prefer the sound's localized subtitle; fall back to a prettified version of the id path.
    private static String resolveDisplayName(SoundManager soundManager, Identifier location) {
        WeighedSoundEvents soundEvent = soundManager.getSoundEvent(location);
        if (soundEvent != null) {
            Component subtitle = soundEvent.getSubtitle();
            if (subtitle != null) {
                String localized = subtitle.getString();
                if (localized != null && !localized.isEmpty()) {
                    return localized;
                }
            }
        }

        String name = location.getPath().replace('_', ' ').replace('.', ' ');
        if (!name.isEmpty()) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        // Search Box
        searchBox = new EditBox(this.font, cx - 110, 22, 220, 18, Component.translatable("soundculling.search.hint"));
        searchBox.setHint(Component.translatable("soundculling.search.hint"));
        searchBox.setResponder(text -> filterSounds());
        addRenderableWidget(searchBox);

        // Sound List - increased height per entry to 30 for 2-line layout
        int listHeight = this.height - 45 - 40;
        soundList = new SoundList(this.minecraft, this.width, listHeight, 45, 30);
        addRenderableWidget(soundList);

        // Bottom action bar: lay buttons out centered so longer (e.g. localized) labels still fit.
        int btnY = this.height - 32;
        int gap = 6;
        int wBlock = 80, wUnblock = 95, wMaster = 110, wSave = 75, wCancel = 65;
        int totalWidth = wBlock + wUnblock + wMaster + wSave + wCancel + gap * 4;
        int x = cx - totalWidth / 2;

        blockAllButton = Button.builder(Component.translatable("soundculling.button.block_all"), b -> setAllBlockedStates(true))
                .bounds(x, btnY, wBlock, 20)
                .build();
        addRenderableWidget(blockAllButton);
        x += wBlock + gap;

        unblockAllButton = Button.builder(Component.translatable("soundculling.button.unblock_all"), b -> setAllBlockedStates(false))
                .bounds(x, btnY, wUnblock, 20)
                .build();
        addRenderableWidget(unblockAllButton);
        x += wUnblock + gap;

        toggleMasterButton = Button.builder(getMasterButtonText(), b -> {
                    tempBlockAll = !tempBlockAll;
                    b.setMessage(getMasterButtonText());
                })
                .bounds(x, btnY, wMaster, 20)
                .build();
        addRenderableWidget(toggleMasterButton);
        x += wMaster + gap;

        saveButton = Button.builder(Component.translatable("soundculling.button.save"), b -> {
                    Config.get().blockAll = tempBlockAll;
                    for (SoundEntryData data : allSounds) {
                        Config.get().setSoundBlocked(data.idStr, data.blocked);
                    }
                    Config.save();
                    onClose();
                })
                .bounds(x, btnY, wSave, 20)
                .build();
        addRenderableWidget(saveButton);
        x += wSave + gap;

        cancelButton = Button.builder(Component.translatable("soundculling.button.cancel"), b -> onClose())
                .bounds(x, btnY, wCancel, 20)
                .build();
        addRenderableWidget(cancelButton);

        filterSounds();
    }

    private Component getMasterButtonText() {
        Component state = Component.translatable(tempBlockAll ? "soundculling.state.on" : "soundculling.state.off")
                .withStyle(tempBlockAll ? ChatFormatting.GREEN : ChatFormatting.RED);
        return Component.translatable("soundculling.button.master", state);
    }

    private void setAllBlockedStates(boolean blocked) {
        String query = searchBox.getValue().toLowerCase(Locale.ROOT);
        for (SoundEntryData data : allSounds) {
            if (query.isEmpty() || data.searchKey.contains(query)) {
                data.blocked = blocked;
            }
        }
        filterSounds();
    }

    private void filterSounds() {
        String query = searchBox.getValue().toLowerCase(Locale.ROOT);
        soundList.clearEntries();
        for (SoundEntryData data : allSounds) {
            if (query.isEmpty() || data.searchKey.contains(query)) {
                soundList.addEntry(new SoundListEntry(data));
            }
        }
        // Reset scroll position when filter changes to avoid empty list views
        soundList.setScrollAmount(0);
        soundList.updateSizeAndPosition(soundList.getWidth(), soundList.getHeight(), soundList.getY());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        // 26.x deferred render: widgets/background are extracted by super; we just add the title.
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
        extractor.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private class SoundList extends ContainerObjectSelectionList<SoundListEntry> {
        public SoundList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
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
        protected int scrollBarX() {
            return this.getX() + this.width / 2 + this.getRowWidth() / 2 + 15;
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
                    .bounds(0, 0, 90, 20)
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
            return data.blocked
                    ? Component.translatable("soundculling.state.blocked").withStyle(ChatFormatting.RED)
                    : Component.translatable("soundculling.state.allowed").withStyle(ChatFormatting.GREEN);
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
        public void extractContent(GuiGraphicsExtractor extractor, int mouseX, int mouseY, boolean hovering, float partialTick) {
            // 26.x deferred render: the two ints are mouseX / mouseY, not index / top.
            // Position via the entry's own geometry so the row contents stay on the row.
            int left = getX();
            int top = getY();
            int width = getWidth();

            int playX = left + width - 114;
            int textRight = playX - 4;

            // 2-line layout: localized display name on top, raw id below it (drawn with shadow).
            extractor.text(SoundCullingScreen.this.font,
                    clip(data.displayName, textRight - (left + 4)), left + 4, top + 4, 0xFFFFFFFF, true);
            extractor.text(SoundCullingScreen.this.font,
                    clip(data.idStr, textRight - (left + 4)), left + 4, top + 16, 0xFF888888, true);

            this.playBtn.setX(playX);
            this.playBtn.setY(top + 5);
            this.playBtn.extractRenderState(extractor, mouseX, mouseY, partialTick);

            this.toggleBtn.setX(left + width - 90);
            this.toggleBtn.setY(top + 5);
            this.toggleBtn.extractRenderState(extractor, mouseX, mouseY, partialTick);
        }

        // Trim a string with an ellipsis so it fits within maxWidth pixels.
        private String clip(String text, int maxWidth) {
            var font = SoundCullingScreen.this.font;
            if (font.width(text) <= maxWidth) {
                return text;
            }
            return font.plainSubstrByWidth(text, maxWidth - font.width("...")) + "...";
        }
    }
}
