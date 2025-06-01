package dungeon.engine;

import dungeon.engine.items.Empty; // Make sure Empty.java is created in dungeon.engine.items
import java.io.Serializable;

public class Cell implements Serializable {
    private static final long serialVersionUID = 3L;
    private Item item;
    private final Position position; // Mostly for context if a Cell object is passed around
    public Cell(int x, int y) {
        this.position = new Position(x, y);
        this.item = new Empty(); // Default to an empty cell
    }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public Position getPosition() { return position; }
    public char getSymbol() { return item.getSymbol(); }
}