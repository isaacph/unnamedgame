package game;

import model.Action;

public interface AnimatorSupplier {

    Animator get(Action action);
}
