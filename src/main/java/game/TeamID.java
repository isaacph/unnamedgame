package game;

import java.io.Serializable;
import java.util.Random;

public class TeamID implements Serializable {

    private final int id;

    private TeamID(int init) {
        this.id = init;
    }

    public TeamID(TeamID other) {
        this.id = other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Team" + id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TeamID && ((TeamID) other).id == id;
    }

    public static class Generator {

        private int generatorNumber;

        public Generator() {
            generatorNumber = 0;
        }

        public TeamID generate() {
            return new TeamID(++generatorNumber);
        }
    }

    public static TeamID getLocalPlaceholder() {
        return new TeamID(new Random().nextInt());
    }
}