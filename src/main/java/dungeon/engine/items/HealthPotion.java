package dungeon.engine.items;

import dungeon.engine.GameEngine;
import dungeon.engine.Item;
import dungeon.engine.Player;

public class HealthPotion implements Item {
    private static final long serialVersionUID = 106L;
    private final int healAmount = 4;
    @Override public char getSymbol() { return 'H'; }
    @Override public String getDescription() { return "a health potion"; }
    @Override public boolean isPassable() { return true; }
    @Override public String interact(Player player, GameEngine engine) {
        int oldHp = player.getHp();
        player.heal(healAmount);
        engine.removeItemFromMap(player.getPosition());
        return "You drank a health potion. HP " + oldHp + " -> " + player.getHp() + " (Max " + player.getMaxHp() + ")";
    }
    @Override
    public String getIconPath() {
        return "/dungeon/gui/icons/healthpotion_icon.png";
    }
}
