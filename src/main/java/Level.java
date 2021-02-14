import org.joml.Vector2i;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Level implements Serializable {

    public Map<Vector2i, Grid> gridMap = new HashMap<>();
}
