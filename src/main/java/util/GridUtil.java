package util;

import model.*;
import org.joml.Vector2i;

import java.util.Set;

public class GridUtil {
    public static Pathfinding.WeightStorage getWeightStorage(GameObjectID gameObjectID, World world, GameData gameData) {
        GameObject gameObject = world.gameObjects.get(gameObjectID);
        GameObjectType type = gameData.getType(gameObject.type);
        Set<Vector2i> shape = type.getRelativeOccupiedTiles();
        return tile -> {
            if(tile.x == gameObject.x && tile.y == gameObject.y) {
                return 0;
            }
            double weight = 0;
            for(Vector2i occ : shape) {
                int ox = occ.x + tile.x;
                int oy = occ.y + tile.y;
                weight = Math.max(weight, world.getPureTileWeight(gameData, ox, oy));
                GameObjectID id = world.occupied(ox, oy, gameData);
                if(id != null && !id.equals(gameObjectID)) {
                    return Double.POSITIVE_INFINITY;
                }
            }
            return weight;
        };
    }
}
