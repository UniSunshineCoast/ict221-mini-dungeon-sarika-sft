package dungeon.engine.items;

import dungeon.engine.GameEngine;
import dungeon.engine.Item;
import dungeon.engine.Player;

public class PlayerMarkerItem implements Item {
    private static final long serialVersionUID = 110L;
    @Override public char getSymbol() { return 'P'; }
    @Override public String getDescription() { return "the brave adventurer"; }
    @Override public boolean isPassable() { return true; } // Player can be on their own spot
    @Override public String interact(Player player, GameEngine engine) { return "It's you!"; } // Should ideally not be "interacted" with
    @Override public String getIconPath()
    { return "/dungeon/gui/icons/player_icon.png";}
}

