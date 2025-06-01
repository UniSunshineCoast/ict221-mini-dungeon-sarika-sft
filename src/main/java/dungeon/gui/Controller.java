package dungeon.gui;

import dungeon.engine.Cell;
import dungeon.engine.Direction;
import dungeon.engine.GameEngine;
import dungeon.engine.GameState;
import dungeon.engine.Item;
import dungeon.engine.Player;
import dungeon.engine.Position;
import dungeon.engine.ScoreEntry;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
// Note: Imports for Image, ImageView, InputStream, StackPane, Text are NOT needed for this text-symbol version.

import java.util.List;
import java.util.Optional;

public class Controller {

    // FXML Injected Fields (must match fx:id in your FXML file)
    @FXML private GridPane gameGridPane;
    @FXML private Label hpLabel;
    @FXML private Label scoreLabel;
    @FXML private Label stepsLabel;
    @FXML private Button upButton;
    @FXML private Button downButton;
    @FXML private Button leftButton;
    @FXML private Button rightButton;
    @FXML private Button newGameButton;
    @FXML private Button saveButton;
    @FXML private Button loadButton;
    @FXML private Button helpButton;
    @FXML private TextArea statusTextArea;
    @FXML private ListView<String> topScoresListView;

    private GameEngine engine;
    private Player player; // Instance variable to hold the current player
    private static final int CELL_SIZE = 40; // Used for Label preferred size, adjust if needed

    public Controller() {
        // System.out.println("Controller constructor called.");
    }

    @FXML
    public void initialize() {
        // System.out.println("Controller initialize called.");
        engine = new GameEngine();
        promptForDifficultyAndStartGame();
    }

    private void promptForDifficultyAndStartGame() {
        TextInputDialog dialog = new TextInputDialog("3");
        dialog.setTitle("New Game");
        dialog.setHeaderText("Welcome to MiniDungeon!");
        dialog.setContentText("Please enter game difficulty (0-10):");

        Optional<String> result = dialog.showAndWait();
        int difficulty = 3;

        if (result.isPresent() && !result.get().isEmpty()){
            try {
                difficulty = Integer.parseInt(result.get());
                difficulty = Math.max(0, Math.min(10, difficulty));
            } catch (NumberFormatException e) {
                statusTextArea.appendText("Invalid difficulty format. Using default difficulty 3.\n");
                difficulty = 3;
            }
        } else {
            statusTextArea.appendText("New game setup cancelled or no input. Using default difficulty 3.\n");
        }

        engine.startGame(difficulty);
        this.player = engine.getPlayer();
        if (this.player != null) {
            this.player.setGameEngineContext(engine); // If your Player class uses this
        }
        statusTextArea.clear();
        statusTextArea.appendText("New game started with difficulty: " + engine.getDifficultySetting() + ".\n");
        updateGui();
    }

    private void updateGui() {
        if (engine == null) {
            statusTextArea.appendText("Critical Error: Game engine is not initialized.\n");
            disableAllControls(true); return;
        }
        if (this.player == null) {
            statusTextArea.appendText("Status: Player object not initialized.\n");
            disableAllControls(true); if(gameGridPane != null) gameGridPane.getChildren().clear(); return;
        }
        if (engine.getMap() == null) {
            statusTextArea.appendText("Critical Error: Game map is not initialized.\n");
            disableAllControls(true); if(gameGridPane != null) gameGridPane.getChildren().clear(); return;
        }
        disableAllControls(false);

        hpLabel.setText("HP: " + this.player.getHp() + "/" + this.player.getMaxHp());
        scoreLabel.setText("Score: " + this.player.getScore());
        int maxSteps = 100; // As per game spec
        int stepsRemaining = maxSteps - this.player.getStepsTaken();
        stepsLabel.setText("Steps Left: " + stepsRemaining);

        gameGridPane.getChildren().clear();
        for (int y = 0; y < engine.getMap().getHeight(); y++) {
            for (int x = 0; x < engine.getMap().getWidth(); x++) {
                Position currentCellPosition = new Position(x, y);
                Cell cell = engine.getMap().getCell(currentCellPosition);
                Item itemOnCell = (cell != null) ? cell.getItem() : new dungeon.engine.items.Empty();
                if (itemOnCell == null) itemOnCell = new dungeon.engine.items.Empty();

                String symbolToShow;
                if (this.player.getPosition().equals(currentCellPosition)) {
                    symbolToShow = "P"; // Player symbol
                } else {
                    symbolToShow = String.valueOf(itemOnCell.getSymbol());
                }

                Label cellTextLabel = new Label(symbolToShow);
                cellTextLabel.setPrefSize(CELL_SIZE, CELL_SIZE);
                cellTextLabel.setStyle("-fx-alignment: center; -fx-font-weight: bold; -fx-border-color: #CCCCCC; -fx-font-size: 14px;");
                // Example of changing color for player:
                if (symbolToShow.equals("P")) {
                    cellTextLabel.setStyle("-fx-alignment: center; -fx-font-weight: bold; -fx-border-color: #CCCCCC; -fx-text-fill: blue; -fx-font-size: 16px;");
                }
                gameGridPane.add(cellTextLabel, x, y);
            }
        }

        updateTopScoresView();

        if (engine.isGameOver()) {
            disableMovementButtons(true);
            String endMessage = "GAME OVER! ";
            if (engine.getGameState() == GameState.WIN_GAME) {
                endMessage += "You escaped the dungeon! Final Score: " + this.player.getScore();
                if (this.player.getScore() > 0) {
                    boolean isNewTopScore = engine.getTopScores().stream()
                            .anyMatch(se -> se.getScore() == this.player.getScore() &&
                                    se.getDate().equals(java.time.LocalDate.now()));
                    if (isNewTopScore) { // This check is a bit simplistic for "new" top score.
                        // A better check would be if this score IS NOW in the top 5
                        // and wasn't there before, or if it displaced a lower score.
                        // The engine's addScoreToTopList should handle the logic of adding it.
                        showCongratulatoryMessage("Congratulations! Your score of " + this.player.getScore() + " is a Top 5 score!");
                    }
                }
            } else if (engine.getGameState() == GameState.LOSE_HP) {
                endMessage += "Your HP reached 0.";
            } else if (engine.getGameState() == GameState.LOSE_STEPS) {
                endMessage += "You ran out of steps.";
            }
            statusTextArea.appendText(endMessage + "\n");
        } else {
            disableMovementButtons(false);
        }
    }

    private void showCongratulatoryMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("High Score!");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void disableAllControls(boolean disable) {
        disableMovementButtons(disable);
        // Also disable game action buttons if the game is over, except maybe "New Game" and "Help"
        if (newGameButton != null) newGameButton.setDisable(false); // New Game should always be enabled unless mid-action
        if (saveButton != null) saveButton.setDisable(disable);
        if (loadButton != null) loadButton.setDisable(disable);
        // if (helpButton != null) helpButton.setDisable(disable); // Help usually stays enabled
    }

    private void updateTopScoresView() {
        topScoresListView.getItems().clear();
        if (engine != null) {
            List<ScoreEntry> scores = engine.getTopScores(); // Assumes this list is managed by engine
            if (scores != null) {
                for (int i = 0; i < scores.size(); i++) {
                    topScoresListView.getItems().add("#" + (i + 1) + " " + scores.get(i).toString());
                }
            }
        }
    }

    private void disableMovementButtons(boolean disable) {
        if (upButton != null) upButton.setDisable(disable);
        if (downButton != null) downButton.setDisable(disable);
        if (leftButton != null) leftButton.setDisable(disable);
        if (rightButton != null) rightButton.setDisable(disable);
    }

    private void processPlayerMove(Direction direction) {
        if (engine != null && !engine.isGameOver() && this.player != null) {
            String message = engine.processMove(direction);
            statusTextArea.appendText(message + "\n"); // Append message to status area
            updateGui(); // Refresh the entire GUI
        }
    }

    // Event Handlers for FXML Buttons
    @FXML private void handleMoveUp() { processPlayerMove(Direction.UP); }
    @FXML private void handleMoveDown() { processPlayerMove(Direction.DOWN); }
    @FXML private void handleMoveLeft() { processPlayerMove(Direction.LEFT); }
    @FXML private void handleMoveRight() { processPlayerMove(Direction.RIGHT); }

    @FXML
    private void handleNewGame() {
        promptForDifficultyAndStartGame();
    }

    @FXML
    private void handleSaveGame() {
        if (engine != null && engine.getGameState() == GameState.IN_PROGRESS) {
            engine.saveGame();
            statusTextArea.appendText("Game saved.\n");
        } else {
            statusTextArea.appendText("No active game to save, or game is over.\n");
        }
    }

    @FXML
    private void handleLoadGame() {
        GameEngine loadedEngine = GameEngine.loadGame(); // This is a static method in GameEngine
        if (loadedEngine != null) {
            engine = loadedEngine; // Replace current engine instance
            this.player = engine.getPlayer(); // CRITICAL: Update the controller's player field
            if (this.player != null) {
                this.player.setGameEngineContext(engine);
            } else {
                statusTextArea.appendText("Error: Loaded game has no player object.\n");
                disableAllControls(true);
                return;
            }
            statusTextArea.appendText("Game loaded successfully.\n");
            updateGui();
        } else {
            statusTextArea.appendText("Failed to load game. No save file found or error during loading.\n");
        }
    }

    @FXML
    private void handleHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("MiniDungeon Help");
        alert.setHeaderText("How to Play MiniDungeon");
        String helpContent = "Goal: Achieve the highest score by collecting gold (G), defeating mutants (M, R), "
                + "and escaping through the ladder (L) on Level 2.\n\n"
                + "Controls: Use the arrow buttons to move.\n"
                + "Items & Symbols:\n"
                + "  P: Player\n"
                + "  E: Entry point\n"
                + "  L: Ladder (to next level / exit)\n"
                + "  #: Wall (impassable)\n"
                + "  G: Gold (+2 score, collected)\n"
                + "  H: Health Potion (+4 HP, max 10, consumed)\n"
                + "  T: Trap (-2 HP, remains)\n"
                + "  M: Melee Mutant (fight: -2 HP, +2 score, defeated)\n"
                + "  R: Ranged Mutant (attacks from 2 tiles, 50% chance, -2 HP per hit. Stepping on it: +2 score, no HP loss, defeated)\n\n"
                + "You have a maximum of 100 steps per level. Max HP is 10.\n"
                + "Good luck, adventurer!";
        alert.setContentText(helpContent);
        alert.getDialogPane().setMinWidth(500); // Make dialog wider
        alert.setResizable(true);
        alert.showAndWait();
    }
}