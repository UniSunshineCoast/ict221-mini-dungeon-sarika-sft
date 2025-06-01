// This is the code for ScoreEntry.java
package dungeon.engine; // Make sure this is the first line

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ScoreEntry implements Serializable, Comparable<ScoreEntry> { // Ensure 'public class ScoreEntry' matches file name
    private static final long serialVersionUID = 5L;
    private final int score;
    private final LocalDate date;

    public ScoreEntry(int score, LocalDate date) { this.score = score; this.date = date; }
    public int getScore() { return score; }
    public LocalDate getDate() { return date; }
    @Override public int compareTo(ScoreEntry other) {
        if (this.score != other.score) return Integer.compare(other.score, this.score); // Higher score first
        return other.date.compareTo(this.date); // Then more recent date first
    }
    @Override public String toString() { return String.format("%d %s", score, date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); }
    @Override public boolean equals(Object o) {
        if (this == o) return true; if (o == null || getClass() != o.getClass()) return false;
        ScoreEntry that = (ScoreEntry) o; return score == that.score && Objects.equals(date, that.date);
    }
    @Override public int hashCode() { return Objects.hash(score, date); }
}
