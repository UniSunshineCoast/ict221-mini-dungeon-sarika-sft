package dungeon.engine.items;

import dungeon.engine.GameEngine;
import dungeon.engine.Item;
import dungeon.engine.Player;

public class Ladder implements Item {
    private static final long serialVersionUID = 104L;
    @Override public char getSymbol() { return 'L'; }
    @Override public String getDescription() { return "a ladder leading onwards"; }
    @Override public boolean isPassable() { return true; }
    @Override public String interact(Player player, GameEngine engine) {
        engine.advanceLevel(); // This method will set GameState to WIN_GAME or setup next level
        if (engine.getGameState() == dungeon.engine.GameState.WIN_GAME) {
            return "You climbed the ladder and escaped the dungeon! YOU WIN!";
        } else { // Implies WIN_LEVEL was hit, and engine is now IN_PROGRESS for next level
            return "You climbed the ladder to the next level!";
        }
    }
    @Override
    public String getIconPath() {
        return "/dungeon/gui/icons/ladder_icon.png";
    }
}
