package dungeon.engine.items;

import dungeon.engine.GameEngine;
import dungeon.engine.Item;
import dungeon.engine.Player;

public class RangedMutant implements Item {
    private static final long serialVersionUID = 109L;
    public static final int RANGED_ATTACK_DAMAGE = 2;
    public static final int RANGED_ATTACK_RANGE = 2;
    public static final double RANGED_ATTACK_HIT_CHANCE = 0.5;
    private final int scoreValue = 2;
    @Override public char getSymbol() { return 'R'; }
    @Override public String getDescription() { return "a cunning ranged mutant"; }
    @Override public boolean isPassable() { return true; }
    @Override public String interact(Player player, GameEngine engine) {
        player.addScore(scoreValue);
        engine.removeItemFromMap(player.getPosition());
        return "You attacked and defeated a ranged mutant directly! (+" + scoreValue + " score)";
    }
    @Override
    public String getIconPath() {
        return "/dungeon/gui/icons/rangedmutant_icon.png";
    }
}
