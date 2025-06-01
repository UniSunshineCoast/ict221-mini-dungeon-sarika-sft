package dungeon.engine;

import dungeon.engine.items.*; // Imports all classes from the items sub-package
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameMap implements Serializable {
    private static final long serialVersionUID = 4L;
    private final Cell[][] grid;
    private final int width;
    private final int height;
    private Position entryPosition;
    private Position ladderPosition; // To know where the ladder is for advancing levels

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[height][width]; // Standard [row][col] -> [y][x]
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = new Cell(x, y);
            }
        }
    }
    public Cell getCell(int x, int y) {
        if (isValidPosition(x, y)) return grid[y][x];
        return null; // Or throw an exception for invalid coordinates
    }
    public Cell getCell(Position pos) { return getCell(pos.x(), pos.y()); }
    public void setItemAt(Position pos, Item item) {
        if (isValidPosition(pos)) {
            grid[pos.y()][pos.x()].setItem(item);
            if (item instanceof Ladder) this.ladderPosition = pos;
        }
    }
    public boolean isValidPosition(int x, int y) { return x >= 0 && x < width && y >= 0 && y < height; }
    public boolean isValidPosition(Position pos) { return isValidPosition(pos.x(), pos.y()); }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Position getEntryPosition() { return entryPosition; }
    public Position getLadderPosition() { return ladderPosition; }

    public void placeItemsRandomly(int difficulty, int currentLevel, Player player) {
        Random random = new Random();
        List<Position> availableCells = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                availableCells.add(new Position(x, y));
            }
        }
        Collections.shuffle(availableCells);

        // 1. Determine and set Entry
        if (currentLevel == 1) {
            this.entryPosition = new Position(0, height - 1); // Bottom-left for Level 1
        } else {
            // For Level 2+, entry is where the player was (at the ladder of the previous level)
            this.entryPosition = player.getPosition();
        }
        setItemAt(this.entryPosition, new Entry());
        availableCells.remove(this.entryPosition); // Don't place other items on Entry

        if (player != null && !player.getPosition().equals(this.entryPosition) && availableCells.contains(player.getPosition())) {
            availableCells.remove(player.getPosition());
        }

        // 2. Place Ladder (1) - must not be on Entry
        if (!availableCells.isEmpty()) {
            this.ladderPosition = availableCells.remove(0);
            setItemAt(this.ladderPosition, new Ladder());
        } else {
            for (int y_scan = 0; y_scan < height; y_scan++) {
                for (int x_scan = 0; x_scan < width; x_scan++) {
                    Position potentialLadderPos = new Position(x_scan, y_scan);
                    if (!potentialLadderPos.equals(this.entryPosition)) {
                        this.ladderPosition = potentialLadderPos;
                        setItemAt(this.ladderPosition, new Ladder());
                        break;
                    }
                }
                if (this.ladderPosition != null && !this.ladderPosition.equals(this.entryPosition)) break;
            }
            if (this.ladderPosition == null && width > 0 && height > 0) {
                this.ladderPosition = new Position(width -1, 0);
                if(this.ladderPosition.equals(this.entryPosition)) this.ladderPosition = new Position(0,0);
                setItemAt(this.ladderPosition, new Ladder());
            }
        }

        // 3. Place other items
        int goldCount = 5, trapCount = 5, healthPotionCount = 2, meleeMutantCount = 3;
        int rangedMutantCount = Math.max(0, Math.min(10, difficulty));

        for (int i = 0; i < goldCount && !availableCells.isEmpty(); i++) setItemAt(availableCells.remove(0), new Gold());
        for (int i = 0; i < trapCount && !availableCells.isEmpty(); i++) setItemAt(availableCells.remove(0), new Trap());
        for (int i = 0; i < healthPotionCount && !availableCells.isEmpty(); i++) setItemAt(availableCells.remove(0), new HealthPotion());
        for (int i = 0; i < meleeMutantCount && !availableCells.isEmpty(); i++) setItemAt(availableCells.remove(0), new MeleeMutant());
        for (int i = 0; i < rangedMutantCount && !availableCells.isEmpty(); i++) setItemAt(availableCells.remove(0), new RangedMutant());
    }

    public void displayMapText(Player player) {
        int currentDisplayLevel = 1;
        if (player.getGameEngineContext() instanceof GameEngine) {
            currentDisplayLevel = ((GameEngine) player.getGameEngineContext()).getCurrentLevel();
        }
        System.out.println("---- LEVEL " + currentDisplayLevel + " MAP ----");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (player.getPosition().equals(new Position(x, y))) {
                    System.out.print('P' + " ");
                } else {
                    System.out.print(grid[y][x].getSymbol() + " ");
                }
            }
            System.out.println();
        }
        System.out.println("-------------");
    }
}