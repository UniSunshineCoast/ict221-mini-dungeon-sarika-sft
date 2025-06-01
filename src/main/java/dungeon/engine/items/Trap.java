package dungeon.engine.items;

import dungeon.engine.GameEngine;
import dungeon.engine.Item;
import dungeon.engine.Player;

public class Trap implements Item {
    private static final long serialVersionUID = 107L;
    private final int damage = 2;
    @Override public char getSymbol() { return 'T'; }
    @Override public String getDescription() { return "a hidden trap"; }
    @Override public boolean isPassable() { return true; }
    @Override public String interact(Player player, GameEngine engine) {
        player.takeDamage(damage);
        return "You fell into a trap! (-" + damage + " HP)";
    }
    @Override
    public String getIconPath() {
        return "/dungeon/gui/icons/trap_icon.png";}
    }


