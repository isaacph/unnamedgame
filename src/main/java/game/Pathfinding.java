package game;

import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.*;

public final class Pathfinding {

    private Pathfinding() {}

    public static class Paths {
        public final Vector2i start = new Vector2i();
        public final Map<Vector2i, Double> speedLeft = new HashMap<>();
        public final Map<Vector2i, Vector2i> tileParent = new HashMap<>();

        public boolean isTileReachable(Vector2i tile) {
            return speedLeft.containsKey(tile) && speedLeft.get(tile) >= 0;
        }

        public Vector2i getTileParent(Vector2i tile) {
            return tileParent.get(tile);
        }

        public Set<Vector2i> possiblities() {
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
    }

    public interface WeightStorage {
        double getWeight(Vector2i tile);
    }

    public static Paths pathPossibilities(WeightStorage weightStorage,
                                         Vector2i position,
                                         double speed) {
        final Vector2i[] directions = new Vector2i[] {
                new Vector2i(1, 0),
                new Vector2i(0, 1),
                new Vector2i(-1, 0),
                new Vector2i(0, -1)
        };
        Paths paths = new Paths();
        paths.start.set(position);
        ArrayDeque<Vector2i> tileQueue = new ArrayDeque<>();
        tileQueue.addLast(new Vector2i(position));
        paths.speedLeft.put(new Vector2i(position), speed);
        paths.tileParent.put(new Vector2i(position), new Vector2i(position));
        while(!tileQueue.isEmpty()) {
            Vector2i currentTile = tileQueue.removeLast();
            double currentSpeed = paths.speedLeft.get(currentTile);
            for(Vector2i dir : directions) {
                Vector2i tile = new Vector2i(currentTile).add(dir);
                double tileWeight = weightStorage.getWeight(tile);
                double newTileSpeed = currentSpeed - tileWeight;
                if(newTileSpeed >= 0 && (!paths.speedLeft.containsKey(tile) || newTileSpeed > paths.speedLeft.get(tile))) {
                    paths.speedLeft.put(new Vector2i(tile), newTileSpeed);
                    paths.tileParent.put(new Vector2i(tile), currentTile);
                    if(newTileSpeed >= 0) {
                        tileQueue.addLast(new Vector2i(tile));
                    }
                }
            }
        }
        return paths;
    }

    public static List<Vector2i> reconstructPath(Paths paths, Vector2i destination) {
        if(paths.getTileParent(destination) == null) {
            return new ArrayList<>();
        }
        Vector2i current = new Vector2i(destination);
        List<Vector2i> path = new ArrayList<>();
        while(!paths.getTileParent(current).equals(current)) {
            path.add(new Vector2i(paths.getTileParent(current)));
            current.set(path.get(path.size() - 1));
        }
        return path;
    }

    public static void changeSelectGrid(ByteGrid.Group group, Paths paths) {
        group.map.clear();
        for(Vector2i tile : paths.speedLeft.keySet()) {
            if(paths.speedLeft.get(tile) >= 0) {
                group.setTile((byte) 1, tile.x, tile.y);
            }
        }
    }

    public static void changeSelectGrid(ByteGrid.Group group, Collection<Vector2i> tiles) {
        group.map.clear();
        for(Vector2i tile : tiles) {
            group.setTile((byte) 1, tile.x, tile.y);
        }
    }

    // A* shortest path
    public static List<Vector2i> shortestPath(WeightStorage weightStorage,
                                              Vector2i start,
                                              Vector2i end,
                                              double speed) {
        final Vector2i[] directions = new Vector2i[] {
                new Vector2i(1, 0),
                new Vector2i(0, 1),
                new Vector2i(-1, 0),
                new Vector2i(0, -1)
        };
        // start at end
        // for each candidate tile, try first closest to start adjacent to end, make way to farthest from start, adjacent to end
        Map<Vector2i, Double> speedLeft = new HashMap<>();
        Map<Vector2i, Vector2i> tileParent = new HashMap<>();
        ArrayDeque<Vector2i> tileStack = new ArrayDeque<>();
        tileStack.push(new Vector2i(end));
        speedLeft.put(new Vector2i(end), speed - weightStorage.getWeight(end));
        tileParent.put(new Vector2i(end), new Vector2i(end));
        while(!tileStack.isEmpty()) {
            Vector2i current = tileStack.pop();
            List<Vector2i> options = new ArrayList<>();
            double currentSpeed = speedLeft.get(current);
            for(Vector2i dir : directions) {
                options.add(new Vector2i(current).add(dir));
            }
            options.sort(Comparator.comparingInt(a -> manhattanDistance(a, start)));
            for(Vector2i next : options) {
                double nextSpeed = currentSpeed - weightStorage.getWeight(next);
                if(nextSpeed >= 0 && (!speedLeft.containsKey(next) || nextSpeed > speedLeft.get(next))) {
                    speedLeft.put(new Vector2i(next), nextSpeed);
                    tileParent.put(new Vector2i(next), new Vector2i(current));
                    tileStack.push(new Vector2i(next));
                }
            }
        }
        if(!tileParent.containsKey(start)) {
            return new ArrayList<>();
        } else {
            ArrayList<Vector2i> path = new ArrayList<>();
            path.add(start);
            while(!tileParent.get(path.get(path.size() - 1)).equals(path.get(path.size() - 1))) {
                path.add(tileParent.get(path.get(path.size() - 1)));
            }
            return path;
        }
    }

    public static int manhattanDistance(Vector2i a, Vector2i b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    public static double getPathWeight(List<Vector2i> path, WeightStorage weightStorage) {
        if(path == null || weightStorage == null) return 0;
        double weight = 0;
        for(int i = 0; i < path.size(); ++i) {
            Vector2i pos = path.get(i);
            weight += weightStorage.getWeight(pos);
        }
        return weight;
    }
}
