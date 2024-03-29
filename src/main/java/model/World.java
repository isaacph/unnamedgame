package model;

import model.abilities.AbilityComponent;
import model.grid.ByteGrid;
import org.joml.Vector2i;
import org.json.JSONArray;
import org.json.JSONObject;

import game.AbilityOrganizer;
import util.MathUtil;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class World implements Serializable {

    public Map<GameObjectID, GameObject> gameObjects = new HashMap<>();
    public ByteGrid.Group grid = new ByteGrid.Group();
    private UUID version = UUID.randomUUID();
    public TeamManager teams = new TeamManager();
    public GameObjectFactory gameObjectFactory = new GameObjectFactory();

    public World() {

    }

    /** Pretty scuffed that this function exists here,
     * TODO: move this function to an outside class */
    public Collection<GameObjectID> occupied(int x, int y, GameData gameData) {
        ArrayList<GameObjectID> ids = new ArrayList<>();
        for(GameObject object : gameObjects.values()) {
            if(object.alive) {
                Set<Vector2i> occTiles = gameData.getType(object.type).getRelativeOccupiedTiles();
                for(Vector2i occ : occTiles) {
                    int ox = object.x + occ.x;
                    int oy = object.y + occ.y;
                    if(ox == x && oy == y) {
                        ids.add(object.uniqueID);
                    }
                }
            }
        }
        return ids;
    }

    public Collection<GameObject> occupiedObjects(int x, int y, GameData gameData) {
        ArrayList<GameObject> objs = new ArrayList<>();
        for(GameObject object : gameObjects.values()) {
            if(object.alive) {
                Set<Vector2i> occTiles = gameData.getType(object.type).getRelativeOccupiedTiles();
                for(Vector2i occ : occTiles) {
                    int ox = object.x + occ.x;
                    int oy = object.y + occ.y;
                    if(ox == x && oy == y) {
                        objs.add(object);
                    }
                }
            }
        }
        return objs;
    }

    public boolean add(GameObject object, GameData gameData) {
        Collection<Vector2i> positions = MathUtil.addToAll(gameData.getType(object.type).getRelativeOccupiedTiles(), new Vector2i(object.x, object.y));
        for(Vector2i p : positions) {
            if(!occupied(p.x, p.y, gameData).isEmpty()) {
                return false;
            }
        }
        gameObjects.put(object.uniqueID, object);
        return true;
    }

    public UUID getVersion() {
        return version;
    }

    public void nextVersion() {
        this.version = UUID.randomUUID();
    }

    public void setWorld(World other) {
        gameObjects = other.gameObjects;
        grid = other.grid;
        version = other.version;
        teams = other.teams;
        gameObjectFactory = other.gameObjectFactory;
    }

    // Tile weight, agnostic of what occupies it
    public double getPureTileWeight(GameData data, int x, int y) {
        byte tile = grid.getTile(x, y);
        double weight = tile == 1 ? 1 : Double.POSITIVE_INFINITY;
        return weight;
    }

//    public double getTileWeight(GameData data, int x, int y) {
//        byte tile = grid.getTile(x, y);
//        double weight = tile == 1 ? 1 : Double.POSITIVE_INFINITY;
//        for(GameObjectID id : gameObjects.keySet()) {
//            GameObject object = gameObjects.get(id);
//            if(object.alive) {
//                Set<Vector2i> occTiles = data.getType(object.type).getRelativeOccupiedTiles();
//                for(Vector2i occ : occTiles) {
//                    int ox = object.x + occ.x;
//                    int oy = object.y + occ.y;
//                    if(ox == x && oy == y) {
//                        weight = Double.POSITIVE_INFINITY;
//                        return weight;
//                    }
//                }
//            }
//        }
//        return weight;
//    }

    public double getShapeWeightOnTiles(GameData data, int x, int y, Collection<Vector2i> shape) {
        double weight = Double.MIN_VALUE;
        for(Vector2i offset : shape) {
            weight = Math.max(weight, getPureTileWeight(data, x + offset.x, y + offset.y));
        }
        return weight;
    }

    public void resetGameObjectSpeeds(GameData gameData) {
        for(GameObject gameObject : gameObjects.values()) {
            GameObjectType type = gameData.getType(gameObject.type);
            gameObject.speedLeft = type.getSpeed();
        }
    }

    public JSONObject toInitJSON() {
        JSONObject obj = new JSONObject();
        obj.put("grid", grid.toJSON());
        JSONArray arr = new JSONArray();
        for(GameObject go : gameObjects.values()) {
            arr.put(go.toInitJSON());
        }
        obj.put("gameObjects", arr);
        obj.put("gameObjectFactory", gameObjectFactory.toJSON());
        JSONArray teamArr = new JSONArray();
        for(TeamID team : teams.getTeams()) {
            teamArr.put(team.toJSON());
        }
        obj.put("teams", teamArr);
        return obj;
    }

    public void initFromJSON(JSONObject obj, GameData gameData) {
        gameObjects.clear();
        teams.clear();

        grid.fromJSON(obj.getJSONObject("grid"));

        JSONArray goArr = obj.getJSONArray("gameObjects");
        for(int i = 0; i < goArr.length(); ++i) {
            GameObject gameObj = new GameObject(goArr.getJSONObject(i));
            gameObjects.put(gameObj.uniqueID, gameObj);
        }

        gameObjectFactory = new GameObjectFactory(obj.getJSONObject("gameObjectFactory"));

        JSONArray teamArr = obj.getJSONArray("teams");

        Map<GameObjectID, TeamID> chooseNewTeams = new HashMap<>();
        for(int i = 0; i < teamArr.length(); ++i) {
            TeamID oldTeam = new TeamID(teamArr.getJSONObject(i));
            if(!oldTeam.equals(TeamID.NEUTRAL)) {
                TeamID newTeam = teams.teamIDGenerator.generate();
                teams.addTeam(newTeam);

                for(GameObject gameObj : gameObjects.values()) {
                    if(gameObj.team.equals(oldTeam)) {
                        chooseNewTeams.put(gameObj.uniqueID, newTeam);
                    }
                }
            }
        }

        for(GameObjectID key : chooseNewTeams.keySet()) {
            gameObjects.get(key).team = chooseNewTeams.get(key);
        }
    }

    public static List<GameObjectID> getTeamPassives(TeamID team, World world, GameData gameData) {
        List<GameObjectID> ids = new ArrayList<>();
        for(GameObject obj : world.gameObjects.values()) {
            if(obj.team.equals(team)) {
                GameObjectType type = gameData.getType(obj.type);
                if(!type.getPassives().isEmpty()) {
                    ids.add(obj.uniqueID);
                }
            }
        }
        return ids;
    }

    public World deepCopy()
    {
        try {
            //Serialization of object
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);

            //De-serialization of object
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            return (World) in.readObject();
        } catch(Exception e) {
            throw new RuntimeException("Should never happen:" + e.getMessage(), e.getCause());
        }
    }

    public static void animatePassives(World world, GameData gameData, TeamID teamID, Consumer<Action> runAction, Consumer<String> onFail) {
        List<GameObjectID> passivers = World.getTeamPassives(teamID, world, gameData);
        for(GameObjectID id : passivers) {
            Set<AbilityComponent> passives = gameData.getType(world.gameObjects.get(id).type).getPassives();
            for(AbilityComponent passive : passives) {
                Action action = AbilityOrganizer.abilityPassiveCreator.get(passive.getTypeID()).create(passive.getID(), id);
                if(action.validate(null, world, gameData)) {
                    runAction.accept(action);
                } else {
                    onFail.accept("Error: could not validate passive action: " + passive.getID().toString());
                }
            }
        }
    }
}
