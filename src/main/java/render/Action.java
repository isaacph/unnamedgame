package render;

import java.io.Serializable;

public interface Action extends Serializable {

    void onStart();
    void onUpdate();
    void onFinish();
}
