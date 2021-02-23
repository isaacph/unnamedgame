import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {

    public static InputStream getInputStream(String path) throws IOException {
        return new FileInputStream("src/main/resources/" + path);
    }

    public static int floor(double d) {
        return d >= 0 ? (int) d : (int) d - 1;
    }

    public static int floor(float f) {
        return f >= 0 ? (int) f : (int) f - 1;
    }
}
