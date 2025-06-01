package dungeon.engine.items;

import dungeon.engine.GameEngine;
import dungeon.engine.Item;
import dungeon.engine.Player;

public class MeleeMutant implements Item {
    private static final long serialVersionUID = 108L;
    private final int damage = 2;
    private final int scoreValue = 2;
    @Override public char getSymbol() { return 'M'; }
    @Override public String getDescription() { return "a fearsome melee mutant"; }
    @Override public boolean isPassable() { return true; }
    @Override public String interact(Player player, GameEngine engine) {
        player.takeDamage(damage);
        player.addScore(scoreValue);
        engine.removeItemFromMap(player.getPosition());
        return "You defeated a melee mutant! (-" + damage + " HP, +" + scoreValue + " score)";
    }
    @Override
    public String getIconPath() {
        return "/dungeon/gui/icons/ladder_icon.png";
    }
}

