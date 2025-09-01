package Tiles.Units.Players;

import Tiles.Units.Enemies.Enemy;
import Utils.Resource;

import java.util.List;

public class Warrior extends Player {
    private static final int EXTRA_HEALTH = 5;
    private static final int EXTRA_ATTACK = 2;
    private static final int ABILITY_HEAL = 10;
    private static final int ABILITY_RANGE = 3;

    private Resource coolDown;

    public Warrior(String name, int health, int attack, int defense, int cooldown) {
        super(name, health, attack, defense);
        this.coolDown = new Resource(cooldown,0);
    }


    /**
     * Returns a string describing the warrior's stats and cooldown.
     * @return the formatted description string
     */
    public String description() {
        return String.format(
                super.description() +
                        "Cooldown: %s\t", coolDown.toString());
    }

    /**
     * Handles logic when the warrior levels up, increasing stats and resetting cooldown.
     */
    @Override
    protected void onLevelUp() {
        coolDown.setAmount(0);
        health.addCapacity(EXTRA_HEALTH * level);
        attackPoints += EXTRA_ATTACK * level;
        defensePoints += level;
    }

    /**
     * Updates the cooldown timer each game tick.
     */
    @Override
    public void gameTick() {
        if (coolDown.getAmount() > 0) {
            coolDown.reduceAmount(1);
        }
    }

    /**
     * Casts the warrior's special ability, healing and damaging a random enemy in range.
     * @param enemies the list of enemies on the board
     */
    @Override
    public void castAbility(List<Enemy> enemies) {
        if (coolDown.getAmount() > 0) {
            mcb.call(String.format("%s tried to use Avenger's Shield, but there is a cooldown: %d", getName(), coolDown.getAmount()));
            return;
        }

        coolDown.setAmount(coolDown.getCapacity());
        int healing = ABILITY_HEAL * defensePoints;
        health.addAmount(healing);
        List<Enemy> inRangeEnemies = enemies.stream()
                .filter(e -> this.getPosition().calculateRange(e.getPosition()) < ABILITY_RANGE)
                .toList();

        if(!inRangeEnemies.isEmpty()) {
            Enemy target = inRangeEnemies.get(rand.nextInt(inRangeEnemies.size()));
            int damage = (int) (health.getCapacity() * 0.1);
            target.takeDamage(damage);
            if (target.isDead())
                target.onDeath(this);
            mcb.call(String.format("%s hit %s for %d ability damage.", getName(), target.getName(), damage));
        }

        mcb.call(String.format("%s used Avenger's Shield, healing for %d.", getName(), healing));
    }


    ///For tests:
    public Resource getCoolDown() { return this.coolDown; }
}
