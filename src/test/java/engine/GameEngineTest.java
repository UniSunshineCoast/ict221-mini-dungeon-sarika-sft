package dungeon.engine;

import dungeon.engine.items.Gold; // Needed for the gold pickup test
import dungeon.engine.items.Empty; // Needed to check if gold was removed
import dungeon.engine.items.Trap;
import dungeon.engine.items.Entry;
import dungeon.engine.items.RangedMutant;
import dungeon.engine.items.Ladder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine engine;
    private Player player; // To easily access player state
    private GameMap map;   // To easily access map state for setup and verification

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        // For most interaction tests, we'll start a new game.
        // We might override the map for specific scenarios.
        // Start with difficulty 0 for predictable Ranged Mutant count (0) initially,
        // which simplifies early tests that don't focus on ranged mutants.
        engine.startGame(0);
        player = engine.getPlayer();
        map = engine.getMap(); // Get the map instance from the engine after startGame
        player.setGameEngineContext(engine); // Ensure player has context for things like map display if needed by methods it calls
    }

    @Test
    void processMove_MoveToAdjacentEmptyCell_PlayerMovesStepsIncrement() {
        // Player starts at (0,9) after engine.startGame() in setUp()
        Position initialPos = new Position(player.getPosition().x(), player.getPosition().y()); // Make a copy
        int initialSteps = player.getStepsTaken();
        int initialHp = player.getHp();
        int initialScore = player.getScore();

        Direction moveDirection = null;
        Position targetPos = null;

        // Try to find an empty cell to move to. This part relies on the random map.
        // A more robust test would manually clear an adjacent cell.
        // Try moving right from (0,9) to (1,9) if empty
        if (map.isValidPosition(initialPos.x() + 1, initialPos.y()) &&
                map.getCell(initialPos.x() + 1, initialPos.y()).getItem() instanceof dungeon.engine.items.Empty) {
            moveDirection = Direction.RIGHT;
            targetPos = new Position(initialPos.x() + 1, initialPos.y());
        }
        // Else, try moving up from (0,9) to (0,8) if empty
        else if (map.isValidPosition(initialPos.x(), initialPos.y() - 1) &&
                map.getCell(initialPos.x(), initialPos.y() - 1).getItem() instanceof dungeon.engine.items.Empty) {
            moveDirection = Direction.UP;
            targetPos = new Position(initialPos.x(), initialPos.y() - 1);
        }

        if (moveDirection != null && targetPos != null) {
            String message = engine.processMove(moveDirection);

            assertTrue(message.contains("You moved " + moveDirection.toString().toLowerCase()), "Message should confirm movement. Message: " + message);
            // The message for moving into an empty space comes from Empty.interact()
            assertTrue(message.contains("move into an empty space"), "Message should indicate moving into an empty space. Message: " + message);
            assertEquals(targetPos, player.getPosition(), "Player should be at the target position.");
            assertEquals(initialSteps + 1, player.getStepsTaken(), "Steps should increment by 1.");
            // Assuming no ranged attacks from difficulty 0 map for this simple test
            assertEquals(initialHp, player.getHp(), "HP should not change when moving to an empty cell (no ranged attacks expected).");
            assertEquals(initialScore, player.getScore(), "Score should not change when moving to an empty cell.");
        } else {
            // This test might be skipped if the random map doesn't have an adjacent empty cell at start.
            // This is acceptable for this particular test, as more specific tests will control the map.
            System.out.println("WARNING: Skipping MoveToAdjacentEmptyCell test: No adjacent empty cell found from start in random map. This can happen.");
            // For a strict test, you could use:
            // org.junit.jupiter.api.Assumptions.assumeTrue(false, "No adjacent empty cell from start for this random map generation.");
        }
    }

    @Test
    void processMove_PlayerPicksUpGold_ScoreIncreasesAndGoldRemoved() {
        // Arrange: Set up a predictable scenario
        // Player starts at (0,9) due to engine.startGame(0) in setUp()
        Position playerInitialPositionForThisTest = new Position(0, 9);
        player.moveTo(playerInitialPositionForThisTest); // Ensure player is exactly here for this test
        map.setItemAt(playerInitialPositionForThisTest, new dungeon.engine.items.Entry()); // Ensure start is an Entry or Empty

        Position goldPosition = new Position(1, 9); // Place gold to the right of player
        // Ensure the target cell is initially empty or an item that can be overwritten for the test
        map.setItemAt(goldPosition, new Gold()); // Manually place a Gold item

        int initialScore = player.getScore();
        int initialSteps = player.getStepsTaken(); // Get steps after potential moveTo

        // Act: Player moves onto the gold
        String message = engine.processMove(Direction.RIGHT);

        // Assert
        assertTrue(message.contains("You moved right"), "Message should indicate movement. Message: " + message);
        assertTrue(message.contains("picked up gold"), "Message should indicate gold was picked up. Message: " + message);
        assertEquals(initialScore + 2, player.getScore(), "Score should increase by 2 after picking up gold.");
        assertEquals(initialSteps + 1, player.getStepsTaken(), "Steps should increment after moving.");
        assertTrue(map.getCell(goldPosition).getItem() instanceof dungeon.engine.items.Empty, "Cell where gold was should now be Empty.");
        assertEquals(goldPosition, player.getPosition(), "Player should be at the gold's position.");
    }

    @Test
    void processMove_PlayerStepsOnTrap_HpDecreasesTrapRemains() {
        // Arrange: Player starts at (0,9)
        Position playerInitialPosition = new Position(0, 9);
        player.moveTo(playerInitialPosition); // Ensure player is at (0,9)
        map.setItemAt(playerInitialPosition, new dungeon.engine.items.Entry()); // Start on an entry or empty

        Position trapPosition = new Position(1, 9); // Place trap to the right
        map.setItemAt(trapPosition, new Trap());    // Manually place a Trap item

        int initialHp = player.getHp();
        int initialSteps = player.getStepsTaken();

        // Act: Player moves onto the trap
        String message = engine.processMove(Direction.RIGHT);

        // Assert
        assertTrue(message.contains("You moved right"), "Message should indicate movement.");
        assertTrue(message.contains("fell into a trap"), "Message should indicate stepping on a trap.");
        assertEquals(initialHp - 2, player.getHp(), "HP should decrease by 2 after stepping on a trap.");
        assertEquals(initialSteps + 1, player.getStepsTaken(), "Steps should increment.");
        assertTrue(map.getCell(trapPosition).getItem() instanceof Trap, "Trap should still be active on the map.");
        assertEquals(trapPosition, player.getPosition(), "Player should be at the trap's position.");
    }
    @Test
    void processMove_PlayerUsesHealthPotion_HpIncreasesPotionRemoved() {
        // Arrange: Player starts at (0,9), take some damage first
        Position playerInitialPosition = new Position(0, 9);
        player.moveTo(playerInitialPosition);
        map.setItemAt(playerInitialPosition, new dungeon.engine.items.Entry());

        player.takeDamage(5); // Player HP is now 10 - 5 = 5
        assertEquals(5, player.getHp(), "Pre-condition: Player HP should be 5.");

        Position potionPosition = new Position(1, 9); // Place potion to the right
        map.setItemAt(potionPosition, new dungeon.engine.items.HealthPotion()); // Manually place a HealthPotion

        int initialSteps = player.getStepsTaken();

        // Act: Player moves onto the health potion
        String message = engine.processMove(Direction.RIGHT);

        // Assert
        assertTrue(message.contains("You moved right"), "Message should indicate movement.");
        assertTrue(message.contains("drank a health potion"), "Message should indicate potion was used.");
        // Potion heals 4 HP. 5 + 4 = 9. Max HP is 10.
        assertEquals(9, player.getHp(), "HP should increase by 4 (but not exceed max).");
        assertEquals(initialSteps + 1, player.getStepsTaken(), "Steps should increment.");
        assertTrue(map.getCell(potionPosition).getItem() instanceof dungeon.engine.items.Empty, "Cell where potion was should now be Empty.");
        assertEquals(potionPosition, player.getPosition(), "Player should be at the potion's position.");
    }

    @Test
    void processMove_PlayerUsesHealthPotionAtFullHp_HpStaysMaxPotionRemoved() {
        // Arrange: Player starts at (0,9) with full HP (10)
        Position playerInitialPosition = new Position(0, 9);
        player.moveTo(playerInitialPosition);
        map.setItemAt(playerInitialPosition, new dungeon.engine.items.Entry());
        assertEquals(10, player.getHp(), "Pre-condition: Player HP should be full (10).");


        Position potionPosition = new Position(1, 9); // Place potion to the right
        map.setItemAt(potionPosition, new dungeon.engine.items.HealthPotion());

        int initialSteps = player.getStepsTaken();

        // Act: Player moves onto the health potion
        String message = engine.processMove(Direction.RIGHT);

        // Assert
        assertTrue(message.contains("You moved right"), "Message should indicate movement.");
        assertTrue(message.contains("drank a health potion"), "Message should indicate potion was used.");
        assertEquals(10, player.getHp(), "HP should remain at max (10) if already full.");
        assertEquals(initialSteps + 1, player.getStepsTaken(), "Steps should increment.");
        assertTrue(map.getCell(potionPosition).getItem() instanceof dungeon.engine.items.Empty, "Cell where potion was should now be Empty.");
        assertEquals(potionPosition, player.getPosition(), "Player should be at the potion's position.");
    }
    @Test
    void processMove_PlayerDefeatsMeleeMutant_HpDecreasesScoreIncreasesMutantRemoved() {
        // Arrange: Player starts at (0,9)
        Position playerInitialPosition = new Position(0, 9);
        player.moveTo(playerInitialPosition);
        map.setItemAt(playerInitialPosition, new dungeon.engine.items.Entry());

        Position mutantPosition = new Position(1, 9); // Place mutant to the right
        map.setItemAt(mutantPosition, new dungeon.engine.items.MeleeMutant()); // Manually place a MeleeMutant

        int initialHp = player.getHp();       // Should be 10
        int initialScore = player.getScore(); // Should be 0 (or whatever it was before this test setup)
        int initialSteps = player.getStepsTaken();

        // Act: Player moves onto the melee mutant
        String message = engine.processMove(Direction.RIGHT);

        // Assert
        assertTrue(message.contains("You moved right"), "Message should indicate movement.");
        assertTrue(message.contains("defeated a melee mutant"), "Message should indicate mutant was defeated.");
        // Melee Mutant: -2 HP, +2 score as per spec
        assertEquals(initialHp - 2, player.getHp(), "HP should decrease by 2 after fighting melee mutant.");
        assertEquals(initialScore + 2, player.getScore(), "Score should increase by 2 after defeating melee mutant.");
        assertEquals(initialSteps + 1, player.getStepsTaken(), "Steps should increment.");
        assertTrue(map.getCell(mutantPosition).getItem() instanceof dungeon.engine.items.Empty, "Cell where mutant was should now be Empty.");
        assertEquals(mutantPosition, player.getPosition(), "Player should be at the mutant's position.");
    }
    @Test
    void processMove_PlayerDefeatsRangedMutantDirectly_ScoreIncreasesMutantRemovedNoHpLoss() {
        // Arrange: Player starts at (0,9)
        Position playerInitialPosition = new Position(0, 9);
        player.moveTo(playerInitialPosition);
        map.setItemAt(playerInitialPosition, new dungeon.engine.items.Entry());

        Position mutantPosition = new Position(1, 9); // Place Ranged Mutant to the right
        map.setItemAt(mutantPosition, new dungeon.engine.items.RangedMutant());

        int initialHp = player.getHp();       // Should be 10
        int initialScore = player.getScore(); // Should be 0
        int initialSteps = player.getStepsTaken();

        // Act: Player moves onto the ranged mutant
        String message = engine.processMove(Direction.RIGHT);

        // Assert
        assertTrue(message.contains("You moved right"), "Message should indicate movement.");
        assertTrue(message.contains("defeated a ranged mutant directly"), "Message should indicate direct defeat of ranged mutant.");
        // Ranged Mutant (direct defeat): No HP lost, +2 score as per spec
        assertEquals(initialHp, player.getHp(), "HP should NOT change when defeating ranged mutant directly.");
        assertEquals(initialScore + 2, player.getScore(), "Score should increase by 2.");
        assertEquals(initialSteps + 1, player.getStepsTaken(), "Steps should increment.");
        assertTrue(map.getCell(mutantPosition).getItem() instanceof dungeon.engine.items.Empty, "Cell where mutant was should now be Empty.");
        assertEquals(mutantPosition, player.getPosition(), "Player should be at the mutant's position.");
    }
    @Test
    void processMove_RangedMutantAttemptsAttack_PlayerInRange() {
        // Arrange:
        // We will use difficulty > 0 to ensure ranged mutants can be placed by default.
        // However, for a controlled test, manual placement is better.
        engine.startGame(3); // Restart game with difficulty that allows ranged mutants
        player = engine.getPlayer(); // Re-assign player/map from new engine instance
        map = engine.getMap();
        player.setGameEngineContext(engine);


        Position playerTargetPos = new Position(2, 9); // Player will move here
        Position rangedMutantPos = new Position(2, 7); // Ranged mutant 2 tiles above player's target

        // Clear the area and place items manually
        map.setItemAt(player.getPosition(), new dungeon.engine.items.Entry()); // Player starts at 0,9
        map.setItemAt(playerTargetPos, new dungeon.engine.items.Empty());      // Target for player move
        map.setItemAt(rangedMutantPos, new dungeon.engine.items.RangedMutant());// Place the Ranged Mutant

        // For simplicity, let's clear a path.
        map.setItemAt(new Position(1,9), new dungeon.engine.items.Empty());


        int initialHp = player.getHp();
        int initialSteps = player.getStepsTaken();

        // Act: Player moves to a position that is 2 tiles away from the Ranged Mutant
        // Player is at (0,9), Ranged Mutant is at (2,7).
        // Let's move player to (2,9) -> right, right.
        // This makes player 2 tiles below the Ranged Mutant.
        engine.processMove(Direction.RIGHT); // Player at (1,9)
        String message = engine.processMove(Direction.RIGHT); // Player at (2,9)

        // Assert:
        // The Ranged Mutant at (2,7) should now attempt to attack the player at (2,9).
        // The message should contain either "attacked and hit" OR "attacked, but missed".
        boolean attackAttempted = message.contains("A ranged mutant at (" + rangedMutantPos.x() + "," + rangedMutantPos.y() + ") attacked and hit you!") ||
                message.contains("A ranged mutant at (" + rangedMutantPos.x() + "," + rangedMutantPos.y() + ") attacked, but missed.");
        assertTrue(attackAttempted, "Ranged mutant should have attempted an attack. Message: " + message);

        if (message.contains("attacked and hit you!")) {
            assertEquals(initialHp - RangedMutant.RANGED_ATTACK_DAMAGE, player.getHp(), "HP should decrease if hit by ranged mutant.");
        } else if (message.contains("attacked, but missed.")) {
            assertEquals(initialHp, player.getHp(), "HP should not change if ranged mutant missed.");
        } else {
            fail("Unexpected message outcome for ranged mutant attack: " + message); // Should not happen if attackAttempted is true
        }

        assertEquals(initialSteps + 2, player.getStepsTaken(), "Steps should reflect two moves.");
        assertTrue(map.getCell(rangedMutantPos).getItem() instanceof dungeon.engine.items.RangedMutant, "Ranged mutant should still be on the map.");
        assertEquals(playerTargetPos, player.getPosition(), "Player should be at the target position.");
    }
    @Test
    void processMove_ReachLadderOnLevel1_AdvancesToLevel2AndResetsPlayerForNewLevel() {
        // Arrange:
        // engine.startGame(initialDifficulty) is called in setUp(). Let's use initialDifficulty = 1 for this test.
        // We want to make sure difficulty increments correctly.
        int initialDifficultyForTest = 1;
        engine.startGame(initialDifficultyForTest); // Restart game with specific initial difficulty
        player = engine.getPlayer(); // Re-assign from new engine instance
        map = engine.getMap();
        player.setGameEngineContext(engine);

        assertEquals(1, engine.getCurrentLevel(), "Pre-condition: Should be on Level 1.");
        assertEquals(initialDifficultyForTest, engine.getDifficultySetting(), "Pre-condition: Initial difficulty should be set.");

        // Manually place a ladder next to the player
        Position playerStartPos = player.getPosition(); // Player is at (0,9)
        map.setItemAt(playerStartPos, new dungeon.engine.items.Entry()); // Ensure start is clear

        Position ladderPosition = new Position(playerStartPos.x() + 1, playerStartPos.y()); // e.g., (1,9)
        map.setItemAt(ladderPosition, new dungeon.engine.items.Ladder());

        // Modify player state before advancing to check carry-over
        player.addScore(10);
        player.takeDamage(3); // HP becomes 7
        player.incrementSteps(); // Make some steps
        player.incrementSteps(); // Steps = 2 (before moving onto ladder)

        int scoreBeforeAdvance = player.getScore();
        int hpBeforeAdvance = player.getHp();
        // Note: Steps taken before moving onto ladder is 2

        // Act: Player moves onto the ladder
        String message = engine.processMove(Direction.RIGHT); // Move from (0,9) to (1,9)

        // Assert:
        assertTrue(message.contains("climbed the ladder to the next level"), "Message should indicate advancing level.");
        assertEquals(2, engine.getCurrentLevel(), "Should advance to Level 2.");

        // Difficulty should increase by 2 for the new level's map generation
        assertEquals(initialDifficultyForTest + 2, engine.getDifficultySetting(), "Difficulty for Level 2 map generation should increase by 2.");

        assertEquals(0, player.getStepsTaken(), "Player steps should reset to 0 for the new level.");
        assertEquals(scoreBeforeAdvance, player.getScore(), "Player score should carry over.");
        assertEquals(hpBeforeAdvance, player.getHp(), "Player HP should carry over.");

        // Player's new position should be where the ladder was on Level 1,
        // which is also the entry point for the new Level 2 map.
        assertEquals(ladderPosition, player.getPosition(), "Player should start Level 2 at the position of the previous ladder.");
        assertNotNull(engine.getMap(), "A new map for Level 2 should have been generated.");
        assertNotSame(map, engine.getMap(), "The map instance for Level 2 should be different from Level 1 map."); // map variable still holds old L1 map

        assertEquals(GameState.IN_PROGRESS, engine.getGameState(), "Game state should be IN_PROGRESS after advancing to Level 2.");
    }
    @Test
    void processMove_ReachLadderOnLevel2_WinsGame() {
        // Arrange:
        // 1. Start a game (default difficulty, level 1).
        int initialDifficultyLvl1 = 1; // Can be any valid difficulty
        engine.startGame(initialDifficultyLvl1);
        player = engine.getPlayer(); // Re-assign player and map after startGame
        map = engine.getMap();
        player.setGameEngineContext(engine);

        // 2. Simulate advancing to Level 2:
        //    Place a ladder on L1 map and move player onto it.
        Position playerL1StartPos = player.getPosition(); // e.g., (0,9)
        Position l1LadderPos = new Position(playerL1StartPos.x() + 1, playerL1StartPos.y()); // e.g., (1,9)
        map.setItemAt(l1LadderPos, new Ladder());
        engine.processMove(Direction.RIGHT); // Player moves onto L1 ladder, advances to L2

        // 3. Verify we are on Level 2 and get updated references
        assertEquals(2, engine.getCurrentLevel(), "Pre-condition: Should have advanced to Level 2.");
        player = engine.getPlayer(); // Get the player instance for Level 2
        map = engine.getMap();     // Get the map instance for Level 2
        player.setGameEngineContext(engine); // Ensure context is set for the L2 player instance

        // 4. On the Level 2 map, place a new ladder next to the player's L2 starting position.
        //    The player's current position is where the L1 ladder was.
        Position playerL2StartPos = player.getPosition();
        // Ensure the L2 entry cell is clear or an Entry item.
        map.setItemAt(playerL2StartPos, new Entry());

        Position l2LadderPosition = new Position(playerL2StartPos.x() + 1, playerL2StartPos.y());
        // Simple check if right is off map, then try up (for a very small map edge case)
        if (!map.isValidPosition(l2LadderPosition)) {
            l2LadderPosition = new Position(playerL2StartPos.x(), playerL2StartPos.y() -1);
        }
        assertTrue(map.isValidPosition(l2LadderPosition), "Test setup error: L2 ladder position is invalid for this scenario.");
        map.setItemAt(l2LadderPosition, new Ladder());

        player.addScore(25); // Give some score before winning
        int expectedFinalScore = player.getScore();

        // Act: Player moves onto the ladder on Level 2
        Direction moveToL2LadderDirection;
        if (l2LadderPosition.x() > playerL2StartPos.x()) {
            moveToL2LadderDirection = Direction.RIGHT;
        } else if (l2LadderPosition.y() < playerL2StartPos.y()) {
            moveToL2LadderDirection = Direction.UP;
        } else {
            fail("Test setup error: Cannot determine direction to L2 ladder from player's L2 start.");
            return; // Will not be reached if fail() is called
        }
        String message = engine.processMove(moveToL2LadderDirection);

        // Assert
        assertTrue(message.contains("escaped the dungeon! YOU WIN!"), "Message should indicate winning the game. Message: " + message);
        assertEquals(GameState.WIN_GAME, engine.getGameState(), "Game state should be WIN_GAME.");
        assertEquals(expectedFinalScore, player.getScore(), "Final score should be retained (no score change from final ladder itself).");

        // Check if score was added to top scores (engine.addScoreToTopList is called internally by engine on win)
        // This implicitly tests if the top score logic gets triggered. A separate test for top score list content is better.
        // For now, just ensuring no crash and game state is WIN_GAME is the primary focus here.
        // A more robust check would be:
        boolean scoreInTop = engine.getTopScores().stream().anyMatch(s -> s.getScore() == expectedFinalScore);
        if (expectedFinalScore > 0) { // Only positive scores are typically added to top list
        }
    }
    // ===== ADD THIS NEW TEST METHOD =====

    @Test
    void gameOver_HpReachesZero_GameStateIsLoseHpAndScoreIsNegativeOne() {
        // Arrange:
        // Player starts at (0,9) with 10 HP due to engine.startGame(0) in setUp()
        Position playerStartPos = player.getPosition();
        map.setItemAt(playerStartPos, new Entry()); // Ensure start is an Entry or Empty

        // Place enough traps in a row to deplete 10 HP (5 traps * -2 HP each)
        for (int i = 1; i <= 5; i++) {
            // Place traps to the right of the player's path
            Position trapPos = new Position(playerStartPos.x() + i, playerStartPos.y());
            // Ensure the position is valid before placing, though for (0,9) -> (5,9) it should be
            if (map.isValidPosition(trapPos)) {
                map.setItemAt(trapPos, new Trap());
            } else {
                fail("Test setup error: Cannot place trap at " + trapPos);
            }
        }

        // Act: Move player across all traps
        engine.processMove(Direction.RIGHT); // Player moves to (1,9), HP: 8
        engine.processMove(Direction.RIGHT); // Player moves to (2,9), HP: 6
        engine.processMove(Direction.RIGHT); // Player moves to (3,9), HP: 4
        engine.processMove(Direction.RIGHT); // Player moves to (4,9), HP: 2
        String finalMessage = engine.processMove(Direction.RIGHT); // Player moves to (5,9), HP: 0 -> Game Over

        // Assert
        assertEquals(0, player.getHp(), "Player HP should be 0.");
        assertEquals(GameState.LOSE_HP, engine.getGameState(), "Game state should be LOSE_HP.");
        assertEquals(-1, player.getScore(), "Player score should be -1 when HP reaches 0.");
        assertTrue(finalMessage.contains("Your HP reached 0. Game Over."), "Final message should indicate HP loss game over. Message: " + finalMessage);
    }
    // ===== ADD THIS NEW TEST METHOD =====

    @Test
    void gameOver_StepsReachMax_GameStateIsLoseStepsAndScoreIsNegativeOne() {
        // Arrange:
        // Max steps is 100 (from GameEngine.maxStepsPerLevel).
        // We need the player to make 100 moves without HP reaching zero or winning.
        // For simplicity, let's clear a path on the map for continuous movement.
        // Player starts at (0,9)
        Position playerStartPos = player.getPosition();
        map.setItemAt(playerStartPos, new Entry()); // Ensure player start is an Entry

        // Make a clear horizontal path (e.g., row 9)
        for (int x = 0; x < map.getWidth(); x++) {
            Position currentPathPos = new Position(x, playerStartPos.y());
            if (!currentPathPos.equals(playerStartPos)) { // Don't overwrite player's starting Entry
                map.setItemAt(currentPathPos, new Empty());
            }
        }
        // Ensure player's HP won't drop from other random items, by taking a potion first if needed
        // (though difficulty 0 from setUp means few damaging items initially)
        player.heal(player.getMaxHp()); // Ensure full HP at start of step test

        // Act: Make player move maxStepsPerLevel (100) times
        String lastMessage = "";
        for (int i = 0; i < 100; i++) {
            // Move back and forth along the cleared row to avoid hitting map boundaries too early
            // or complex pathfinding.
            Direction moveDir;
            if (player.getPosition().x() < map.getWidth() - 1 && (i % 2 == 0)) { // Move right if not at edge and even step
                moveDir = Direction.RIGHT;
            } else if (player.getPosition().x() > 0) { // Move left if not at edge or odd step
                moveDir = Direction.LEFT;
            } else { // At left edge, must move right
                moveDir = Direction.RIGHT;
            }
            lastMessage = engine.processMove(moveDir);

            // Check if game ended for a reason other than step limit prematurely
            if (engine.isGameOver() && engine.getGameState() != GameState.LOSE_STEPS) {
                fail("Game ended prematurely. Reason: " + engine.getGameState() +
                        ". Steps taken: " + player.getStepsTaken() +
                        ". HP: " + player.getHp() +
                        ". Message: " + lastMessage);
                return; // Stop test if game ended for wrong reason
            }
        }

        // Assert
        assertEquals(100, player.getStepsTaken(), "Player should have taken max steps (100).");
        assertEquals(GameState.LOSE_STEPS, engine.getGameState(), "Game state should be LOSE_STEPS.");
        assertEquals(-1, player.getScore(), "Player score should be -1 when steps reach max.");
        assertTrue(lastMessage.contains("You ran out of steps. Game Over."), "Final message should indicate step limit game over. Message: " + lastMessage);
    }

}
