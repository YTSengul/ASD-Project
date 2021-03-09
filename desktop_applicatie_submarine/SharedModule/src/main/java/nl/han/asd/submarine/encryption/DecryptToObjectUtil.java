package nl.han.asd.submarine.encryption;

import com.google.gson.Gson;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.models.message.system.NewChat;
import nl.han.asd.submarine.models.routing.Onion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DecryptToObjectUtil {

    public static Object toObject(String json) {
        return new Gson().fromJson(json, getClassForJson(json));
    }

    public static Class getClassForJson(String json) {
        try {
            if (getKeysFromJson(json).contains("title") && getKeysFromJson(json).contains("participants")) {
                return NewChat.class;
            }
//            if (getKeysFromJson(json).contains("chunks")) {
//                return ChunkMessage.class;
//            } else if (getKeysFromJson(json).contains("recipe")) {
//                return RecipeMessage.class;
//            } else
            if (getKeysFromJson(json).contains("message")) {
                return TextMessage.class;
            } else if (getKeysFromJson(json).contains("destination") ||
                    getKeysFromJson(json).contains("command") ||
                    getKeysFromJson(json).contains("data")) {
                return Onion.class;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TextMessage.class;
    }

    public static List getKeysFromJson(String jsoString) {
        Object obj = new Gson().fromJson(jsoString, Object.class);
        List keys = new ArrayList();
        collectAllTheKeys(keys, obj);
        return keys;
    }

    private static void collectAllTheKeys(List keys, Object o) {
        Collection values = null;
        if (o instanceof Map) {
            Map map = (Map) o;
            keys.addAll(map.keySet());
            values = map.values();
        } else if (o instanceof Collection) {
            values = (Collection) o;
        } else {
            return;
        }
        for (Object value : values) {
            collectAllTheKeys(keys, value);
        }
    }

}
