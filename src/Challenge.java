import java.util.Random;

public abstract class Challenge {
    protected static final Random RNG = new Random();
    private final String name;           // ENCAPSULATION

    protected Challenge(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String generateText();
    public abstract double getMultiplier();

    public double computeScore(double wpm, double accuracy) {
        return wpm * (accuracy / 100.0) * getMultiplier();
    }

    protected String pick(String[] source) {
        return source[RNG.nextInt(source.length)];
    }
}