package dungeon.engine;

import java.io.Serializable;

public interface Item extends Serializable {
    // All implementing classes should define their own serialVersionUID if they have fields.
    char getSymbol();
    String getDescription();
    boolean isPassable();
    String interact(Player player, GameEngine engine);
    String getIconPath();
}