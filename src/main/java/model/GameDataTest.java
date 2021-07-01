package model;

import util.FileUtil;
import org.json.JSONObject;

import java.io.IOException;

public class GameDataTest {

    public static void main(String... args) throws IOException {
        JSONObject obj = new JSONObject(FileUtil.readFile("gamedata.json"));

        GameData data = new GameData();
        data.fromJSON(obj, null);
        System.out.println(data.toJSON().toString(4));
    }
}
