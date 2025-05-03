package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import net.nxtresources.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MsgCache {
    static Map<String, String> msg = new HashMap<>();

    public static void load(){
        msg.clear();
        for (String key : Main.messagesConfig.getKeys(true)) {
            if (Main.messagesConfig.isString(key))
                msg.put(key, Main.messagesConfig.getString(key));
        }
    }

    public static String get(String key){
        return msg.getOrDefault(key, "ERR: Not found: " + key);
    }

    public static Component getMsg(String key, String... placeholders) {
        String message = get(key);
        message = message.replace("%prefix%", Objects.requireNonNull(Main.messagesConfig.getString("Prefix")));
        for (int i = 0; i < placeholders.length - 1; i += 2)
            message = message.replace(placeholders[i], placeholders[i + 1]);
        return Main.translateColorCodes(message);
    }
}
