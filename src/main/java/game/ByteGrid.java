package game;

import org.joml.Vector2i;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ByteGrid implements Serializable {

    public static final int SIZE = 16;

    public byte[] data = new byte[SIZE * SIZE];
    public int x, y;

    public ByteGrid(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public byte get(int x, int y) {
        return data[x * SIZE + y];
    }
    public void set(byte b, int x, int y) {
        data[x * SIZE + y] = b;
    }

    public static class Group implements Serializable {
        public Map<Vector2i, ByteGrid> map = new HashMap<>();

        public Group() {

        }

        public ByteGrid setTile(byte b, int x, int y) {
            Vector2i p = getGridIndex(x, y);
            ByteGrid f = map.get(p);
            if(f == null) {
                f = new ByteGrid(p.x, p.y);
                map.put(p, f);
            }
            f.set(b, ((x % SIZE) + SIZE) % SIZE, ((y % SIZE) + SIZE) % SIZE);
            return f;
        }

        public ByteGrid makeTileGrid(int x, int y) {
            Vector2i p = getGridIndex(x, y);
            ByteGrid f = map.get(p);
            if(f == null) {
                f = new ByteGrid(p.x, p.y);
                map.put(p, f);
            }
            return f;
        }
        public byte getTile(int x, int y) {
            ByteGrid f = map.get(getGridIndex(x, y));
            if(f == null) return 0;
            return f.get(((x % SIZE) + SIZE) % SIZE, ((y % SIZE) + SIZE) % SIZE);
        }
        public byte getTile(float x, float y) {
            return getTile(MathUtil.floor(x), MathUtil.floor(y));
        }
        public ByteGrid setTile(byte b, float x, float y) {
            return setTile(b, MathUtil.floor(x), MathUtil.floor(y));
        }
        public Vector2i getGridIndex(int x, int y) {
            return new Vector2i((int) Math.floor((double) x / SIZE), (int) Math.floor((double) y / SIZE));
        }
    }
}