package game;

import org.joml.Vector2i;
import org.json.JSONArray;
import org.json.JSONObject;

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

    public ByteGrid(JSONObject obj) {
        x = obj.getInt("x");
        y = obj.getInt("y");
        JSONArray d = obj.getJSONArray("data");
        if(d.length() != data.length) throw new RuntimeException("ByteGrid at " + x + ", " + y + " has block data of wrong size " + d.length());
        for(int i = 0; i < d.length(); ++i) {
            data[i] = (byte) d.getInt(i);
        }
    }

    public byte get(int x, int y) {
        return data[x * SIZE + y];
    }
    public void set(byte b, int x, int y) {
        data[x * SIZE + y] = b;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("x", x);
        obj.put("y", y);
        JSONArray arr = new JSONArray();
        for(byte b : data) {
            arr.put(b);
        }
        obj.put("data", arr);
        return obj;
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

        public JSONObject toJSON() {
            JSONObject obj = new JSONObject();
            for(Vector2i key : map.keySet()) {
                String id = "(" + key.x + ", " + key.y + ")";
                obj.put(id, map.get(key).toJSON());
            }
            return obj;
        }

        public void fromJSON(JSONObject obj) {
            for(String key : obj.keySet()) {
                String[] args = key.split(",");
                if(!args[0].trim().startsWith("(") ||
                        !args[1].trim().endsWith(")") ||
                        args[0].trim().length() <= 1 ||
                        args[1].trim().length() <= 1)
                    throw new RuntimeException("Wrong ByteGrid JSON key format: " + key);

                int x, y;
                try {
                    x = Integer.parseInt(args[0].trim().substring(1));
                    y = Integer.parseInt(args[1].trim().substring(0, args[1].trim().length() - 1));
                } catch(NumberFormatException e) {
                    throw new RuntimeException("Could not parse ByteGrid JSON key as ints: " + key);
                }
                map.put(new Vector2i(x, y), new ByteGrid(obj.getJSONObject(key)));
            }
        }
    }
}