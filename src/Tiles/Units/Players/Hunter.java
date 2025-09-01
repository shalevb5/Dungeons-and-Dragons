package Tiles.Units.Players;

import Tiles.Units.Enemies.Enemy;
import Utils.Resource;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Hunter extends Player {

    private static final int ARROWS_PER_LEVEL_UP = 10;
    private static final int ATTACK_BONUS_PER_LEVEL = 2;
    private static final int DEFENSE_BONUS_PER_LEVEL = 1;
    private static final int TICKS_FOR_ARROW_REGEN = 10;

    protected Resource arrows;
    protected int range;
    protected int ticksCount;

    public Hunter(String name, int health, int attack, int defense, int range) {
        super(name, health, attack, defense);
        this.range = range;
        this.arrows = new Resource(ARROWS_PER_LEVEL_UP, ARROWS_PER_LEVEL_UP);
        this.ticksCount = 0;
    }

    /**
     * Handles logic when the Hunter levels up, increasing arrows, attack, and defense.
     */
    @Override
    protected void onLevelUp() {
        arrows.addAmount(ARROWS_PER_LEVEL_UP * level);
        attackPoints += ATTACK_BONUS_PER_LEVEL * level;
        defensePoints += DEFENSE_BONUS_PER_LEVEL * level;
    }

    /**
     * Advances the game tick for the Hunter, regenerating arrows after a set number of ticks.
     */
    @Override
    public void gameTick() {
        if (ticksCount == TICKS_FOR_ARROW_REGEN) {
            arrows.addAmount(level);
            ticksCount = 0;
        } else {
            ticksCount++;
        }
    }

    /**
     * Casts the Hunter's ability, firing an arrow at the closest enemy within range.
     * If no arrows are available or no enemies are in range, notifies the player.
     * @param enemies the list of enemies on the board
     */
    @Override
    public void castAbility(List<Enemy> enemies) {
        if (arrows.getAmount() <= 0) {
            mcb.call(String.format("%s tried to shoot but has no arrows left.", getName()));
            return;
        }

        // Find the closest enemy within range
        Optional<Enemy> closestEnemy = enemies.stream()
                .filter(e -> this.getPosition().calculateRange(e.getPosition()) < this.range)
                .min(Comparator.comparingDouble(e -> this.getPosition().calculateRange(e.getPosition())));

        if (closestEnemy.isEmpty()) {
            mcb.call(String.format("%s tried to shoot an arrow but there were no enemies in range.", getName()));
            return;
        }

        Enemy target = closestEnemy.get();
        arrows.reduceAmount(1);
        mcb.call(String.format("%s fired an arrow at %s.", getName(), target.getName()));

        // Deal damage equal to attack points
        int defenseRoll = target.rollDefense();
        int damage = Math.max(0, this.attackPoints - defenseRoll);

        mcb.call(String.format("%s rolled %d defense points.", target.getName(), defenseRoll));
        mcb.call(String.format("%s hit %s for %d ability damage.", getName(), target.getName(), damage));

        target.takeDamage(damage);
        if (target.isDead()) {
            target.onDeath(this);
        }
    }

    /**
     * Returns a string description of the Hunter, including arrows and range.
     * @return the Hunter's description
     */
    @Override
    public String description() {
        return String.format(
                super.description() +
                        "Arrows: %s\t" +
                        "Range: %d",
                arrows.toString(), range);
    }

}