package game;

import model.*;
import model.grid.ByteGrid;
import model.grid.Pathfinding;
import model.grid.TileGrid;
import org.joml.Vector2i;

import java.util.Collection;

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
        Pathfinding.Paths paths = Pathfinding.pathPossibilities(new TileGrid(gameData, world), obj.uniqueID, new Vector2i(obj.x, obj.y), speed);
        Pathfinding.changeSelectGrid(selectionGrid,
                Pathfinding.fillTileSetToShape(type.getRelativeOccupiedTiles(), paths.speedLeft.keySet()));
    }

    public void regenerateSelect(Collection<Vector2i> tiles) {
        Pathfinding.changeSelectGrid(selectionGrid, tiles);
    }

    public ByteGrid.Group getSelectionGrid() {
        return selectionGrid;
    }

}
