package game;

import model.*;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.Set;

public class SelectGridManager {

    private final GameData gameData;
    private final World world;
    private final ByteGrid.Group selectionGrid = new ByteGrid.Group();

    public SelectGridManager(World world, GameData gameData) {
        this.world = world;
        this.gameData = gameData;
    }

    public void regenerateSelect(GameObjectID object) {
        GameObject obj = world.gameObjects.get(object);
        GameObjectType type = gameData.getType(obj.type);
        double speed = obj.speedLeft;
        Pathfinding.Paths paths = Pathfinding.pathPossibilities(getWeightStorage(object, world, gameData), new Vector2i(obj.x, obj.y), speed);
        Pathfinding.changeSelectGrid(selectionGrid,
                Pathfinding.fillTileSetToShape(type.getRelativeOccupiedTiles(), paths.speedLeft.keySet()));
    }

    public void regenerateSelect(Collection<Vector2i> tiles) {
        Pathfinding.changeSelectGrid(selectionGrid, tiles);
    }

    public ByteGrid.Group getSelectionGrid() {
        return selectionGrid;
    }

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
