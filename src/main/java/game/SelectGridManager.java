package game;

import org.joml.Vector2i;

public class SelectGridManager {

    private final World world;
    private final ByteGrid.Group selectionGrid = new ByteGrid.Group();

    public SelectGridManager(World world) {
        this.world = world;
    }

    public void regenerateSelect(Vector2i start, double speed) {
        Pathfinding.Paths paths = Pathfinding.pathPossibilities(getWeightStorage(world), start, speed);
        Pathfinding.changeSelectGrid(selectionGrid, paths);
    }

    public ByteGrid.Group getSelectionGrid() {
        return selectionGrid;
    }

    public static Pathfinding.WeightStorage getWeightStorage(World world) {
        return tile -> {
            byte t = world.grid.getTile(tile.x, tile.y);
            if(t == 0) return 1;
            else return Double.POSITIVE_INFINITY;
        };
    }
}
