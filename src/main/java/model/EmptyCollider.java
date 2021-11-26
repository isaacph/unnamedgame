package model;

import model.grid.Pathfinding;
import org.joml.Vector2i;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;

public class EmptyCollider implements TypeCollider {

    public EmptyCollider(JSONObject obj) {
        GameObjectType.assertString(obj.getString("type"), getType());
    }

    @Override
    public String getType() {
        return "none";
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
        return Collections.emptyList();
    }

    @Override
    public Collection<Pathfinding.MovementResult> outResult(GameData gameData,
                                                World world,
                                                GameObjectID moverID,
                                                GameObjectID colliderID,
                                                Vector2i moveDir,
                                                Vector2i moveFrom) {
        return Collections.emptyList();
    }
}
