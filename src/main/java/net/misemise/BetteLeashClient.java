package net.misemise;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class BetterLeashClient implements ClientModInitializer {
    private static double clientMaxDistance = 50.0;
    private static double clientPullStrength = 0.5;

    private static KeyBinding configKeyBinding;

    @Override
    public void onInitializeClient() {
        // キーバインド登録（デフォルト: K）
        configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterleash.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.betterleash"
        ));

        // サーバーから設定を受信
        ClientPlayNetworking.registerGlobalReceiver(BetterLeash.CONFIG_SYNC_PACKET, (client, handler, buf, responseSender) -> {
            double maxDistance = buf.readDouble();
            double pullStrength = buf.readDouble();

            client.execute(() -> {
                clientMaxDistance = maxDistance;
                clientPullStrength = pullStrength;
                BetterLeash.LOGGER.info("サーバーから設定を受信: 最大距離={}, 引き寄せ強度={}",
                        maxDistance, pullStrength);
            });
        });

        // キー押下で設定画面を開く
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKeyBinding.wasPressed()) {
                openConfigScreen(client);
            }
        });

        // サーバー接続時に設定をリクエスト
        ClientPlayNetworking.registerGlobalReceiver(BetterLeash.CONFIG_SYNC_PACKET, (client, handler, buf, responseSender) -> {
            double maxDistance = buf.readDouble();
            double pullStrength = buf.readDouble();

            client.execute(() -> {
                clientMaxDistance = maxDistance;
                clientPullStrength = pullStrength;
            });
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
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(clientMaxDistance);
            buf.writeDouble(clientPullStrength);
            ClientPlayNetworking.send(BetterLeash.CONFIG_UPDATE_PACKET, buf);
        });

        client.setScreen(builder.build());
    }
}