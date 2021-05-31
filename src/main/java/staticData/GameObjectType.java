package staticData;

import game.GameObject;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class GameObjectType implements Serializable {

    private String name;
    private String texture;
    private Vector2f textureOffset;
    private Vector2f textureScale;
    private Vector2f clickBoxOffset;
    private Vector2f clickBoxSize;
    private Vector2f clickBoxDepthOffset;
    private Set<Vector2i> relativeOccupiedTiles;
    private int uniqueID;
    private double baseSpeed;
    private float circleSize;
    private Vector2f circleOffset;
    private float maxHealth;
    private boolean canMove;
    private float damage;
    private Vector2f centerOffset;

    public GameObjectType(int uniqueID, String name, String texture, Vector2f offset, Vector2f scale, Vector2f cbOffset, Vector2f cbSize, Vector2f cbdOffset,
                          Vector2i occupySizeMin, Vector2i occupySizeMax, double baseSpeed, float circleSize, Vector2f circleOffset, float maxHealth, boolean canMove, float damage, Vector2f centerOffset) {
        this.name = name;
        this.texture = texture;
        this.textureOffset = offset;
        this.textureScale = scale;
        this.uniqueID = uniqueID;
        this.clickBoxOffset = cbOffset;
        this.clickBoxSize = cbSize;
        this.clickBoxDepthOffset = cbdOffset;
        relativeOccupiedTiles = new HashSet<>();
        for(int x = occupySizeMin.x; x <= occupySizeMax.x; ++x) {
            for(int y = occupySizeMin.y; y <= occupySizeMax.y; ++y) {
                relativeOccupiedTiles.add(new Vector2i(x, y));
            }
        }
        this.baseSpeed = baseSpeed;
        this.circleOffset = circleOffset;
        this.circleSize = circleSize;
        this.maxHealth = maxHealth;
        this.canMove = canMove;
        this.damage = damage;
        this.centerOffset = centerOffset;
    }

    public void initialize(GameObject gameObject) {

    }

    public String getName() {
        return name;
    }

    public String getTexturePath() {
        return texture;
    }

    public Vector2f getTextureOffset() {
        return new Vector2f(textureOffset);
    }

    public Vector2f getTextureScale() {
        return new Vector2f(textureScale);
    }

    public Vector2f getClickBoxOffset() {
        return new Vector2f(clickBoxOffset);
    }

    public Vector2f getClickBoxSize() {
        return new Vector2f(clickBoxSize);
    }

    public Vector2f getClickBoxDepthOffset() {
        return new Vector2f(clickBoxDepthOffset);
    }

    public float getCircleSize() {
        return circleSize;
    }
    public Vector2f getCircleOffset() {
        return new Vector2f(circleOffset);
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    /** plz don't change my set */
    public Set<Vector2i> getRelativeOccupiedTiles() {
        return relativeOccupiedTiles;
    }

    public float getMaxHealth() { return maxHealth; }

    public boolean canMove() {
        return canMove;
    }

    public float getDamage() { return damage; }

    public Vector2f getCenterOffset() {
        return new Vector2f(centerOffset);
    }
}
