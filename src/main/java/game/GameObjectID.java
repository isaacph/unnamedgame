package game;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.UUID;

public class GameObjectID implements Serializable {

    private final int id;

    private GameObjectID(int id) {
        this.id = id;
    }

    public GameObjectID(GameObjectID other) {
        this.id = other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GameObjectID && ((GameObjectID) other).id == id;
    }

    @Override
    public String toString() {
        return "GameObject:" + id;
    }

    public static class Generator implements Serializable {

        public int number = 0;

        public Generator() {

        }

        public GameObjectID generate() {
            return new GameObjectID(number++);
        }
    }
}
