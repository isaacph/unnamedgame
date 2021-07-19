package model;

import model.grid.Pathfinding;
import org.joml.Vector2i;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;

public class BlockCollider implements TypeCollider {

    public BlockCollider(JSONObject obj) {
        GameObjectType.assertString(obj.getString("type"), getType());
    }

    @Override
    public String getType() {
        return "block";
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("type", getType());
        return obj;
    }

    @Override
    public Collection<Pathfinding.MovementResult> inResult(GameData gameData,
                                                           World world,
                                                           GameObjectID moverID,
                                                           GameObjectID colliderID,
                                                           Vector2i moveDir,
                                                           Vector2i moveTo) {
        return Collections.singletonList(new BlockResult(moveTo));
    }

    @Override
    public Collection<Pathfinding.MovementResult> outResult(GameData gameData,
                                                World world,
                                                GameObjectID moverID,
                                                GameObjectID colliderID,
                                                Vector2i moveDir,
                                                Vector2i moveFrom) {
        return Collections.singletonList(new BlockResult(new Vector2i(moveFrom).add(moveDir)));
    }

    public class BlockResult implements Pathfinding.MovementResult {

        private Vector2i dest;

        public BlockResult(Vector2i dest) {
            this.dest = new Vector2i(dest);
        }

        @Override
        public double getCost() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public Vector2i getOutcome() {
            return new Vector2i(dest);
        }
    }
}
