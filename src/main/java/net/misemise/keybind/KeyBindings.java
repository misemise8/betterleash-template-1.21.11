package net.misemise.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.misemise.BetterLeash;
import org.lwjgl.glfw.GLFW;

/**
 * キーバインド管理クラス
 */
public class KeyBindings {
    public static KeyBinding openConfigKey;

    public static final KeyBinding.Category BETTERLEASH_CATEGORY = KeyBinding.Category.create(
            Identifier.of(BetterLeash.MOD_ID, "general")
    );

    public static void register() {
        // 設定画面を開くキー (K)
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterleash.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                BETTERLEASH_CATEGORY
        ));

        BetterLeash.LOGGER.info("キーバインドを登録しました");
    }

    public static boolean wasOpenConfigPressed() {
        return openConfigKey != null && openConfigKey.wasPressed();
    }
}