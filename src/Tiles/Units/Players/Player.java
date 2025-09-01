package Tiles.Units.Players;

import Callbacks.ChangePositionCallback;
import Callbacks.MessageCallback;
import Callbacks.PlayerDeathCallback;
import Tiles.Units.Enemies.Enemy;
import Tiles.Units.Unit;
import Utils.Resource;

import java.util.List;

public abstract class Player extends Unit {
    private PlayerDeathCallback pdcb;
    private static final int EXPERIENCE_PER_LEVEL = 50;
    private static final int HEALTH_POOL_PER_LEVEL = 10;
    private static final int ATTACK_POINTS_PER_LEVEL = 4;
    private static final char PLAYER_CHAR = '@';
    private static final char DEAD_PLAYER = 'X';

    protected int experience;
    protected int level;

    public Player(String name, int health, int attack, int defense) {
        super();
        this.name = name;
        this.health = new Resource(health,health);
        this.attackPoints = attack;
        this.defensePoints = defense;
        this.experience = 0;
        this.level = 1;
        this.character = PLAYER_CHAR;
    }

    /**
     * Returns a string describing the player's stats, level, and experience.
     * @return the formatted description string
     */
    public String description() {
        return String.format(
                super.description()  +
                        "Level: %d\t" +
                        "Experience: %d/%d\t",
                level, experience,(EXPERIENCE_PER_LEVEL * level));
    }

    /**
     * Adds experience to the player and handles leveling up if needed.
     * @param exp the amount of experience to gain
     */
    public void gainXP(int exp) {
        experience += exp;
        mcb.call(name + " gained " + exp + " experience");
        while (experience >= EXPERIENCE_PER_LEVEL * level) {
            levelUp();
        }
    }


    /**
     * Sets the callbacks for messages, position changes, and player death.
     * @param mcb the message callback
     * @param pcb the position change callback
     * @param pdcb the player death callback
     */
    public void setCallbacks(MessageCallback mcb, ChangePositionCallback pcb, PlayerDeathCallback pdcb) {
        super.setCallbacks(mcb,pcb);
        this.pdcb = pdcb;
    }

    /**
     * Handles logic when the player dies.
     * @param u the unit responsible for the player's death
     */
    public void onDeath(Unit u) {
        String output = String.format("%s was killed by %s.",this.getName(), u.getName());
        this.character = DEAD_PLAYER;

        mcb.call(output);
        pdcb.call();
    }

    /**
     * Accepts a visiting unit for interaction.
     * @param unit the visiting unit
     */
    @Override
    public void accept(Unit unit) {
        unit.visit(this);
    }

    /**
     * Handles being visited by another player (does nothing).
     * @param player the player visiting
     */
    @Override
    public void visit(Player player) { }

    /**
     * Handles being visited by an enemy (initiates combat).
     * @param enemy the enemy visiting
     */
    @Override
    public void visit(Enemy enemy) {
        combat(enemy);
    }

    /**
     * Handles the logic for leveling up the player, increasing stats and restoring health.
     */
    protected void levelUp() {
        int prevHealth = health.getCapacity();
        int prevAttack = attackPoints;
        int prevDefense = defensePoints;

        experience-= (EXPERIENCE_PER_LEVEL * level);
        level++;
        health.addCapacity(HEALTH_POOL_PER_LEVEL * level);
        attackPoints += (ATTACK_POINTS_PER_LEVEL * level);
        defensePoints += level;
        onLevelUp();
        health.restore();

        String levelUpMessage = String.format("%s reached level %d: +%d Health, +%d Attack, +%d Defense"
                ,name,level,(this.health.getAmount() - prevHealth),(attackPoints - prevAttack),(defensePoints - prevDefense));

        mcb.call(levelUpMessage);
    }

    protected abstract void onLevelUp();
    public abstract void castAbility(List<Enemy> enemies);

    ///For tests:
    public int getExperience() {
        return experience;
    }

    public int getLevel() {
        return level;
    }
}
