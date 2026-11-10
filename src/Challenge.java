import java.util.Random;

// ABSTRACTION: a challenge is "something the player types"
public abstract class Challenge {
    protected static final Random RNG = new Random();
    private final String name;           // ENCAPSULATION

    protected Challenge(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Each challenge decides what text the player must type
    public abstract String generateText();

    // Each challenge decides how hard it is
    public abstract double getMultiplier();

    // Default scoring; subclasses may override it (POLYMORPHISM)
    public double computeScore(double wpm, double accuracy) {
        return wpm * (accuracy / 100.0) * getMultiplier();
    }

    protected String pick(String[] source) {
        return source[RNG.nextInt(source.length)];
    }
}