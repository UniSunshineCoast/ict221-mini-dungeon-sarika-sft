package dungeon.engine;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 2L;
    private Position position;
    private int hp;
    private final int maxHp = 10;
    private int score;
    private int stepsTaken;
    private transient Object gameEngineContext; // To hold reference to GameEngine, e.g. for GameMap.displayMapText

    public Player(Position startPosition) {
        this.position = startPosition;
        this.hp = maxHp;
        this.score = 0;
        this.stepsTaken = 0;
    }
    public Position getPosition() { return position; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getScore() { return score; }
    public int getStepsTaken() { return stepsTaken; }
    public void moveTo(Position newPosition) { this.position = newPosition; }
    public void incrementSteps() { this.stepsTaken++; }
    public void takeDamage(int amount) {
        this.hp -= amount;
        if (this.hp < 0) this.hp = 0;
    }
    public void heal(int amount) {
        this.hp += amount;
        if (this.hp > maxHp) this.hp = maxHp;
    }
    public void addScore(int amount) { this.score += amount; }
    public void setScore(int score) { this.score = score; } // For game over state
    public void resetForNewLevel(Position newStartPosition) {
        this.position = newStartPosition;
        this.stepsTaken = 0;
        // HP and score typically carry over to the next level.
    }
    public void setGameEngineContext(Object context) { this.gameEngineContext = context; }
    public Object getGameEngineContext() { return this.gameEngineContext; }
    @Override public String toString() {
        return "Player [Pos=" + position.x() + "," + position.y() + " HP=" + hp + "/" + maxHp + ", Score=" + score + ", Steps=" + stepsTaken + "]";
    }
}