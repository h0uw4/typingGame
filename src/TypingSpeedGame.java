import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// INHERITANCE: TypingSpeedGame IS-A Game
public class TypingSpeedGame extends Game {
    private final Scanner in = new Scanner(System.in);
    private final List<Challenge> challenges = new ArrayList<>();
    private Player player;

    public TypingSpeedGame() {
        // POLYMORPHISM: different subclasses stored as the same type
        challenges.add(new WordChallenge());
        challenges.add(new SentenceChallenge());
        challenges.add(new CodeChallenge());
    }

    @Override
    protected void showWelcome() {
        System.out.println("=====================================");
        System.out.println("        TYPING SPEED GAME");
        System.out.println("=====================================");
        System.out.print("Enter your name: ");
        player = new Player(in.nextLine());
        System.out.println("Welcome, " + player.getName() + "!");
    }

    @Override
    protected void start() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = in.nextLine().trim();
            switch (choice) {
                case "0":
                    running = false;
                    break;
                case "4":
                    showStats();
                    break;
                default:
                    try {
                        int idx = Integer.parseInt(choice) - 1;
                        if (idx >= 0 && idx < challenges.size()) {
                            playRound(challenges.get(idx));
                        } else {
                            System.out.println("Invalid choice.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid choice.");
                    }
            }
        }
    }

    @Override
    protected void showGoodbye() {
        System.out.println();
        System.out.println("Thanks for playing, " + player.getName() + "!");
        System.out.printf("Best WPM: %.1f | Best score: %.1f%n",
                player.getBestWpm(), player.getBestScore());
    }

    private void printMenu() {
        System.out.println();
        System.out.println("---------- MENU ----------");
        for (int i = 0; i < challenges.size(); i++) {
            Challenge c = challenges.get(i);
            System.out.printf("%d. %s (x%.1f)%n", i + 1, c.getName(), c.getMultiplier());
        }
        System.out.println("4. View stats");
        System.out.println("0. Quit");
        System.out.print("Choose: ");
    }

    private void playRound(Challenge challenge) {
        String target = challenge.generateText();

        System.out.println();
        System.out.println("Type this text exactly, then press Enter:");
        System.out.println();
        System.out.println("  " + target);
        System.out.println();
        System.out.print("Press Enter when ready...");
        in.nextLine();

        for (int i = 3; i >= 1; i--) {
            System.out.println(i + "...");
            sleep(700);
        }
        System.out.println("GO!");

        long start = System.nanoTime();
        String typed = in.nextLine();
        long end = System.nanoTime();

        double seconds = Math.max((end - start) / 1_000_000_000.0, 0.001);
        double accuracy = calculateAccuracy(target, typed);
        double wpm = (typed.length() / 5.0) / (seconds / 60.0);
        double score = challenge.computeScore(wpm, accuracy); // polymorphic call

        Result result = new Result(wpm, accuracy, seconds, score);
        player.recordResult(result);
        showResult(result);
    }

    private double calculateAccuracy(String target, String typed) {
        int total = Math.max(target.length(), typed.length());
        if (total == 0) return 0;
        int correct = 0;
        int limit = Math.min(target.length(), typed.length());
        for (int i = 0; i < limit; i++) {
            if (target.charAt(i) == typed.charAt(i)) correct++;
        }
        return correct * 100.0 / total;
    }

    private void showResult(Result r) {
        System.out.println();
        System.out.println("---------- RESULT ----------");
        System.out.printf("Time     : %.2f seconds%n", r.getSeconds());
        System.out.printf("Speed    : %.1f WPM%n", r.getWpm());
        System.out.printf("Accuracy : %.1f%%%n", r.getAccuracy());
        System.out.printf("Score    : %.1f%n", r.getScore());
    }

    private void showStats() {
        System.out.println();
        System.out.println("---------- STATS ----------");
        System.out.println("Player       : " + player.getName());
        System.out.println("Games played : " + player.getGamesPlayed());
        System.out.printf("Best WPM     : %.1f%n", player.getBestWpm());
        System.out.printf("Best score   : %.1f%n", player.getBestScore());
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}