package Tiles.Units.Enemies;

public class Trap extends Enemy {
    private int visibilityTime;
    private int invisibilityTime;
    private int ticksCount;
    private boolean visible;


    public Trap(String name, char tile, int health, int attack, int defense, int exp, int visTime, int invisTime) {
        super(name, tile, health, attack, defense, exp);
        this.visibilityTime = visTime;
        this.invisibilityTime = invisTime;
        this.ticksCount = 0;
        this.visible = true;
    }

    /**
     * Returns the character representation of the trap.
     * If the trap is invisible, returns '.' to hide it from the player.
     *
     * @return The trap's visible tile character, or '.' when invisible.
     */
    @Override
    public char getCharacter() { return visible ? character : '.'; }

    /**
     * Updates the trap’s state every game tick:
     * - Alternates visibility based on the tick counter.
     * - Resets the tick counter after a full visibility + invisibility cycle.
     * - If the player is within attack range, updates the trap's desired position.
     */
    @Override
    public void gameTick() {
        this.desiredPosition = position;

        if (ticksCount == (visibilityTime + invisibilityTime) - 1) {
            ticksCount = 0;
        } else {
            ticksCount++;
        }

        visible = ticksCount < visibilityTime;

        if(canAttackPlayer()) {
            desiredPosition = playerPosition;
        }
    }

    /**
     * Determines whether the trap is close enough to attack the player.
     *
     * @return {@code true} if the player is within 1 unit of distance; {@code false} otherwise.
     */
    private boolean canAttackPlayer() {
        return position.calculateRange(playerPosition) < 2;
    }


    ///For tests:
    public int getVisibilityTime() {
        return visibilityTime;
    }

    public int getInvisibilityTime() {
        return invisibilityTime;
    }

    public int getTicksCount() {
        return ticksCount;
    }

    public boolean isVisible() {
        return visible;
    }
}
