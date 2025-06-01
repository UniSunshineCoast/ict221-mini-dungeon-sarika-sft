package dungeon.engine.items;

import dungeon.engine.GameEngine;
import dungeon.engine.Item;
import dungeon.engine.Player;

public class Entry implements Item {
    private static final long serialVersionUID = 103L;
    @Override public char getSymbol() { return 'E'; }
    @Override public String getDescription() { return "the entry point"; }
    @Override public boolean isPassable() { return true; }
    @Override public String interact(Player player, GameEngine engine) { return "You are at the entry point."; }
    @Override
    public String getIconPath() {
        return "/dungeon/gui/icons/entry_icon.png";
    }
}