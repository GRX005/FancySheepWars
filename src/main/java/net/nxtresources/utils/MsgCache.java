package net.nxtresources.utils;

import net.nxtresources.managers.ConfigMgr;

import java.util.*;

public class MsgCache {
    static Map<String, String> msg;
    static Map<String, List<String>> msgList;

    public static void load(){
        msg = new HashMap<>();
        msgList = new HashMap<>();
        String prefix = Objects.requireNonNullElse(ConfigMgr.messagesConfig.getString("Prefix"), "§8[§ePrefix§8] ");
        for (String key : ConfigMgr.messagesConfig.getKeys(true)) {
            if (ConfigMgr.messagesConfig.isString(key)) {
                String msgStr = ConfigMgr.messagesConfig.getString(key);
                if (msgStr != null) {
                    msgStr = msgStr.replace("%prefix%", prefix);
                    msg.put(key, msgStr);
                }
            }
            if (ConfigMgr.messagesConfig.isList(key)) {
                List<String> rawList = ConfigMgr.messagesConfig.getStringList(key);
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
