package game;

import java.io.Serializable;
import java.util.UUID;

public class GameObjectID implements Serializable {

    private final UUID id;

    private GameObjectID(UUID id) {
        this.id = id;
    }

    public GameObjectID(GameObjectID other) {
        this.id = other.id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GameObjectID && ((GameObjectID) other).id.equals(id);
    }

    @Override
    public String toString() {
        return "GameObject:" + id.toString();
    }

    public static class Generator {

        public Generator() {

        }

        public GameObjectID generate() {
            return new GameObjectID(UUID.randomUUID());
        }
    }
}
