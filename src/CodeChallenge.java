public class CodeChallenge extends Challenge {
    private static final String[] SNIPPETS = {
        "for (int i = 0; i < 10; i++) { sum += i; }",
        "System.out.println(\"Hello, World!\");",
        "if (x > 0 && y < 5) { return x + y; }",
        "List<String> names = new ArrayList<>();",
        "public static void main(String[] args) { }"
    };

    public CodeChallenge() {
        super("Java Code");
    }

    @Override
    public String generateText() {
        return pick(SNIPPETS);
    }

    @Override
    public double getMultiplier() {
        return 2.0;
    }

    @Override
    public double computeScore(double wpm, double accuracy) {
        double factor = accuracy / 100.0;
        return wpm * factor * factor * getMultiplier();
    }
}