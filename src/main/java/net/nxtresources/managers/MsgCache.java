package net.nxtresources.managers;

import net.nxtresources.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MsgCache {
    static Map<String, String> msg;

    public static void load(){
        msg = new HashMap<>();
        String prefix = Objects.requireNonNullElse(Main.messagesConfig.getString("Prefix"), "§8[§ePrefix§8] ");
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
}
