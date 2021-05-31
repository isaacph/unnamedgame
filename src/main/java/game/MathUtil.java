package game;

import org.joml.Vector2i;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MathUtil {

    public static final Vector2i[] DIRECTIONS = new Vector2i[] {
            new Vector2i(1, 0),
            new Vector2i(0, 1),
            new Vector2i(-1, 0),
            new Vector2i(0, -1),
    };

    public static final Vector2i[] DIRECTIONS_DIAGONAL = new Vector2i[] {
            new Vector2i(1, 0),
            new Vector2i(1, 1),
            new Vector2i(0, 1),
            new Vector2i(-1, 1),
            new Vector2i(-1, 0),
            new Vector2i(-1, -1),
            new Vector2i(0, -1),
            new Vector2i(1, -1),
    };

    public static final Vector2i[][] SQUARE_DIRECTIONS_DIAGONAL = new Vector2i[][] {
            new Vector2i[]{
                    new Vector2i(1, 0),
                    new Vector2i(1, 1),
                    new Vector2i(0, 1),
            },
            new Vector2i[]{
                    new Vector2i(0, 1),
                    new Vector2i(-1, 1),
                    new Vector2i(-1, 0),
            },
            new Vector2i[]{
                    new Vector2i(-1, 0),
                    new Vector2i(-1, -1),
                    new Vector2i(0, -1),
            },
            new Vector2i[]{
                    new Vector2i(0, -1),
                    new Vector2i(1, -1),
                    new Vector2i(1, 0),
            },
    };

    public static InputStream getInputStream(String path) throws IOException {
//        return new FileInputStream("src/main/resources/" + path);
        InputStream stream = MathUtil.class.getResourceAsStream("../" + path);
        if(stream == null) {
            throw new IOException("Resource not found at ../" + path);
        }
        return stream;
    }

    public static int floor(double d) {
        return d >= 0 ? (int) d : (int) d - 1;
    }

    public static int floor(float f) {
        return f >= 0 ? (int) f : (int) f - 1;
    }

    public static boolean between(float a, float bound1, float bound2) {
        return Math.min(bound1, bound2) <= a && a <= Math.max(bound1, bound2);
    }

    public static boolean pointInside(float pointX, float pointY, float ax, float ay, float bx, float by) {
        return between(pointX, ax, bx) && between(pointY, ay, by);
    }

    /**
     * Return true if a is greater than or equal to 2^xBits
     */
    public static boolean gteb(int a, int xBits) {
        return a >= (1<<xBits);
    }

    public static int log2(int x) {
        int low = 0, high = 31;
        int mid;
        while(low < high - 1) {
            mid = (low + high) / 2;
            if(gteb(x, mid)) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return low;
    }

    public static int pow2Ceil(int x) {
        if(x <= 0) return 0;
        int log2 = log2(x);
        if(x == 1<<log2) {
            return x;
        }
        return 1<<(log2+1);
    }

    public static Set<Vector2i> addToAll(Set<Vector2i> collection, Vector2i add) {
        Set<Vector2i> newCol = new HashSet<>();
        for(Vector2i pos : collection) {
            newCol.add(new Vector2i(pos).add(add));
        }
        return newCol;
    }

    public static Set<Vector2i> adjacentTiles(Set<Vector2i> collection) {
        Set<Vector2i> adjacent = new HashSet<>();
        for(Vector2i tile : collection) {
            for(Vector2i direction : DIRECTIONS) {
                Vector2i next = new Vector2i(tile).add(direction);
                if(!collection.contains(next)) {
                    adjacent.add(next);
                }
            }
        }
        return adjacent;
    }

    public static boolean isSquare(Collection<Vector2i> square) {
        if(square == null) return false;
        if(square.size() != 4) return false;

        // get first component
        Iterator<Vector2i> it = square.iterator();
        Vector2i start = it.next();
        if(start == null) return false;

        // create list of 3 offsets
        ArrayList<Vector2i> diff = new ArrayList<>();
        while(it.hasNext()) {
            diff.add(new Vector2i(new Vector2i(it.next()).sub(start)));
        }

        // assure that two are length 1 and one is length 2
        int len1 = 0, len2i = -1;
        for(int i = 0; i < diff.size(); ++i) {
            Vector2i v = diff.get(i);
            int len = Math.abs(v.x) + Math.abs(v.y);
            if(len == 1) {
                ++len1;
            } else if(len == 2) {
                if(len2i != -1) return false;
                len2i = i;
            } else {
                return false;
            }
        }
        if(len1 != 2) return false;

        // ensure that the other two add to the len2 one
        Vector2i l2 = diff.get(len2i);
        diff.remove(len2i);
        return diff.get(0).add(diff.get(1)).equals(l2);
    }

    public static Vector2i squareTop(Collection<Vector2i> square) {
        if(!isSquare(square)) return null;
        int topSum = Integer.MIN_VALUE;
        Vector2i top = null;
        for(Vector2i v : square) {
            int sum = -(v.x + v.y);
            if(sum > topSum) {
                topSum = sum;
                top = v;
            }
        }
        return top;
    }
}