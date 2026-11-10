// ABSTRACTION: anything in the game that has health and can be hit
// ENCAPSULATION: hp can only change through takeDamage()
public abstract class Entity {
    private final String name;
    private final int maxHp;
    private int hp;

    protected Entity(String name, int maxHp) {
        this.name = name;
        this.maxHp = Math.max(1, maxHp);
        this.hp = this.maxHp;
    }

    public String getName() { return name; }
    public int getHp()      { return hp; }
    public int getMaxHp()   { return maxHp; }

    public void takeDamage(int amount) {
        if (amount <= 0) return;
        hp = Math.max(0, hp - amount);   // hp never goes below 0
    }

    public boolean isDefeated() {
        return hp == 0;
    }

    public double getHpPercent() {
        return hp * 100.0 / maxHp;
    }

    // Every entity has a picture; each subclass says which file
    public abstract String getImageFile();
}