package dungeon.engine.items;

import dungeon.engine.GameEngine;
import dungeon.engine.Item;
import dungeon.engine.Player;

public class Wall implements Item {
    private static final long serialVersionUID = 102L;
    @Override public char getSymbol() { return '#'; }
    @Override public String getDescription() { return "a solid wall"; }
    @Override public boolean isPassable() { return false; }
    @Override public String interact(Player player, GameEngine engine) { return "You bumped into a wall."; }
    @Override
    public String getIconPath() {
        return "/dungeon/gui/icons/wall_icon.png";
    }
}
