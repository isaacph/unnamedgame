import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {

    public static InputStream getInputStream(String path) throws IOException {
        return new FileInputStream("src/main/resources/" + path);
    }
}
