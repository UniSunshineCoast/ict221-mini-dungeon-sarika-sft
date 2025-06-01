package dungeon.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;
    private Position startPosition;

    @BeforeEach
    void setUp() {
        startPosition = new Position(5, 5);
        player = new Player(startPosition);
    }

    @Test // This annotation marks this method as a test case
    void constructor_InitializesPlayerCorrectly() {
        // Assertions check if the actual values match the expected values
        assertEquals(startPosition, player.getPosition(), "Player should be at the starting position after construction.");
        assertEquals(10, player.getMaxHp(), "Max HP should be initialized to 10.");
        assertEquals(10, player.getHp(), "Current HP should be initialized to max HP (10).");
        assertEquals(0, player.getScore(), "Initial score should be 0.");
        assertEquals(0, player.getStepsTaken(), "Initial steps taken should be 0.");
    }

    @Test
    void moveTo_ChangesPlayerPosition() {
        Position newPosition = new Position(6, 7); // A different position
        player.moveTo(newPosition);
        assertEquals(newPosition, player.getPosition(), "Player position should be updated to the new position.");
    }

    @Test
    void incrementSteps_IncreasesStepsByOne() {
        int initialSteps = player.getStepsTaken();
        player.incrementSteps();
        assertEquals(initialSteps + 1, player.getStepsTaken(), "Steps taken should increase by 1.");
    }

    @Test
    void takeDamage_ReducesHp() {
        // Player HP is 10 from setUp()
        int damageAmount = 3;
        player.takeDamage(damageAmount);
        assertEquals(10 - damageAmount, player.getHp(), "HP should be reduced by damage amount.");
    }

    @Test
    void takeDamage_HpDoesNotGoBelowZero() {
        player.takeDamage(player.getMaxHp() + 5); // Damage more than current HP (e.g., 15 damage on 10 HP)
        assertEquals(0, player.getHp(), "HP should not go below zero.");
    }

    @Test
    void heal_IncreasesHpWhenBelowMax() {
        player.takeDamage(7); // Player HP is now 10 - 7 = 3
        int healAmount = 4;
        player.heal(healAmount);
        assertEquals(3 + healAmount, player.getHp(), "HP should be increased by heal amount.");
    }

    @Test
    void heal_HpDoesNotExceedMaxHp() {
        player.takeDamage(3); // Player HP is now 10 - 3 = 7
        player.heal(5); // Healing 5 would take it to 12, but should cap at 10
        assertEquals(player.getMaxHp(), player.getHp(), "HP should not exceed max HP.");
    }

    @Test
    void heal_HpStaysAtMaxHpIfAlreadyFull() {
        // Player HP is 10 from setUp()
        player.heal(5);
        assertEquals(player.getMaxHp(), player.getHp(), "HP should remain at max HP if already full and healed.");
    }

    @Test
    void addScore_IncreasesScore() {
        int initialScore = player.getScore(); // Should be 0 from setUp()
        int scoreToAdd = 50;
        player.addScore(scoreToAdd);
        assertEquals(initialScore + scoreToAdd, player.getScore(), "Score should be increased by the added amount.");
    }

    @Test
    void setScore_UpdatesScoreCorrectly() {
        player.setScore(-1); // As per game losing condition
        assertEquals(-1, player.getScore(), "Score should be set to the new value.");
    }

    @Test
    void resetForNewLevel_ResetsPositionAndSteps_KeepsHpAndScore() {
        // Given: Modify player state
        player.moveTo(new Position(1,1));
        player.incrementSteps();
        player.incrementSteps(); // Steps = 2
        player.addScore(20);     // Score = 20
        player.takeDamage(3);    // HP = 7 (from initial 10)

        Position newLevelStartPosition = new Position(0, 9); // Example new start

        // When: resetForNewLevel is called
        player.resetForNewLevel(newLevelStartPosition);

        // Then: Assert correct states
        assertEquals(newLevelStartPosition, player.getPosition(), "Position should reset to new level's start.");
        assertEquals(0, player.getStepsTaken(), "Steps should reset to 0 for the new level.");
        assertEquals(7, player.getHp(), "HP should be retained from previous level.");
        assertEquals(20, player.getScore(), "Score should be retained from previous level.");
    }
}
