// INHERITANCE: SentenceChallenge IS-A Challenge
public class SentenceChallenge extends Challenge {
    private static final String[] SENTENCES = {
        "The quick brown fox jumps over the lazy dog.",
        "Practice makes perfect when you type every day.",
        "A journey of a thousand miles begins with a single step.",
        "Programming is the art of telling a computer what to do.",
        "Good code is its own best documentation."
    };

    public SentenceChallenge() { super("Sentences"); }

    @Override public String generateText() { return pick(SENTENCES); }
    @Override public double getMultiplier() { return 1.5; }

    @Override
    public Boss createBoss() {
        return new Boss("Sentence Dragon", 250, "sentence-dragon.png", "Medium", 50);
    }
}