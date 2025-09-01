package Tiles.Units.Players;

import Tiles.Units.Enemies.Enemy;
import Utils.Resource;

import java.util.List;

public class Rogue extends Player {
    private static final int MAX_ENERGY = 100;
    private static final int EXTRA_ATTACK = 3;
    private static final int ABILITY_RANGE = 2;

    private int cost;
    private Resource energy;

    public Rogue(String name, int health, int attack, int defense, int cost) {
        super(name, health, attack, defense);
        this.cost = cost;
        this.energy = new Resource(MAX_ENERGY, MAX_ENERGY);
    }

    /**
     * Returns a string describing the rogue's stats and energy.
     * @return the formatted description string
     */
    public String description() {
        return String.format(
                super.description()  +
                        "Energy: %s",
                energy.toString());
    }

    /**
     * Handles logic when the rogue levels up, restoring energy and increasing attack.
     */
    @Override
    protected void onLevelUp() {
        energy.restore();
        attackPoints += EXTRA_ATTACK * level;
    }

    /**
     * Updates the rogue's energy each game tick.
     */
    @Override
    public void gameTick() {
        energy.addAmount(10);
    }

    /**
     * Casts the rogue's special ability, damaging all enemies in range if enough energy is available.
     * @param enemies the list of enemies on the board
     */
    @Override
    public void castAbility(List<Enemy> enemies) {
        if (energy.getAmount() < cost) {
            mcb.call(String.format("%s tried to cast Fan of Knives, but there was not enough energy: %s.", getName(), energy.toString()));
            return;
        }

        energy.reduceAmount(cost);
        mcb.call(String.format("%s cast Fan of Knives.", getName()));

        List<Enemy> inRangeEnemies = enemies.stream()
                .filter(e -> this.getPosition().calculateRange(e.getPosition()) < ABILITY_RANGE)
                .toList();

        for (Enemy target : inRangeEnemies) {
            int defenseRoll = target.rollDefense();
            int damage = Math.max(0, attackPoints - defenseRoll);
            mcb.call(String.format("%s hit %s for %d ability damage.", getName(), target.getName(), damage));
            target.takeDamage(damage);

            if (target.isDead())
                target.onDeath(this);
        }
    }

    ///For tests:
    public Resource getEnergy() { return this.energy; }
    ///For tests:
    public int getCost() { return this.cost; }
}
