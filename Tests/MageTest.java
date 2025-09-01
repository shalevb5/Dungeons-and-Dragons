import Callbacks.*;
import Tiles.Units.Enemies.Enemy;
import Tiles.Units.Enemies.Monster;
import Tiles.Units.Players.Mage;
import Utils.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mage Class Tests")
class MageTest {

    private Mage mage;
    private Monster enemyInRange;
    private Monster enemyOutOfRange;
    private List<String> messageLog;
    private Enemy deadEnemy;

    // Dummy callbacks to capture events
    private MessageCallback mcb;
    private ChangePositionCallback pcb;
    private PlayerDeathCallback pdcb;
    private EnemyDeathCallback edcb;

    @BeforeEach
    void setUp() {
        // Reset state
        messageLog = new ArrayList<>();
        deadEnemy = null;

        // Setup callbacks
        mcb = messageLog::add;
        pcb = (t1, t2) -> {};
        pdcb = () -> {};
        edcb = enemy -> deadEnemy = enemy;

        // Initialize Mage: Melisandre, 100 HP, 5 Atk, 1 Def, 300 Mana, 30 Cost, 15 Spell Power, 5 Hits, 6 Range
        mage = new Mage("Melisandre", 100, 5, 1, 300, 30, 15, 5, 6);
        mage.setCallbacks(mcb, pcb, pdcb);
        mage.setPosition(new Position(5, 5));

        // Initialize Enemies for ability tests
        enemyInRange = new Monster("Ice Zombie", 'z', 100, 10, 0, 5, 50);
        enemyInRange.setCallbacks(mcb, pcb, edcb);
        enemyInRange.setPosition(new Position(6, 6)); // Range is ~1.41 (< 6)
        enemyInRange.setDefensePoints(0); // Set defense to 0 for predictable damage

        enemyOutOfRange = new Monster("Distant Dragon", 'd', 100, 10, 2, 5, 50);
        enemyOutOfRange.setCallbacks(mcb, pcb, edcb);
        enemyOutOfRange.setPosition(new Position(20, 20)); // Range > 6
    }

    @Nested
    @DisplayName("Constructor and Initial State")
    class ConstructorAndState {

        @Test
        @DisplayName("should correctly initialize all mage attributes and mana")
        void testMageConstructor() {
            assertEquals("Melisandre", mage.getName());
            assertEquals(100, mage.getHealth().getCapacity());
            assertEquals(300, mage.getMana().getCapacity(), "Mana capacity should be set.");
            assertEquals(75, mage.getMana().getAmount(), "Initial mana should be capacity / 4.");
            assertEquals(30, mage.getManaCost());
            assertEquals(15, mage.getSpellPower());
            assertEquals(5, mage.getHitsCount());
            assertEquals(6, mage.getAbilityRange());
            assertEquals(1, mage.getLevel());
        }

        @Test
        @DisplayName("description method should include mana and spell power")
        void testDescription() {
            String desc = mage.description();
            assertTrue(desc.contains("Melisandre"), "Description should contain name.");
            assertTrue(desc.contains("Health: 100/100"), "Description should contain health.");
            assertTrue(desc.contains("Mana: 75/300"), "Description should contain mana status.");
            assertTrue(desc.contains("Spell Power: 15"), "Description should contain spell power.");
        }
    }

    @Nested
    @DisplayName("Leveling Up")
    class LevelingUp {

        @Test
        @DisplayName("onLevelUp should increase stats, mana, and spell power")
        void testOnLevelUp_IncreasesStatsAndPower() {
            // Leveling up requires 50 XP for level 1
            mage.gainXP(50);

            // Assertions for level 2
            assertEquals(2, mage.getLevel(), "Mage should be level 2.");

            // Check Mana increase for level 2:
            // Old Capacity: 300. New Capacity: 300 + (25 * 2) = 350
            // Mana gain: 350 / 4 = 87. Previous mana was 75. New mana: 75 + 87 = 162.

            assertEquals(350, mage.getMana().getCapacity(), "Mana capacity should increase correctly.");
            assertEquals(162, mage.getMana().getAmount(), "Mana amount should be updated correctly on level up.");

            // Check Spell Power increase for level 2:
            // Old Spell Power: 15. New Spell Power: 15 + (10 * 2) = 35
            assertEquals(35, mage.getSpellPower(), "Spell power should increase correctly.");

            assertTrue(messageLog.stream().anyMatch(s -> s.contains("reached level 2")), "Level up message should be logged.");
        }
    }

    @Nested
    @DisplayName("Game Tick Logic")
    class GameTick {

        @Test
        @DisplayName("gameTick should restore mana equal to the current level")
        void testGameTick_RestoresMana() {
            mage.getMana().setAmount(50);
            mage.gameTick(); // Mage is level 1
            assertEquals(51, mage.getMana().getAmount(), "Mana should be restored by an amount equal to the level.");
        }

        @Test
        @DisplayName("gameTick should not restore mana beyond its capacity")
        void testGameTick_ManaDoesNotExceedCapacity() {
            mage.getMana().setAmount(mage.getMana().getCapacity() - 1);
            mage.gameTick(); // Tries to add 1 mana
            assertEquals(mage.getMana().getCapacity(), mage.getMana().getAmount(), "Mana should not exceed its capacity.");
        }
    }

    @Nested
    @DisplayName("Ability: Blizzard")
    class AbilityCasting {

        @Test
        @DisplayName("should fail to cast when mana is insufficient")
        void castAbility_notEnoughMana_shouldFail() {
            mage.getMana().setAmount(29); // Mana cost is 30
            int initialMana = mage.getMana().getAmount();

            mage.castAbility(List.of(enemyInRange));

            assertEquals(initialMana, mage.getMana().getAmount(), "Mana should not be consumed.");
            assertEquals(100, enemyInRange.getHealth().getAmount(), "Enemy should not take damage.");
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("not enough mana")), "Insufficient mana message should be logged.");
        }

        @Test
        @DisplayName("should succeed and hit a single target multiple times")
        void castAbility_withOneEnemy_shouldHitMultipleTimes() {
            // Spell Power: 15, Enemy Defense: 0 -> Damage per hit is 15. Hits: 5.
            // Expected total damage: 15 * 5 = 75.
            mage.castAbility(List.of(enemyInRange));

            long hitCount = messageLog.stream().filter(s -> s.contains("ability damage")).count();

            assertEquals(5, hitCount, "Should log a damage message for each hit.");
            assertEquals(100 - 75, enemyInRange.getHealth().getAmount(), "Enemy should take the total calculated damage.");
            assertEquals(75 - 30, mage.getMana().getAmount(), "Mana should be consumed.");
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("cast Blizzard")), "Cast message should be logged.");
        }

        @Test
        @DisplayName("should consume mana even if no targets are in range")
        void castAbility_withNoEnemiesInRange_shouldConsumeMana() {
            mage.castAbility(List.of(enemyOutOfRange));

            assertEquals(75 - 30, mage.getMana().getAmount(), "Mana should be consumed even with no targets.");
            assertEquals(100, enemyOutOfRange.getHealth().getAmount(), "Out-of-range enemy should not be damaged.");
            long hitCount = messageLog.stream().filter(s -> s.contains("ability damage")).count();
            assertEquals(0, hitCount, "No damage messages should be logged.");
        }

        @Test
        @DisplayName("should stop hitting once all in-range enemies are dead")
        void castAbility_shouldStopWhenTargetsDie() {
            // Enemy has 20 HP. Mage deals 15 damage per hit. It should die on the 2nd hit.
            enemyInRange.getHealth().setAmount(20);

            mage.castAbility(List.of(enemyInRange));

            assertTrue(enemyInRange.isDead(), "Enemy should be dead.");
            assertNotNull(deadEnemy, "Enemy death callback should have been triggered.");

            // It takes 2 hits to kill. The loop should stop after that.
            long hitCount = messageLog.stream().filter(s -> s.contains("ability damage")).count();
            assertEquals(2, hitCount, "Should only log 2 hits, not all 5.");
        }

        @Test
        @DisplayName("should distribute hits among multiple targets and handle death mid-cast")
        void castAbility_withMultipleTargets_handlesMidCastDeath() {
            // Setup two enemies in range
            Monster weakEnemy = new Monster("Weak Zombie", 'z', 10, 10, 0, 5, 20);
            weakEnemy.setCallbacks(mcb, pcb, edcb);
            weakEnemy.setPosition(new Position(5, 4));
            weakEnemy.setDefensePoints(0);

            List<Enemy> enemies = new ArrayList<>(List.of(enemyInRange, weakEnemy));

            // Mage will hit 5 times for 15 damage each.
            // The first hit will kill the weak enemy (10 HP).
            // The remaining 4 hits should all go to the other enemy.
            mage.castAbility(enemies);

            assertTrue(weakEnemy.isDead(), "The weak enemy should be killed.");
            // 4 hits on the strong enemy: 4 * 15 = 60 damage.
            assertEquals(100 - 60, enemyInRange.getHealth().getAmount(), "The strong enemy should take damage from the remaining hits.");

            long totalHits = messageLog.stream().filter(s -> s.contains("ability damage")).count();
            assertEquals(5, totalHits, "All 5 hits should have been dealt.");
        }
    }
}