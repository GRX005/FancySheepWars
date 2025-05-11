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
        String prefix = Objects.requireNonNullElse(Main.messagesConfig.getString("Prefix"), "");
        for (String key : Main.messagesConfig.getKeys(true)) {
            if (Main.messagesConfig.isString(key)) {
                String msgStr = Main.messagesConfig.getString(key);
                if(msgStr!=null){
                    msgStr = msgStr.replace("%prefix%", prefix);
                    msg.put(key, msgStr);
                }
            }
        }
    }

    public static String get(String key){
        return msg.getOrDefault(key, "ERR: Not found: " + key);
    }

    public static Component getMsg(String key) {
        return Main.translateColorCodes(get(key));
    }

    public static Component getMsg(String key, String arena, String usage, String ms) {
        String msg = get(key);
                if(arena!=null)msg=msg.replace("%arena_name%", arena);
                if(ms!=null)msg=msg.replace("%ms%", ms);
                if(usage!=null)msg=msg.replace("%usage%", usage);
        return Main.translateColorCodes(msg);
    }
}
