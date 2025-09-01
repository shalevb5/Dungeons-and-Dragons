import Callbacks.*;
import Tiles.Units.Enemies.Enemy;
import Tiles.Units.Enemies.Monster;
import Tiles.Units.Players.Warrior;
import Utils.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Warrior Class Tests")
class WarriorTest {

    private Warrior warrior;
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
        // Reset state before each test
        messageLog = new ArrayList<>();
        deadEnemy = null;

        // Setup dummy callbacks
        mcb = messageLog::add;
        pcb = (t1, t2) -> {}; // Not critical for these tests
        pdcb = () -> {};      // Not critical for these tests
        edcb = enemy -> deadEnemy = enemy;

        // Initialize Warrior: Jon Snow, 300 HP, 30 Atk, 4 Def, 3 Cooldown
        warrior = new Warrior("Jon Snow", 300, 30, 4, 3);
        warrior.setCallbacks(mcb, pcb, pdcb);
        warrior.setPosition(new Position(5, 5));

        // Initialize Enemies for ability tests
        enemyInRange = new Monster("Test Monster", 'm', 50, 10, 2, 5, 20);
        enemyInRange.setCallbacks(mcb, pcb, edcb);
        enemyInRange.setPosition(new Position(5, 6)); // Range is 1.0 (< 3)

        enemyOutOfRange = new Monster("Far Monster", 'f', 50, 10, 2, 5, 20);
        enemyOutOfRange.setCallbacks(mcb, pcb, edcb);
        enemyOutOfRange.setPosition(new Position(10, 10)); // Range is > 3
    }

    @Nested
    @DisplayName("Constructor and Initial State")
    class ConstructorAndState {

        @Test
        @DisplayName("should correctly initialize all warrior attributes and cooldown")
        void testWarriorConstructor() {
            assertEquals("Jon Snow", warrior.getName());
            assertEquals(300, warrior.getHealth().getCapacity());
            assertEquals(300, warrior.getHealth().getAmount());
            assertEquals(30, warrior.getAttackPoints());
            assertEquals(4, warrior.getDefensePoints());
            assertEquals(1, warrior.getLevel());

            // Cooldown resource should be initialized with capacity but 0 amount
            assertEquals(3, warrior.getCoolDown().getCapacity(), "Cooldown capacity should be set.");
            assertEquals(0, warrior.getCoolDown().getAmount(), "Cooldown amount should start at 0.");
        }

        @Test
        @DisplayName("description method should include cooldown status")
        void testDescription() {
            String desc = warrior.description();
            assertTrue(desc.contains("Jon Snow"), "Description should contain name.");
            assertTrue(desc.contains("Health: 300/300"), "Description should contain getHealth().");
            assertTrue(desc.contains("Cooldown: 0/3"), "Description should contain cooldown status.");
        }
    }

    @Nested
    @DisplayName("Leveling Up")
    class LevelingUp {

        @Test
        @DisplayName("onLevelUp should increase stats and reset cooldown")
        void testOnLevelUp_IncreasesStatsAndResetsCooldown() {
            // Set cooldown to a non-zero value before leveling up
            warrior.getCoolDown().setAmount(2);
            assertEquals(2, warrior.getCoolDown().getAmount());

            // Leveling up requires 50 XP for level 1
            warrior.gainXP(50);

            // Assertions for level 2
            assertEquals(2, warrior.getLevel(), "Warrior should be level 2.");
            assertEquals(0, warrior.getExperience(), "Experience should be reset relative to the new level.");
            assertEquals(0, warrior.getCoolDown().getAmount(), "Cooldown should be reset to 0 on level up.");

            // Check stat increases for level 2:
            // Health: 300 + (5 * 2) + (10 * 2)= 330
            // Attack: 30 + (2 * 2) + (4 * 2) = 42
            // Defense: 4 + 2 + 2 = 8
            assertEquals(330, warrior.getHealth().getCapacity(), "Health capacity should increase correctly.");
            assertEquals(330, warrior.getHealth().getAmount(), "Health should be fully restored on level up.");
            assertEquals(42, warrior.getAttackPoints(), "Attack points should increase correctly.");
            assertEquals(8, warrior.getDefensePoints(), "Defense points should increase correctly.");

            assertTrue(messageLog.stream().anyMatch(s -> s.contains("reached level 2")), "Level up message should be logged.");
        }
    }

    @Nested
    @DisplayName("Game Tick Logic")
    class GameTick {

        @Test
        @DisplayName("gameTick should reduce cooldown when it is greater than 0")
        void testGameTick_ReducesCooldown() {
            warrior.getCoolDown().setAmount(3);
            warrior.gameTick();
            assertEquals(2, warrior.getCoolDown().getAmount());
            warrior.gameTick();
            assertEquals(1, warrior.getCoolDown().getAmount());
        }

        @Test
        @DisplayName("gameTick should not reduce cooldown when it is 0")
        void testGameTick_DoesNotReduceCooldownAtZero() {
            assertEquals(0, warrior.getCoolDown().getAmount());
            warrior.gameTick();
            assertEquals(0, warrior.getCoolDown().getAmount());
        }
    }

    @Nested
    @DisplayName("Ability: Avenger's Shield")
    class AbilityCasting {

        @Test
        @DisplayName("should fail to cast when ability is on cooldown")
        void castAbility_onCooldown_shouldFailAndLogMessage() {
            warrior.getCoolDown().setAmount(1);
            int initialHealth = warrior.getHealth().getAmount();

            warrior.castAbility(Collections.singletonList(enemyInRange));

            assertEquals(1, warrior.getCoolDown().getAmount(), "Cooldown should not change.");
            assertEquals(initialHealth, warrior.getHealth().getAmount(), "Health should not change.");
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("tried to use Avenger's Shield, but there is a cooldown")), "Cooldown message should be logged.");
        }

        @Test
        @DisplayName("should succeed when off cooldown, healing and damaging an enemy in range")
        void castAbility_withEnemyInRange_shouldHealAndDamage() {
            // Setup: Reduce warrior's health to see healing effect
            warrior.getHealth().reduceAmount(50); // Health is 250/300
            int enemyInitialHealth = enemyInRange.getHealth().getAmount(); // 50

            warrior.castAbility(List.of(enemyInRange, enemyOutOfRange));

            // Assert Cooldown is reset
            assertEquals(warrior.getCoolDown().getCapacity(), warrior.getCoolDown().getAmount(), "Cooldown should be reset to its max value.");

            // Assert Healing: Heals for 10 * defense (10 * 4 = 40). New health: 250 + 40 = 290
            assertEquals(290, warrior.getHealth().getAmount(), "Warrior should be healed.");

            // Assert Damage: Deals 10% of MAX health as damage (0.1 * 300 = 30). New enemy health: 50 - 30 = 20
            assertEquals(enemyInitialHealth - 30, enemyInRange.getHealth().getAmount(), "Enemy in range should take damage.");

            // Assert out-of-range enemy is unaffected
            assertEquals(50, enemyOutOfRange.getHealth().getAmount(), "Enemy out of range should not take damage.");

            // Assert messages
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("used Avenger's Shield, healing for 40")), "Healing message should be logged.");
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("hit Test Monster for 30 ability damage")), "Damage message should be logged.");
        }

        @Test
        @DisplayName("should only heal when no enemies are in range")
        void castAbility_withNoEnemiesInRange_shouldOnlyHeal() {
            warrior.getHealth().reduceAmount(50); // Health is 250/300

            warrior.castAbility(Collections.singletonList(enemyOutOfRange));

            assertEquals(warrior.getCoolDown().getCapacity(), warrior.getCoolDown().getAmount(), "Cooldown should be reset.");
            assertEquals(290, warrior.getHealth().getAmount(), "Warrior should be healed.");
            assertEquals(50, enemyOutOfRange.getHealth().getAmount(), "Enemy health should not change.");

            assertTrue(messageLog.stream().anyMatch(s -> s.contains("healing for 40")), "Healing message should be logged.");
            assertFalse(messageLog.stream().anyMatch(s -> s.contains("ability damage")), "No damage message should be logged.");
        }

        @Test
        @DisplayName("should not heal beyond maximum health capacity")
        void castAbility_atFullHealth_shouldNotHeal() {
            assertEquals(300, warrior.getHealth().getAmount());

            warrior.castAbility(Collections.emptyList());

            assertEquals(300, warrior.getHealth().getAmount(), "Health should not exceed maximum capacity.");
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("healing for 40")), "Healing message is still logged even if no health is gained.");
        }

        @Test
        @DisplayName("should kill an enemy and trigger its death callback")
        void castAbility_shouldKillEnemy() {
            // Enemy has 25 HP. Ability deals 30 damage.
            enemyInRange.getHealth().setAmount(25);

            warrior.castAbility(Collections.singletonList(enemyInRange));

            assertTrue(enemyInRange.isDead(), "Enemy should be dead after ability hit.");
            assertNotNull(deadEnemy, "EnemyDeathCallback should have been triggered.");
            assertEquals(enemyInRange, deadEnemy, "The correct enemy should be reported as dead.");
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("Test Monster died.")), "Enemy death message should be logged.");
        }
    }
}