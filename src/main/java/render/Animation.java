package render;

public interface Animation {

    void onStart();
    void onUpdate();
    void onFinish();

    void onObjectChange();
}
