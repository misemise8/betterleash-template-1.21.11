package net.misemise;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterLeash implements ModInitializer {
	public static final String MOD_ID = "betterleash";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("BetterLeash MODを読み込み中...");

		// 設定ファイルの読み込み
		LeashConfig.load();

		// ペイロードの登録
		PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ConfigUpdatePayload.ID, ConfigUpdatePayload.CODEC);

		// プレイヤー接続時に設定を同期
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			syncConfigToClient(handler.player);
		});

		// クライアントからの設定更新を処理
		ServerPlayNetworking.registerGlobalReceiver(ConfigUpdatePayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();

			// プレイヤーがOPかチェック
			if (player.hasPermissionLevel(2)) {
				context.server().execute(() -> {
					LeashConfig.maxLeashDistance = payload.maxDistance();
					LeashConfig.pullStrength = payload.pullStrength();
					LeashConfig.save();

					// 全プレイヤーに設定を同期
					for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
						syncConfigToClient(p);
					}

					LOGGER.info("設定が更新されました: 最大距離={}, 引き寄せ強度={}",
							payload.maxDistance(), payload.pullStrength());
				});
			}
		});

		LOGGER.info("BetterLeash MODの読み込みが完了しました");
	}

	private void syncConfigToClient(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, new ConfigSyncPayload(
				LeashConfig.maxLeashDistance,
				LeashConfig.pullStrength
		));
	}
}