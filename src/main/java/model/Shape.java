package model;

import org.joml.Vector2i;

import java.util.Set;

public interface Shape extends TypeComponent {

    Set<Vector2i> getRelativeOccupiedTiles();
}
