package staticData;

import game.MathUtil;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GameDataTest {

    public static void main(String... args) throws IOException {
        JSONObject obj = new JSONObject(MathUtil.readFile("gamedata.json"));

        GameData data = new GameData();
        data.fromJSON(obj, null);
        System.out.println(data.toJSON().toString(4));
    }
}
