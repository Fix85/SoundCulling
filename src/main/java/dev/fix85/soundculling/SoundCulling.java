package dev.fix85.soundculling;

import net.fabricmc.api.ClientModInitializer;

public class SoundCulling implements ClientModInitializer {
    public static final String MOD_ID = "soundculling";

    @Override
    public void onInitializeClient() {
        Config.load();
    }
}
