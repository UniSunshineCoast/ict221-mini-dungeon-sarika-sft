package dungeon.engine.items;

import dungeon.engine.GameEngine;
import dungeon.engine.Item;
import dungeon.engine.Player;

public class Empty implements Item {
    private static final long serialVersionUID = 101L; // Unique for this class
    @Override public char getSymbol() { return '.'; }
    @Override public String getDescription() { return "an empty space"; }
    @Override public boolean isPassable() { return true; }
    @Override public String interact(Player player, GameEngine engine) { return "You move into an empty space."; }
    @Override
    public String getIconPath() {
        return "/dungeon/gui/icons/empty_icon.png";
    }


}

