package model.grid;

import model.GameObjectID;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;

public final class Pathfinding {

    private Pathfinding() {}

    public static class Paths {

        public final GridInfo<?> gridInfo;

        public final Vector2i start = new Vector2i();

        /** The amount of speed you have left when you're at this tile */
        public final Map<Vector2i, Double> speedLeft = new HashMap<>();

        /** The tile that came before reaching this tile (in the shortest path) */
        public final Map<Vector2i, Vector2i> tileParent = new HashMap<>();

        public final Map<Vector2i, Movementc> tileParentMovement = new HashMap<>();

        public Paths(GridInfo gridInfo) {
            this.gridInfo = gridInfo;
        }

        public boolean isTileReachable(Vector2i tile) {
            return speedLeft.containsKey(tile) && speedLeft.get(tile) >= 0;
        }

        public Vector2i getTileParent(Vector2i tile) {
            return tileParent.get(tile);
        }

        /** All possible places for the origin of the shape to end up as a result of moving from one
         * of the possible paths encompassed by this Paths object
         */
        public Collection<Vector2i> possibilities() {
            Set<Vector2i> keys = speedLeft.keySet();
            Collection<Vector2i> toRemove = new ArrayList<>();
            for(Vector2i v : keys) {
                if(speedLeft.get(v) < 0) {
                    toRemove.add(v);
                }
            }
            for(Vector2i v : toRemove) keys.remove(v);
            return keys;
        }

        public List<Movement> getPath(Vector2i destination) {
            return reconstructPath(this, destination);
        }

        public List<Vector2i> getPathTiles(Vector2i destination) {
            return Pathfinding.getPathTiles(getPath(destination));
        }

        public double getPathWeight(Vector2i destination) {
            return Pathfinding.getPathWeight(getPath(destination));
        }
    }

    public interface MovementResult {
        /** The cost of a movement */
        double getCost();

        /** Resulting origin tile position after a movement */
        Vector2i getOutcome();
    }

    public interface Movement {
        MovementResult getResult();
        Vector2i getDirection();
    }

    public static class MovementResultc implements MovementResult {
        private double cost;
        private Vector2ic outcome;

        public MovementResultc(double cost, Vector2ic outcome) {
            this.cost = cost;
            this.outcome = new Vector2i(outcome);
        }

        public MovementResultc(MovementResult result) {
            this.cost = result.getCost();
            this.outcome = result.getOutcome();
        }

        @Override
        public double getCost() {
            return cost;
        }

        @Override
        public Vector2i getOutcome() {
            return new Vector2i(outcome);
        }
    }

    private static class Movementc implements Movement {
        private MovementResult result;
        private Vector2i direction;

        public Movementc(Vector2i direction, MovementResult result) {
            this.direction = direction;
            this.result = new MovementResultc(result);
        }

        public MovementResult getResult() {
            return result;
        }

        public Vector2i getDirection() {
            return direction;
        }
    }

    public interface GridInfo<MoverID> {
        Collection<MovementResult> getMovementResults(MoverID moverID, Vector2i start, Vector2i direction);
    }

    public static final Vector2i[] MOVEMENT_DIRECTIONS = new Vector2i[] {
            new Vector2i(1, 0),
            new Vector2i(0, 1),
            new Vector2i(-1, 0),
            new Vector2i(0, -1)
    };

    public static <MoverID> Paths pathPossibilities(GridInfo<MoverID> gridInfo,
                                          MoverID moverID,
                                          Vector2i position,
                                          double speed) {
        Paths paths = new Paths(gridInfo);
        paths.start.set(position);
        ArrayDeque<Vector2i> tileQueue = new ArrayDeque<>();
        tileQueue.addLast(new Vector2i(position));
        paths.speedLeft.put(new Vector2i(position), speed);
        paths.tileParent.put(new Vector2i(position), new Vector2i(position));
        while(!tileQueue.isEmpty()) {
            Vector2i currentTile = tileQueue.removeLast();
            double currentSpeed = paths.speedLeft.get(currentTile);
            ArrayList<Movementc> possibleMovements = new ArrayList<>();
            for(Vector2i dir : MOVEMENT_DIRECTIONS) {
                Collection<MovementResult> results = gridInfo.getMovementResults(moverID, currentTile, dir);
                for(MovementResult res : results) {
                    possibleMovements.add(new Movementc(dir, res));
                }
            }
            for(Movementc movement : possibleMovements) {
                Vector2i tile = movement.getResult().getOutcome();
                double newTileSpeed = currentSpeed - movement.getResult().getCost();

                // check if the option (movement, tile, tileWeight, newTileSpeed) is better than what we currently have
                if(newTileSpeed >= 0 && (!paths.speedLeft.containsKey(tile) || newTileSpeed > paths.speedLeft.get(tile))) {

                    // set the current option to the one given
                    paths.speedLeft.put(new Vector2i(tile), newTileSpeed);
                    paths.tileParent.put(new Vector2i(tile), new Vector2i(currentTile));
                    paths.tileParentMovement.put(new Vector2i(currentTile), movement);

                    // the new option given should open up new paths that we need to consider
                    // this tile should allow us to find the new paths
                    tileQueue.addLast(new Vector2i(tile));
                }
            }
        }
        return paths;
    }

    public static void changeSelectGrid(ByteGrid.Group group, Collection<Vector2i> tiles) {
        group.map.clear();
        for(Vector2i tile : tiles) {
            group.setTile((byte) 1, tile.x, tile.y);
        }
    }

    // A* shortest path
    public static <MoverID> List<Movement> shortestPath(GridInfo<MoverID> gridInfo,
                                              MoverID moveable,
                                              Vector2i start,
                                              Vector2i end,
                                              double speed) {
        // start at end
        // for each candidate tile, try first closest to start adjacent to end, make way to farthest from start, adjacent to end

        // keep track of new possibilities opening up with this
        ArrayDeque<Vector2i> tileStack = new ArrayDeque<>();

        // this one traces forward, will only be used for reconstruction
        Map<Vector2i, Movementc> bestMovementFromTile = new HashMap<>();

        // these two trace backwards
        Map<Vector2i, Double> speedLeft = new HashMap<>();
        Map<Vector2i, Vector2i> tileParent = new HashMap<>(); // parent means NEXT in a path, bear with me
        tileStack.push(new Vector2i(end));
        speedLeft.put(new Vector2i(end), speed);
        tileParent.put(new Vector2i(end), new Vector2i(end));
        while(!tileStack.isEmpty()) {
            // get current
            Vector2i current = tileStack.pop();

            // put together options to reach the current tile
            List<Vector2i> options = new ArrayList<>(); // these are directional options, not encompassing extra movement fanciness
            double currentSpeed = speedLeft.get(current);
            for(Vector2i dir : MOVEMENT_DIRECTIONS) {
                options.add(new Vector2i(current).add(dir.x, dir.y));
            }
            // sorting is faster only when not using portals, we're doing it anyway
            options.sort(Comparator.comparingInt(a -> manhattanDistance(a, start)));
            ArrayList<Movementc> movementOptions = new ArrayList<>();
            for(Vector2i next : options) {
                Vector2i direction = new Vector2i(current.x - next.x, current.y - next.y);
                for(MovementResult result : gridInfo.getMovementResults(moveable, next, direction)) {
                    movementOptions.add(new Movementc(direction, result));
                }
            }

            // check each option to reach the current tile
            for(Movementc movement : movementOptions) {
                double nextSpeed = currentSpeed - movement.getResult().getCost();
                Vector2i nextTile = new Vector2i(movement.getResult().getOutcome()).sub(movement.getDirection()); // since we refer to this A LOT

                // using the above option (movement, nextSpeed), check if it's better than the current best
                // for this tile.    we optimize for cost
                if(nextSpeed >= 0 &&
                        (!speedLeft.containsKey(nextTile) || nextSpeed > speedLeft.get(nextTile))) {

                    // since it's better, replace the current best
                    speedLeft.put(new Vector2i(nextTile), nextSpeed);
                    tileParent.put(new Vector2i(nextTile), new Vector2i(current));
                    bestMovementFromTile.put(new Vector2i(nextTile), movement);

                    // the new best will open new possibilities, which we will find using this next tile
                    tileStack.push(new Vector2i(nextTile));
                }
            }
        }
        if(!tileParent.containsKey(start)) {
            return new ArrayList<>();
        } else {
            // reconstruct the path
            ArrayList<Movement> path = new ArrayList<>();

            // current tile in path
            Vector2i current = new Vector2i(start);

            // we go until we reach the tile whose parent/next tile is himself
            while(!tileParent.get(current).equals(current)) {

                // add movement from current to parent to path
                path.add(bestMovementFromTile.get(current));

                // set next tile in path
                current.set(tileParent.get(current));
            }
            return path;
        }
    }

    public static int manhattanDistance(Vector2i a, Vector2i b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    public static List<Movement> reconstructPath(Paths paths, Vector2i destination) {
        if(paths.getTileParent(destination) == null) {
            return new ArrayList<>();
        }
        Vector2i current = new Vector2i(destination);
        List<Movement> path = new ArrayList<>();
        while(!paths.getTileParent(current).equals(current)) {
            path.add(paths.tileParentMovement.get(paths.getTileParent(current)));
            current.set(paths.getTileParent(current));
        }
        return path;
    }

    /** For each tile in tileSet, treat that tile as the origin for the shape,
     * and add each resultant shape together to a set, which is returned
     */
    public static Collection<Vector2i> fillTileSetToShape(Collection<Vector2i> shape, Collection<Vector2i> tileSet) {
        if(shape == null || shape.isEmpty() || tileSet == null || tileSet.isEmpty()) return Collections.emptyList();
        Set<Vector2i> newPath = new HashSet<>();
        for(Vector2i center : tileSet) {
            for(Vector2i shapeOffset : shape) {
                newPath.add(new Vector2i(center).add(shapeOffset));
            }
        }
        return newPath;
    }

    public static double getPathWeight(List<Movement> path) {
        if(path == null || path.size() < 1) return 0;
        double weight = 0;
        for(Movement movement : path) {
            weight += movement.getResult().getCost();
        }
        return weight;
    }

    public static List<Vector2i> getPathTiles(List<Movement> path) {
        if(path == null || path.isEmpty()) return Collections.emptyList();
        List<Vector2i> cover = new ArrayList<>();
        for(Movement movement : path) {
            cover.add(movement.getResult().getOutcome());
        }
        return cover;
    }
}
