package model.grid;

import model.*;
import org.joml.Vector2i;

import java.util.*;

public class TileGrid implements Pathfinding.GridInfo<GameObjectID> {

    private final World world;
    private final GameData gameData;
    private final WorldCollider worldCollider;

    public TileGrid(GameData gameData, World world) {
        this.gameData = gameData;
        this.world = world;
        this.worldCollider = new WorldCollider();
    }

    @Override
    public Collection<Pathfinding.MovementResult> getMovementResults(GameObjectID moverID, Vector2i start, Vector2i direction) {
        Vector2i dest = new Vector2i(start.x + direction.x, start.y + direction.y);
        Collection<GameObjectID> destObjID = world.occupied(dest.x, dest.y, gameData);
        Collection<GameObjectID> srcObjID = world.occupied(start.x, start.y, gameData);
        srcObjID.removeAll(destObjID);

        // add together, for each tile, all costs to get to that tile
        Map<Vector2i, MovementResultSum> possibleResults = new HashMap<>();

        Collection<ResultProducer> resultProducers = new ArrayList<>();
        // add normal world collider result
        resultProducers.add(() -> worldCollider.inResult(gameData,
                world,
                moverID,
                null,
                direction,
                dest));

        // add results resulting from moving off game objects on the start tile
        for(GameObjectID id : srcObjID) {
            if(!id.equals(moverID)) {
                resultProducers.add(() ->
                        gameData.getType(world.gameObjects.get(id).type).getCollider().outResult(
                            gameData,
                            world,
                            moverID,
                            id,
                            new Vector2i(direction),
                            new Vector2i(start))
                );
            }
        }
        // add results resulting from moving into or inside of game objects on the destination tile
        for(GameObjectID id : destObjID) {
            if(!id.equals(moverID)) {
                resultProducers.add(() ->
                        gameData.getType(world.gameObjects.get(id).type).getCollider().inResult(
                            gameData,
                            world,
                            moverID,
                            id,
                            new Vector2i(direction),
                            new Vector2i(dest))
                );
            }
        }

        // check each possible result
        for(ResultProducer resultProducer : resultProducers) {
            for(Pathfinding.MovementResult newResult : resultProducer.produce()) {

                // newResult is a possible movement outcome (end position) and additional cost to get there
                Vector2i currentOutcome = newResult.getOutcome();

                // get the current cost to get to this outcome
                MovementResultSum currentResult = possibleResults.get(currentOutcome);
                if(currentResult == null) {
                    // no cost has yet been specified, so newResult has the correct cost so far for that outcome tile
                    possibleResults.put(currentOutcome, new MovementResultSum(newResult));
                } else {
                    // there is a cost so far, so add newResult's cost to it
                    currentResult.cost += newResult.getCost();
                }
            }
        }
        return new ArrayList<>(possibleResults.values());
    }

    private interface ResultProducer {
        Collection<Pathfinding.MovementResult> produce();
    }

    private static class MovementResultSum implements Pathfinding.MovementResult {

        double cost;
        private Vector2i outcome;

        MovementResultSum(Pathfinding.MovementResult start) {
            cost = start.getCost();
            outcome = start.getOutcome();
        }

        @Override
        public double getCost() {
            return cost;
        }

        @Override
        public Vector2i getOutcome() {
            return outcome;
        }
    }

    private static class WorldCollider implements Collider {

        @Override
        public Collection<Pathfinding.MovementResult> inResult(GameData gameData,
                                                               World world,
                                                               GameObjectID moverID,
                                                               GameObjectID colliderID, // null
                                                               Vector2i moveDir,
                                                               Vector2i moveTo) {
            return Collections.singletonList(new Pathfinding.MovementResultc(
                    world.getShapeWeightOnTiles(
                            gameData,
                            moveTo.x, moveTo.y,
                            gameData.getType(world.gameObjects.get(moverID).type).getRelativeOccupiedTiles()),
                    new Vector2i(moveTo)));
        }

        @Override
        public Collection<Pathfinding.MovementResult> outResult(GameData gameData,
                                                                World world,
                                                                GameObjectID moverID,
                                                                GameObjectID colliderID,
                                                                Vector2i moveDir,
                                                                Vector2i moveFrom) {
            return Collections.emptyList(); // it's impossible to move out of the world
        }
    }
}
