package net.misemise;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterLeash implements ModInitializer {
	public static final String MOD_ID = "betterleash";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// ネットワーク通信用のID
	public static final Identifier CONFIG_SYNC_PACKET = Identifier.of(MOD_ID, "config_sync");
	public static final Identifier CONFIG_REQUEST_PACKET = Identifier.of(MOD_ID, "config_request");
	public static final Identifier CONFIG_UPDATE_PACKET = Identifier.of(MOD_ID, "config_update");

	@Override
	public void onInitialize() {
		LOGGER.info("BetterLeash MODを読み込み中...");

		// 設定ファイルの読み込み
		LeashConfig.load();

		// プレイヤー接続時に設定を同期
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			syncConfigToClient(handler.player);
		});

		// クライアントからの設定リクエストを処理
		ServerPlayNetworking.registerGlobalReceiver(CONFIG_REQUEST_PACKET, (server, player, handler, buf, responseSender) -> {
			syncConfigToClient(player);
		});

		// クライアントからの設定更新を処理
		ServerPlayNetworking.registerGlobalReceiver(CONFIG_UPDATE_PACKET, (server, player, handler, buf, responseSender) -> {
			// プレイヤーがOPかチェック
			if (player.hasPermissionLevel(2)) {
				double maxDistance = buf.readDouble();
				double pullStrength = buf.readDouble();

				server.execute(() -> {
					LeashConfig.maxLeashDistance = maxDistance;
					LeashConfig.pullStrength = pullStrength;
					LeashConfig.save();

					// 全プレイヤーに設定を同期
					for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
						syncConfigToClient(p);
					}

					LOGGER.info("設定が更新されました: 最大距離={}, 引き寄せ強度={}", maxDistance, pullStrength);
				});
			}
		});

		LOGGER.info("BetterLeash MODの読み込みが完了しました");
	}

	private void syncConfigToClient(ServerPlayerEntity player) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeDouble(LeashConfig.maxLeashDistance);
		buf.writeDouble(LeashConfig.pullStrength);
		ServerPlayNetworking.send(player, CONFIG_SYNC_PACKET, buf);
	}
}