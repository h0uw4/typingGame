// ABSTRACTION: defines what every game must do, not how
public abstract class Game {

    // Template method: fixed order of steps, subclasses fill in the details
    public final void run() {
        showWelcome();
        start();
        showGoodbye();
    }

    protected abstract void showWelcome();
    protected abstract void start();
    protected abstract void showGoodbye();
}