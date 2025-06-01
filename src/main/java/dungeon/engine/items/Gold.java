package dungeon.engine.items;

import dungeon.engine.GameEngine;
import dungeon.engine.Item;
import dungeon.engine.Player;

public class Gold implements Item {
    private static final long serialVersionUID = 105L;
    private final int value = 2;
    @Override public char getSymbol() { return 'G'; }
    @Override public String getDescription() { return "shining gold"; }
    @Override public boolean isPassable() { return true; }
    @Override public String interact(Player player, GameEngine engine) {
        player.addScore(value);
        engine.removeItemFromMap(player.getPosition());
        return "You picked up gold. (+" + value + " score)";
    }
    @Override
    public String getIconPath() {
        return "/dungeon/gui/icons/gold_icon.png";
    }
}

