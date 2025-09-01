package Tiles.Units.Players;

import Tiles.Units.Enemies.Enemy;
import Utils.Resource;

import java.util.List;

public class Mage extends Player {
    private static final int EXTRA_MANA = 25;
    private static final int MANA_SETTER = 4;
    private static final int EXTRA_SPELL_POWER = 10;

    private Resource mana;
    private int manaCost;
    private int spellPower;
    private int hitsCount;
    private int abilityRange;

    public Mage(String name, int health, int attack, int defense, int mana, int cost, int spell, int hits, int range) {
        super(name, health, attack, defense);
        this.mana = new Resource(mana,mana / MANA_SETTER);
        this.manaCost = cost;
        this.spellPower = spell;
        this.hitsCount = hits;
        this.abilityRange = range;
    }

    /**
     * Returns a string describing the mage's stats, mana, and spell power.
     * @return the formatted description string
     */
    public String description() {
        return String.format(
                super.description() +
                        "Mana: %s\t" +
                        "Spell Power: %d",
                mana.toString(), spellPower);
    }

    /**
     * Handles logic when the mage levels up, increasing mana and spell power.
     */
    @Override
    protected void onLevelUp() {
        mana.addCapacity(EXTRA_MANA * level);
        mana.addAmount(mana.getCapacity() / MANA_SETTER);
        spellPower += EXTRA_SPELL_POWER * level;
    }

    /**
     * Updates the mage's mana each game tick.
     */
    @Override
    public void gameTick() {
        mana.addAmount(level);
    }

    @Override
    public void castAbility(List<Enemy> enemies) {
        if (mana.getAmount() < manaCost) {
            mcb.call(String.format("%s tried to cast Blizzard, but there was not enough mana: %s.", getName(), mana.toString()));
            return;
        }

        mana.reduceAmount(manaCost);
        mcb.call(String.format("%s cast Blizzard.", getName()));

        List<Enemy> inRangeEnemies = new java.util.ArrayList<>(enemies.stream()
                .filter(e -> this.getPosition().calculateRange(e.getPosition()) < abilityRange)
                .toList());

        int hits = 0;
        while (hits < hitsCount && !inRangeEnemies.isEmpty()) {
            Enemy target = inRangeEnemies.get(rand.nextInt(inRangeEnemies.size()));
            int defenseRoll = target.rollDefense();
            int damage = Math.max(0, spellPower - defenseRoll);
            mcb.call(String.format("%s hit Bonus Trap for %d ability damage.", getName(), damage));
            target.takeDamage(damage);
            if (target.isDead()) {
                target.onDeath(this);
                inRangeEnemies.remove(target);
            }
            hits++;
        }
    }

    ///For tests:
    public Resource getMana() {
        return mana;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getSpellPower() {
        return spellPower;
    }

    public int getHitsCount() {
        return hitsCount;
    }

    public int getAbilityRange() {
        return abilityRange;
    }

}
