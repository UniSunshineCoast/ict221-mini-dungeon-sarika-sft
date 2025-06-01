package dungeon.engine;

import dungeon.engine.items.Entry;
import dungeon.engine.items.Gold; // Added for count test
import dungeon.engine.items.Ladder; // Added for count test
import dungeon.engine.items.Wall; // Example item for testing set/get
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameMapTest {

    private GameMap map;
    private final int DEFAULT_WIDTH = 10;
    private final int DEFAULT_HEIGHT = 10;

    @BeforeEach
    void setUp() {
        // Create a new map for each test
        map = new GameMap(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Test
    void constructor_CreatesGridOfCorrectSize() {
        assertNotNull(map, "Map should not be null after construction.");
        assertEquals(DEFAULT_WIDTH, map.getWidth(), "Map width should match constructor argument.");
        assertEquals(DEFAULT_HEIGHT, map.getHeight(), "Map height should match constructor argument.");
    }

    @Test
    void isValidPosition_WithValidCoordinates_ReturnsTrue() {
        assertTrue(map.isValidPosition(0, 0), "Position (0,0) should be valid.");
        assertTrue(map.isValidPosition(5, 5), "Position (5,5) should be valid.");
        assertTrue(map.isValidPosition(DEFAULT_WIDTH - 1, DEFAULT_HEIGHT - 1), "Boundary corner should be valid.");
        assertTrue(map.isValidPosition(new Position(3, 7)), "Position object (3,7) should be valid.");
    }

    @Test
    void isValidPosition_WithInvalidCoordinates_ReturnsFalse() {
        assertFalse(map.isValidPosition(-1, 0), "Position (-1,0) should be invalid.");
        assertFalse(map.isValidPosition(0, -1), "Position (0,-1) should be invalid.");
        assertFalse(map.isValidPosition(DEFAULT_WIDTH, 0), "Position (width,0) should be invalid.");
        assertFalse(map.isValidPosition(0, DEFAULT_HEIGHT), "Position (0,height) should be invalid.");
        assertFalse(map.isValidPosition(new Position(DEFAULT_WIDTH + 5, DEFAULT_HEIGHT + 5)), "Out of bounds Position object should be invalid.");
    }

    @Test
    void getCell_WithValidCoordinates_ReturnsCellAndItemIsInitiallyEmpty() {
        Cell cell = map.getCell(0, 0);
        assertNotNull(cell, "getCell(0,0) should return a non-null Cell.");
        assertTrue(cell.getItem() instanceof dungeon.engine.items.Empty, "Initially, cell item should be Empty.");

        Cell cell2 = map.getCell(new Position(5,5));
        assertNotNull(cell2, "getCell with Position object should return a non-null Cell.");
        assertTrue(cell2.getItem() instanceof dungeon.engine.items.Empty, "Initially, cell item should be Empty.");
    }

    @Test
    void getCell_WithInvalidCoordinates_ReturnsNull() {
        assertNull(map.getCell(-1, 0), "getCell(-1,0) should return null for invalid X.");
        assertNull(map.getCell(0, -1), "getCell(0,-1) should return null for invalid Y.");
        assertNull(map.getCell(DEFAULT_WIDTH, DEFAULT_HEIGHT), "getCell(width,height) should return null for out of bounds.");
    }

    @Test
    void setItemAt_And_GetItem_WorkCorrectly() {
        Position testPos = new Position(3, 4);
        Wall wallItem = new Wall();

        map.setItemAt(testPos, wallItem);
        Cell cell = map.getCell(testPos);

        assertNotNull(cell, "Cell at test position should not be null.");
        assertSame(wallItem, cell.getItem(), "Item retrieved from cell should be the same item that was set.");
    }

    @Test
    void setItemAt_Ladder_UpdatesLadderPosition() {
        Position ladderPos = new Position(7, 8);
        Ladder ladderItem = new Ladder();

        assertNull(map.getLadderPosition(), "Ladder position should be null initially (before placing a ladder).");
        map.setItemAt(ladderPos, ladderItem);
        assertEquals(ladderPos, map.getLadderPosition(), "Ladder position should be updated after setting a Ladder item.");
    }

    @Test
    void placeItemsRandomly_Level1_SetsCorrectEntryPosition() {
        Player dummyPlayer = new Player(new Position(0,0)); // Dummy player for method signature
        map.placeItemsRandomly(3, 1, dummyPlayer); // Difficulty 3, Level 1

        Position expectedEntry = new Position(0, DEFAULT_HEIGHT - 1); // Bottom-left
        assertEquals(expectedEntry, map.getEntryPosition(), "Level 1 entry should be bottom-left.");
        assertTrue(map.getCell(expectedEntry).getItem() instanceof Entry, "Cell at entry position should contain an Entry item.");
    }

    @Test
    void placeItemsRandomly_PlacesOneLadder() {
        Player dummyPlayer = new Player(new Position(0,0));
        map.placeItemsRandomly(3, 1, dummyPlayer);

        assertNotNull(map.getLadderPosition(), "Ladder position should be set after random placement.");
        // Ensure the item at the recorded ladder position is actually a Ladder
        assertTrue(map.getCell(map.getLadderPosition()).getItem() instanceof Ladder, "Item at stored ladder position should be a Ladder.");

        // Count ladders to ensure only one
        int ladderCount = 0;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getCell(x, y).getItem() instanceof Ladder) {
                    ladderCount++;
                }
            }
        }
        assertEquals(1, ladderCount, "There should be exactly one ladder on the map.");
    }

    @Test
    void placeItemsRandomly_PlacesCorrectNumberOfGold() {
        Player dummyPlayer = new Player(new Position(0,0));
        map.placeItemsRandomly(3, 1, dummyPlayer); // Difficulty 3, Level 1

        int goldCount = 0;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getCell(x, y) != null && map.getCell(x, y).getItem() instanceof Gold) {
                    goldCount++;
                }
            }
        }
        assertEquals(5, goldCount, "There should be 5 gold items on the map.");
    }

    @Test
    void placeItemsRandomly_EntryAndLadderNotOverlapping() {
        Player dummyPlayer = new Player(new Position(0,0));
        // Run multiple times to increase chance of catching overlap if logic is flawed
        for (int i=0; i < 20; i++) { // Increased iterations for better chance
            map = new GameMap(DEFAULT_WIDTH, DEFAULT_HEIGHT); // Fresh map for each iteration
            map.placeItemsRandomly(3, 1, dummyPlayer);
            assertNotNull(map.getEntryPosition(), "Entry position must be set. Iteration: " + i);
            assertNotNull(map.getLadderPosition(), "Ladder position must be set. Iteration: " + i);
            assertNotEquals(map.getEntryPosition(), map.getLadderPosition(),
                    "Entry and Ladder should not be at the same position. Iteration: " + i);
        }
    }
    // Add these new test methods inside GameMapTest class

    @Test
    void placeItemsRandomly_PlacesCorrectNumberOfTraps() {
        Player dummyPlayer = new Player(new Position(0,0));
        map.placeItemsRandomly(3, 1, dummyPlayer); // Difficulty 3, Level 1

        int trapCount = 0;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getCell(x, y) != null && map.getCell(x, y).getItem() instanceof dungeon.engine.items.Trap) {
                    trapCount++;
                }
            }
        }
        assertEquals(5, trapCount, "There should be 5 trap items on the map.");
    }

    @Test
    void placeItemsRandomly_PlacesCorrectNumberOfHealthPotions() {
        Player dummyPlayer = new Player(new Position(0,0));
        map.placeItemsRandomly(3, 1, dummyPlayer); // Difficulty 3, Level 1

        int potionCount = 0;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getCell(x, y) != null && map.getCell(x, y).getItem() instanceof dungeon.engine.items.HealthPotion) {
                    potionCount++;
                }
            }
        }
        assertEquals(2, potionCount, "There should be 2 health potion items on the map.");
    }

    @Test
    void placeItemsRandomly_PlacesCorrectNumberOfMeleeMutants() {
        Player dummyPlayer = new Player(new Position(0,0));
        map.placeItemsRandomly(3, 1, dummyPlayer); // Difficulty 3, Level 1

        int meleeMutantCount = 0;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getCell(x, y) != null && map.getCell(x, y).getItem() instanceof dungeon.engine.items.MeleeMutant) {
                    meleeMutantCount++;
                }
            }
        }
        assertEquals(3, meleeMutantCount, "There should be 3 melee mutant items on the map.");
    }

    @Test
    void placeItemsRandomly_PlacesCorrectNumberOfRangedMutantsBasedOnDifficulty() {
        Player dummyPlayer = new Player(new Position(0,0));
        int testDifficulty = 5; // Test with a specific difficulty
        map.placeItemsRandomly(testDifficulty, 1, dummyPlayer); // Level 1

        int rangedMutantCount = 0;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getCell(x, y) != null && map.getCell(x, y).getItem() instanceof dungeon.engine.items.RangedMutant) {
                    rangedMutantCount++;
                }
            }
        }
        // According to your GameMap logic, rangedMutantCount = Math.max(0, Math.min(10, difficulty));
        int expectedRangedMutants = Math.max(0, Math.min(10, testDifficulty));
        assertEquals(expectedRangedMutants, rangedMutantCount, "Number of ranged mutants should match difficulty (clamped 0-10).");
    }
}
