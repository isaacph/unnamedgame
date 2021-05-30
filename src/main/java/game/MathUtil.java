package game;

import org.joml.Vector2i;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

public class MathUtil {

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

    public static Collection<Vector2i> addToAll(Collection<Vector2i> collection, Vector2i add) {
        Collection<Vector2i> newCol = new ArrayList<>();
        for(Vector2i pos : collection) {
            newCol.add(new Vector2i(pos).add(add));
        }
        return newCol;
    }
}
