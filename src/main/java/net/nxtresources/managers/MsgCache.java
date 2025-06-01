package net.nxtresources.managers;

import net.nxtresources.Main;

import java.util.*;

public class MsgCache {
    static Map<String, String> msg;
    static Map<String, List<String>> msgList = new HashMap<>();

    public static void load(){
        msg = new HashMap<>();
        msgList = new HashMap<>();
        String prefix = Objects.requireNonNullElse(Main.messagesConfig.getString("Prefix"), "§8[§ePrefix§8] ");
        for (String key : Main.messagesConfig.getKeys(true)) {
            if (Main.messagesConfig.isString(key)) {
                String msgStr = Main.messagesConfig.getString(key);
                if (msgStr != null) {
                    msgStr = msgStr.replace("%prefix%", prefix);
                    msg.put(key, msgStr);
                }
            }
            if (Main.messagesConfig.isList(key)) {
                List<String> rawList = Main.messagesConfig.getStringList(key);
                List<String> replacedList = new ArrayList<>();
                for (String s : rawList)
                    replacedList.add(s.replace("%prefix%", prefix));
                msgList.put(key, replacedList);
            }
        }
    }

    public static String get(String key){
        return msg.getOrDefault(key, "ERR: Not found: " + key);
    }

    public static List<String> getList(String key) {
        return msgList.getOrDefault(key, Collections.singletonList("ERR: List not found: " + key));
    }
}
