package game;

import org.joml.Vector2f;
import org.joml.Vector2i;
import staticData.GameData;
import staticData.GameObjectType;

import java.io.Serializable;
import java.util.*;

public class World implements Serializable {

    public Map<GameObjectID, GameObject> gameObjects = new HashMap<>();
    public ByteGrid.Group grid = new ByteGrid.Group();
    private UUID version = UUID.randomUUID();
    public TeamManager teams = new TeamManager();
    public GameObjectFactory gameObjectFactory = new GameObjectFactory();

    public World() {

    }

    public GameObjectID occupied(int x, int y, GameData gameData) {
        for(GameObject object : gameObjects.values()) {
            if(object.alive) {
                Set<Vector2i> occTiles = gameData.getType(object.type).getRelativeOccupiedTiles();
                for(Vector2i occ : occTiles) {
                    int ox = object.x + occ.x;
                    int oy = object.y + occ.y;
                    if(ox == x && oy == y) {
                        return object.uniqueID;
                    }
                }
            }
        }
        return null;
    }

    public boolean add(GameObject object, GameData gameData) {
        Collection<Vector2i> positions = MathUtil.addToAll(gameData.getType(object.type).getRelativeOccupiedTiles(), new Vector2i(object.x, object.y));
        for(Vector2i p : positions) {
            if(occupied(p.x, p.y, gameData) != null) {
                return false;
            }
        }
        gameObjects.put(object.uniqueID, object);
        return true;
    }

    public UUID getVersion() {
        return version;
    }

    public void nextVersion() {
        this.version = UUID.randomUUID();
    }

    public void setWorld(World other) {
        gameObjects = other.gameObjects;
        grid = other.grid;
        version = other.version;
        teams = other.teams;
        gameObjectFactory = other.gameObjectFactory;
    }

    public double getTileWeight(GameData data, int x, int y) {
        byte tile = grid.getTile(x, y);
        double weight = tile == 1 ? 1 : Double.POSITIVE_INFINITY;
        for(GameObjectID id : gameObjects.keySet()) {
            GameObject object = gameObjects.get(id);
            if(object.alive) {
                Set<Vector2i> occTiles = data.getType(object.type).getRelativeOccupiedTiles();
                for(Vector2i occ : occTiles) {
                    int ox = object.x + occ.x;
                    int oy = object.y + occ.y;
                    if(ox == x && oy == y) {
                        weight = Double.POSITIVE_INFINITY;
                        return weight;
                    }
                }
            }
        }
        return weight;
    }

    public void resetGameObjectSpeeds() {
        for(GameObject gameObject : gameObjects.values()) {
            gameObject.resetSpeed();
        }
    }
}
