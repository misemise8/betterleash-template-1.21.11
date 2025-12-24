package net.misemise;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.misemise.keybind.KeyBindings;

public class BetterLeashClient implements ClientModInitializer {
    private static double clientMaxDistance = 50.0;
    private static double clientPullStrength = 0.5;

    @Override
    public void onInitializeClient() {
        // ペイロードの登録
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfigUpdatePayload.ID, ConfigUpdatePayload.CODEC);

        // キーバインド登録
        KeyBindings.register();

        // サーバーから設定を受信
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                clientMaxDistance = payload.maxDistance();
                clientPullStrength = payload.pullStrength();
                BetterLeash.LOGGER.info("サーバーから設定を受信: 最大距離={}, 引き寄せ強度={}",
                        payload.maxDistance(), payload.pullStrength());
            });
        });

        // キー押下で設定画面を開く
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (KeyBindings.wasOpenConfigPressed()) {
                openConfigScreen(client);
            }
        });
    }

    private void openConfigScreen(MinecraftClient client) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(client.currentScreen)
                .setTitle(Text.translatable("config.betterleash.title"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.betterleash.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 最大リード距離の設定
        general.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("config.betterleash.max_distance"), clientMaxDistance)
                .setDefaultValue(50.0)
                .setMin(10.0)
                .setMax(200.0)
                .setTooltip(Text.translatable("config.betterleash.max_distance.tooltip"))
                .setSaveConsumer(value -> clientMaxDistance = value)
                .build());

        // 引き寄せ強度の設定
        general.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("config.betterleash.pull_strength"), clientPullStrength)
                .setDefaultValue(0.5)
                .setMin(0.1)
                .setMax(2.0)
                .setTooltip(Text.translatable("config.betterleash.pull_strength.tooltip"))
                .setSaveConsumer(value -> clientPullStrength = value)
                .build());

        builder.setSavingRunnable(() -> {
            // 設定をサーバーに送信
            ClientPlayNetworking.send(new ConfigUpdatePayload(clientMaxDistance, clientPullStrength));
        });

        client.setScreen(builder.build());
    }
}