package staticData;

import game.ClickBox;
import game.GameObject;
import game.GameObjectID;
import game.World;
import org.joml.Vector2i;
import org.json.JSONObject;
import render.RenderComponent;
import render.TeamTextureComponent;
import render.WorldRenderer;

import java.io.Serializable;
import java.util.Set;

public class GameObjectType implements Serializable {

    private String name;
    private GameObjectTypeID uniqueID;
    private float speed;
    private float health;
    private Display display;
    private ClickBoxData clickbox;
    private Shape shape;
    private float damage;
    private boolean canMove;
    private boolean canGrow;
    private boolean canSpawn;
    private GameObjectTypeID produce;

    /*private String texture;
    private Vector2f textureOffset;
    private Vector2f textureScale;
    private Vector2f clickBoxOffset;
    private Vector2f clickBoxSize;
    private Vector2f clickBoxDepthOffset;
    private Set<Vector2i> relativeOccupiedTiles;
    private GameObjectTypeID uniqueID;
    private double baseSpeed;
    private float circleSize;
    private Vector2f circleOffset;
    private float maxHealth;
    private boolean canMove;
    private float damage;
    private Vector2f centerOffset;*/

    public GameObjectType(JSONObject obj, GameObjectTypeFactory factory) {
        name = obj.getString("name");
        uniqueID = new GameObjectTypeID(name);
        speed = obj.getFloat("speed");
        health = obj.getFloat("health");
        display = factory.makeDisplay(obj.getJSONObject("display"));
        clickbox = factory.makeClickbox(obj.getJSONObject("clickbox"));
        shape = factory.makeShape(obj.getJSONObject("shape"));
        damage = obj.getFloat("damage");
        canMove = obj.getBoolean("canMove");
        canGrow = obj.getBoolean("canGrow");
        canSpawn = obj.getBoolean("canSpawn");
        produce = new GameObjectTypeID(obj.getString("produce"));
    }

    public void initialize(Object gameObject) {

    }

    public String getName() {
        return uniqueID.getName();
    }

    public GameObjectTypeID getUniqueID() {
        return uniqueID;
    }

    public double getSpeed() {
        return speed;
    }

    public float getMaxHealth() { return health; }

    public boolean canMove() {
        return canMove;
    }

    public boolean canGrow() {
        return canGrow;
    }

    public boolean canSpawn() {
        return canSpawn;
    }

    public GameObjectTypeID producedType() {
        return produce;
    }

    public float getDamage() { return damage; }

    @Override
    public String toString() {
        return uniqueID.toString();
    }

    public static void assertString(String givenType, String assertType) throws AssertionError {
        if(!givenType.equals(assertType)) {
            throw new AssertionError("Wrong object type! Should have been checked earlier! (type: "
                    + givenType + ", should be " + assertType + ")");
        }
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("speed", speed);
        obj.put("display", display.toJSON());
        obj.put("clickbox", clickbox.toJSON());
        obj.put("shape", shape.toJSON());
        obj.put("health", health);
        obj.put("damage", damage);
        obj.put("canMove", canMove);
        obj.put("canGrow", canGrow);
        obj.put("canSpawn", canSpawn);
        obj.put("produce", produce.getName());
        return obj;
    }

    public Set<Vector2i> getRelativeOccupiedTiles() {
        return shape.getRelativeOccupiedTiles();
    }

    public ClickBox makeClickBox(GameObject obj) {
        return clickbox.makeClickBox(obj);
    }

    public RenderComponent makeRenderComponent(GameObjectID gameObjectID, World world, GameData gameData, WorldRenderer.GameObjectTextures textureLibrary) {
        return display.makeRenderComponent(gameObjectID, world, gameData, textureLibrary);
    }
}
