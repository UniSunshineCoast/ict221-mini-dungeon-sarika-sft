package dungeon.engine;

import dungeon.engine.items.Empty; // For removeItemFromMap
import dungeon.engine.items.RangedMutant; // For RangedMutant specific logic

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner; // For text UI


public class GameEngine implements Serializable {
    private static final long serialVersionUID = 6L;
    private GameMap map;
    private Player player;
    private int currentLevel;
    private final int maxLevels = 2;
    private int difficulty; // User input (0-10), default 3. This is the initial difficulty.
    // It will be incremented by 2 when advancing a level.
    private final int maxStepsPerLevel = 100;
    private GameState gameState;

    private List<ScoreEntry> topScores;
    private static final String SAVE_FILE_GAME = "minidungeon.sav";
    private static final String SAVE_FILE_SCORES = "minidungeon_scores.dat";

    private static final int MAP_WIDTH = 10;
    private static final int MAP_HEIGHT = 10;
    private transient Random randomGenerator; // For RangedMutant attacks, etc. Reinitialize on load.

    public GameEngine() {

        this.topScores = new ArrayList<>();
        loadTopScoresFromFile();
        this.randomGenerator = new Random();
    }

    // Getter for GameMap to use to display level number (via player context)
    // Also for GUI to display current level
    public int getCurrentLevel() { return currentLevel; }


    private Random getRandom() {
        if (randomGenerator == null) {
            randomGenerator = new Random();
        }
        return randomGenerator;
    }

    public void startGame(int initialDifficulty) {
        this.difficulty = Math.max(0, Math.min(10, initialDifficulty)); // Clamp difficulty 0-10
        this.currentLevel = 1;
        Position startPosLvl1 = new Position(0, MAP_HEIGHT - 1); // Level 1: bottom left
        this.player = new Player(startPosLvl1);
        this.player.setGameEngineContext(this); // Give player a reference to this engine instance

        setupLevel(); // Setup map for currentLevel (1) using this.difficulty
        this.gameState = GameState.IN_PROGRESS;
        System.out.println("Game started. Initial Difficulty: " + initialDifficulty + " (Current effective difficulty for Level " + this.currentLevel + ": " + this.difficulty + ")");
    }

    private void setupLevel() {
        // Note: this.difficulty is used here. It's the initial difficulty for level 1,
        // or the incremented difficulty for subsequent levels.
        this.map = new GameMap(MAP_WIDTH, MAP_HEIGHT);
        // player.getPosition() is correct here: for L1 it's startPosLvl1, for L2+ it's the prev ladder pos.
        this.map.placeItemsRandomly(this.difficulty, this.currentLevel, this.player);
        // After map generation, ensure player object is at the map's official entry point and stats are reset.
        player.resetForNewLevel(map.getEntryPosition());
        System.out.println("Welcome to Level " + currentLevel + "! (Difficulty for this level: " + this.difficulty + ")");
    }

    public String processMove(Direction direction) {
        if (gameState != GameState.IN_PROGRESS) {
            return "Game is over or level is transitioning. Cannot move.";
        }

        StringBuilder messageBuilder = new StringBuilder();
        Position currentPos = player.getPosition();
        Position newPos = new Position(currentPos.x() + direction.getDx(), currentPos.y() + direction.getDy());

        if (!map.isValidPosition(newPos)) {
            messageBuilder.append("You tried to move ").append(direction.toString().toLowerCase()).append(" but it's a boundary wall.");
            return messageBuilder.toString();
        }

        Item targetItem = map.getCell(newPos).getItem();
        if (!targetItem.isPassable()) {
            messageBuilder.append("You tried to move ").append(direction.toString().toLowerCase()).append(" but it's a ").append(targetItem.getDescription()).append(".");
            return messageBuilder.toString();
        }

        // Player actually moves
        player.moveTo(newPos);
        player.incrementSteps();
        messageBuilder.append("You moved ").append(direction.toString().toLowerCase()).append(". ");

        // Interact with the item on the new cell
        Item itemAtNewPos = map.getCell(newPos).getItem();
        messageBuilder.append(itemAtNewPos.interact(player, this)).append(" ");


        // After player's move and interaction, check for Ranged Mutant attacks
        // Only if game still in progress (e.g., didn't just win by hitting ladder)
        if (gameState == GameState.IN_PROGRESS) {
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    Item itemOnMap = map.getCell(x, y).getItem();
                    if (itemOnMap instanceof RangedMutant) {
                        // Ensure mutant wasn't just defeated by stepping on it
                        if (player.getPosition().equals(new Position(x,y))) continue;

                        Position mutantPos = new Position(x,y);
                        int dx = Math.abs(player.getPosition().x() - mutantPos.x());
                        int dy = Math.abs(player.getPosition().y() - mutantPos.y());

                        if ((dx == 0 && dy > 0 && dy <= RangedMutant.RANGED_ATTACK_RANGE) ||
                                (dy == 0 && dx > 0 && dx <= RangedMutant.RANGED_ATTACK_RANGE)) {
                            if (getRandom().nextDouble() < RangedMutant.RANGED_ATTACK_HIT_CHANCE) {
                                player.takeDamage(RangedMutant.RANGED_ATTACK_DAMAGE);
                                messageBuilder.append("A ranged mutant at (").append(x).append(",").append(y)
                                        .append(") attacked and hit you! (-").append(RangedMutant.RANGED_ATTACK_DAMAGE).append(" HP). ");
                            } else {
                                messageBuilder.append("A ranged mutant at (").append(x).append(",").append(y)
                                        .append(") attacked, but missed. ");
                            }
                        }
                    }
                }
            }
        }

        // Check game over conditions (HP, Steps) or if game was won via Ladder interaction
        checkGameEndConditions(messageBuilder);

        return messageBuilder.toString().trim();
    }

    private void checkGameEndConditions(StringBuilder msgBuilder) {
        if (gameState == GameState.WIN_GAME) { // This state is set by advanceLevel()
            if (msgBuilder != null && !msgBuilder.toString().contains("escaped the dungeon")) {
                msgBuilder.append("You escaped the dungeon! YOU WIN! ");
            }
            addScoreToTopList(player.getScore());
            return;
        }

        if (player.getHp() <= 0) {
            gameState = GameState.LOSE_HP;
            player.setScore(-1);
            if (msgBuilder != null) msgBuilder.append("Your HP reached 0. Game Over. ");
            addScoreToTopList(player.getScore());
        } else if (player.getStepsTaken() >= maxStepsPerLevel) {
            gameState = GameState.LOSE_STEPS;
            player.setScore(-1);
            if (msgBuilder != null) msgBuilder.append("You ran out of steps. Game Over. ");
            addScoreToTopList(player.getScore());
        }
    }


    public void removeItemFromMap(Position pos) {
        map.setItemAt(pos, new Empty());
    }

    public void advanceLevel() {
        if (currentLevel < maxLevels) {
            currentLevel++;
            this.difficulty += 2; // Increase effective difficulty for the next level
            setupLevel();
            gameState = GameState.IN_PROGRESS;
        } else {
            gameState = GameState.WIN_GAME; // Player completed the final level
            // The win message is usually part of the Ladder's interact method or checkGameEndConditions
        }
    }

    public boolean isGameOver() {
        return gameState == GameState.LOSE_HP || gameState == GameState.LOSE_STEPS || gameState == GameState.WIN_GAME;
    }

    public void saveGame() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_GAME))) {
            oos.writeObject(this);
            System.out.println("Game saved successfully to " + SAVE_FILE_GAME);
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static GameEngine loadGame() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE_GAME))) {
            GameEngine loadedEngine = (GameEngine) ois.readObject();
            loadedEngine.randomGenerator = new Random();
            if (loadedEngine.player != null) {
                loadedEngine.player.setGameEngineContext(loadedEngine);
            }
            loadedEngine.loadTopScoresFromFile();
            System.out.println("Game loaded successfully from " + SAVE_FILE_GAME);
            return loadedEngine;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading game: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTopScoresFromFile() {
        File scoresFile = new File(SAVE_FILE_SCORES);
        if (scoresFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(scoresFile))) {
                this.topScores = (List<ScoreEntry>) ois.readObject();
                if (this.topScores == null) this.topScores = new ArrayList<>();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading top scores from file: " + e.getMessage());
                this.topScores = new ArrayList<>();
            }
        } else {
            this.topScores = new ArrayList<>();
        }
    }

    private void saveTopScoresToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_SCORES))) {
            oos.writeObject(this.topScores);
        } catch (IOException e) {
            System.err.println("Error saving top scores to file: " + e.getMessage());
        }
    }

    public void addScoreToTopList(int scoreOnPlayerObject) {
        // scoreOnPlayerObject is player.getScore() at the time of game end.
        // The requirement is "final score: -1" for losing.
        if (scoreOnPlayerObject == -1 && (gameState == GameState.LOSE_HP || gameState == GameState.LOSE_STEPS)) {
            System.out.println("Game lost with score -1. This score is not added to the Top 5 player scores list.");
            return; // Do not add -1 scores to the persistent top list.
        }

        // Only proceed if it's a winning score (which should be > 0)
        if (gameState != GameState.WIN_GAME || scoreOnPlayerObject <= 0) {
            // Or some other logic if you want to record other scores, but spec implies top 5 *winning* plays.
            return;
        }

        ScoreEntry newEntry = new ScoreEntry(scoreOnPlayerObject, LocalDate.now());

        // Add and sort
        topScores.add(newEntry);
        Collections.sort(topScores); // Sorts descending by score, then date

        // Keep only top 5
        while (topScores.size() > 5) {
            topScores.remove(topScores.size() - 1); // Remove lowest score
        }
        saveTopScoresToFile(); // Save the updated list

        // Check if the new score made it to the top 5 (after potential removal of others)
        boolean madeItToTop5 = false;
        for (ScoreEntry se : topScores) {
            if (se.equals(newEntry)) {
                madeItToTop5 = true;
                break;
            }
        }
        if (madeItToTop5) {
            System.out.println("Congratulations! Your score of " + newEntry.getScore() + " made it to the Top 5!");
        }
    }


    public List<ScoreEntry> getTopScores() {
        return Collections.unmodifiableList(topScores);
    }

    public GameMap getMap() { return map; }
    public Player getPlayer() { return player; }
    public GameState getGameState() { return gameState; }
    public int getDifficultySetting() { return difficulty; } // Returns the current effective difficulty for the level

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameEngine engine = null;

        System.out.println("Welcome to MiniDungeon!");

        while(true) {
            System.out.print("Choose action: (N)ew Game, (L)oad Game, (Q)uit: ");
            String initialChoice = scanner.nextLine().trim().toUpperCase();
            if (initialChoice.equals("N")) {
                System.out.print("Enter initial difficulty (0-10, default 3): ");
                String diffInput = scanner.nextLine().trim();
                int difficultySetting = 3;
                try {
                    if (!diffInput.isEmpty()) {
                        difficultySetting = Integer.parseInt(diffInput);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid difficulty format, using default 3.");
                }
                engine = new GameEngine();
                engine.startGame(difficultySetting);
                break;
            } else if (initialChoice.equals("L")) {
                GameEngine loaded = GameEngine.loadGame();
                if (loaded != null) {
                    engine = loaded;
                    System.out.println("Game loaded. Current Level: " + engine.getCurrentLevel() +
                            ", HP: " + engine.getPlayer().getHp() +
                            ", Score: " + engine.getPlayer().getScore() +
                            ", Steps: " + engine.getPlayer().getStepsTaken() + "/" + engine.maxStepsPerLevel +
                            ", Difficulty: " + engine.getDifficultySetting());
                    break;
                } else {
                    System.out.println("Failed to load game. Please try again or start a new game.");
                }
            } else if (initialChoice.equals("Q")) {
                System.out.println("Thanks for playing!");
                scanner.close();
                return;
            } else {
                System.out.println("Invalid choice. Please enter N, L, or Q.");
            }
        }

        while (engine != null && !engine.isGameOver()) {
            engine.getMap().displayMapText(engine.getPlayer());
            System.out.println(engine.getPlayer());
            System.out.println("Steps remaining in level: " + (engine.maxStepsPerLevel - engine.getPlayer().getStepsTaken()));
            System.out.println("Top Scores (from file):");
            List<ScoreEntry> currentTopScores = engine.getTopScores();
            if (currentTopScores.isEmpty()) {
                System.out.println("No top scores recorded yet.");
            } else {
                int rank = 1;
                for(ScoreEntry se : currentTopScores){
                    System.out.println("#" + rank++ + " " + se);
                }
            }

            System.out.print("Enter command (u, d, l, r, save, quit): ");
            String command = scanner.nextLine().trim().toLowerCase();
            String message = "";

            switch (command) {
                case "u": message = engine.processMove(Direction.UP); break;
                case "d": message = engine.processMove(Direction.DOWN); break;
                case "l": message = engine.processMove(Direction.LEFT); break;
                case "r": message = engine.processMove(Direction.RIGHT); break;
                case "save":
                    engine.saveGame();
                    message = "Game saved.";
                    break;
                case "quit":
                    System.out.println("Quitting game...");
                    engine.gameState = GameState.LOSE_STEPS; // Force a game over state
                    engine.player.setScore(-1); // Set score to -1 for quitting
                    message="Game quit by user.";
                    break; // Break from switch, loop condition will handle exit
                default:
                    message = "Unknown command: " + command;
                    break;
            }
            System.out.println("\n" + message + "\n");
        }

        if (engine != null) {
            System.out.println("================ GAME OVER ================");
            if (engine.getMap() != null && engine.getPlayer()!=null) engine.getMap().displayMapText(engine.getPlayer());
            if (engine.getPlayer()!=null) System.out.println(engine.getPlayer());
            System.out.println("Final Game State: " + engine.getGameState());

            if (engine.getGameState() == GameState.WIN_GAME) {
                System.out.println("CONGRATULATIONS! YOU HAVE ESCAPED THE DUNGEON!");
                System.out.println("Your final score: " + engine.getPlayer().getScore());
            } else if (engine.getPlayer() != null && engine.getPlayer().getScore() == -1) {
                System.out.println("Game ended. Final score: -1.");
            } else if (engine.getPlayer() != null) {
                System.out.println("Game ended. Final score: " + engine.getPlayer().getScore());
            }

            System.out.println("\nFinal Top 5 Player Scores:");
            List<ScoreEntry> finalTopScores = engine.getTopScores();
            if (finalTopScores.isEmpty()) {
                System.out.println("No top scores recorded yet.");
            } else {
                int rank = 1;
                for(ScoreEntry se : finalTopScores){
                    System.out.println("#" + rank++ + " " + se);
                }
            }
        }
        scanner.close();
        System.out.println("Thanks for playing MiniDungeon!");
    }
}
