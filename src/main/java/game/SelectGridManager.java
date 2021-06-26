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
        double speed = obj.speedLeft;
        Pathfinding.Paths paths = Pathfinding.pathPossibilities(getWeightStorage(object, world, gameData), new Vector2i(obj.x, obj.y), speed);
        Pathfinding.changeSelectGrid(selectionGrid, paths);
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
        return tile -> {
            if(tile.x == gameObject.x && tile.y == gameObject.y) {
                return 0;
            }
            double weight = 0;
            Set<Vector2i> occTiles = type.getRelativeOccupiedTiles();
            for(Vector2i occ : occTiles) {
                int ox = occ.x + tile.x;
                int oy = occ.y + tile.y;
                weight = Math.max(weight, world.getTileWeight(gameData, ox, oy));
            }
            return weight;
        };
    }
}
