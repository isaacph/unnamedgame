package game;

import game.ClickBox;
import model.GameObject;
import model.TypeComponent;

public interface ClickBoxComponent extends TypeComponent {

    ClickBox makeClickBox(GameObject gameObject);
}
