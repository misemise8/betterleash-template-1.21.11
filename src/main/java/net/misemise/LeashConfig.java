package net.misemise;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LeashConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "betterleash.json"
    );

    // デフォルト値
    public static double maxLeashDistance = 50.0;
    public static double pullStrength = 0.5;

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    maxLeashDistance = data.maxLeashDistance;
                    pullStrength = data.pullStrength;
                }
                BetterLeash.LOGGER.info("設定を読み込みました: 最大距離={}, 引き寄せ強度={}",
                        maxLeashDistance, pullStrength);
            } catch (IOException e) {
                BetterLeash.LOGGER.error("設定ファイルの読み込みに失敗しました", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            ConfigData data = new ConfigData();
            data.maxLeashDistance = maxLeashDistance;
            data.pullStrength = pullStrength;
            GSON.toJson(data, writer);
            BetterLeash.LOGGER.info("設定を保存しました");
        } catch (IOException e) {
            BetterLeash.LOGGER.error("設定ファイルの保存に失敗しました", e);
        }
    }

    private static class ConfigData {
        double maxLeashDistance = 50.0;
        double pullStrength = 0.5;
    }
}