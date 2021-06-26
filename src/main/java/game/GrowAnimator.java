package game;

public class GrowAnimator implements Animator {

    private GrowAction action;

    public GrowAnimator(GrowAction action) {
        this.action = action;
    }

    @Override
    public void animate(Game game) {
        action.execute(game.world, game.gameData);
        game.worldRenderer.resetGameObjectRenderCache();
        game.animationManager.resetWhereNeeded();
        for(GameObjectID id : action.seeds) {
            game.clickBoxManager.resetGameObjectClickBox(id);
        }
        game.clickBoxManager.resetGameObjectClickBox(action.newGameObjectResult);
    }
}
