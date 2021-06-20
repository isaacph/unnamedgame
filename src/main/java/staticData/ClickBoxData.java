package staticData;

import game.ClickBox;
import game.GameObject;

public interface ClickBoxData extends TypeComponent {

    ClickBox makeClickBox(GameObject gameObject);
}
