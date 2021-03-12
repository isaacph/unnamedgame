package game;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {

    public static InputStream getInputStream(String path) throws IOException {
//        return new FileInputStream("src/main/resources/" + path);
        InputStream stream = Util.class.getResourceAsStream("../" + path);
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
}
