package dungeon.engine;

import java.io.Serializable;
import java.util.Objects;

public record Position(int x, int y) implements Serializable {
    private static final long serialVersionUID = 1L; // For Serializable records
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }
    @Override
    public int hashCode() { return Objects.hash(x, y); }
}
