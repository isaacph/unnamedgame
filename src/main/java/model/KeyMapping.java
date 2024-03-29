package model;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class KeyMapping {

    private Map<Integer, Integer> keySlotMap;

    public KeyMapping() {
        keySlotMap = new HashMap<>();
        keySlotMap.put(GLFW_KEY_Q, 0);
        keySlotMap.put(GLFW_KEY_W, 1);
        keySlotMap.put(GLFW_KEY_E, 2);
        keySlotMap.put(GLFW_KEY_R, 3);
        keySlotMap.put(GLFW_KEY_T, 4);
        keySlotMap.put(GLFW_KEY_Y, 5);
        keySlotMap.put(GLFW_KEY_U, 6);
        keySlotMap.put(GLFW_KEY_I, 7);
    }

    public Integer getKeyAbilitySlot(int key) {
        return keySlotMap.get(key);
    }
}
