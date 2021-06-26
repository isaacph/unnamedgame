package game;

public class SpawnAnimator implements Animator {

    public SpawnAction action;

    public SpawnAnimator(SpawnAction action) {
        this.action = action;
    }

    @Override
    public void animate(Game game) {
        action.execute(game.world, game.gameData);
        game.worldRenderer.resetGameObjectRenderCache();
        game.clickBoxManager.resetGameObjectClickBox(action.newGameObjectResult);
    }
}
