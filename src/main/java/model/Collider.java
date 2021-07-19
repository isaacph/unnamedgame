package model;

import model.grid.Pathfinding;
import org.joml.Vector2i;

import java.util.Collection;

public interface Collider {

    /** The mover object is not guaranteed to be in a position to make this result */
    Collection<Pathfinding.MovementResult> inResult(GameData gameData,
                                                    World world,
                                                    GameObjectID moverID,
                                                    GameObjectID colliderID,
                                                    Vector2i moveDir,
                                                    Vector2i moveTo);

    /** The mover object is not guaranteed to be in a position to make this result */
    Collection<Pathfinding.MovementResult> outResult(GameData gameData,
                                                     World world,
                                                     GameObjectID moverID,
                                                     GameObjectID colliderID,
                                                     Vector2i moveDir,
                                                     Vector2i moveFrom);
}
