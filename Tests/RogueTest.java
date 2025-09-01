import Callbacks.*;
import Tiles.Units.Enemies.Enemy;
import Tiles.Units.Enemies.Monster;
import Tiles.Units.Players.Rogue;
import Utils.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Rogue Class Tests")
class RogueTest {

    private Rogue rogue;
    private Monster enemyInRange;
    private Monster anotherEnemyInRange;
    private Monster enemyOutOfRange;
    private List<String> messageLog;
    private List<Enemy> deadEnemies;

    // Dummy callbacks
    private MessageCallback mcb;
    private ChangePositionCallback pcb;
    private PlayerDeathCallback pdcb;
    private EnemyDeathCallback edcb;

    @BeforeEach
    void setUp() {
        // Reset state
        messageLog = new ArrayList<>();
        deadEnemies = new ArrayList<>();

        // Setup callbacks
        mcb = messageLog::add;
        pcb = (t1, t2) -> {};
        pdcb = () -> {};
        edcb = deadEnemies::add; // Capture all dead enemies

        // Initialize Rogue: Arya Stark, 150 HP, 40 Atk, 2 Def, 20 Cost
        rogue = new Rogue("Arya Stark", 150, 40, 2, 20);
        rogue.setCallbacks(mcb, pcb, pdcb);
        rogue.setPosition(new Position(5, 5));

        // Initialize Enemies for ability tests
        // Ability Range is 2
        enemyInRange = new Monster("Guard", 'g', 80, 10, 5, 5, 30);
        enemyInRange.setCallbacks(mcb, pcb, edcb);
        enemyInRange.setPosition(new Position(5, 6)); // Range 1.0 < 2
        enemyInRange.setDefensePoints(0); // For predictable damage

        anotherEnemyInRange = new Monster("Another Guard", 'g', 80, 10, 5, 5, 30);
        anotherEnemyInRange.setCallbacks(mcb, pcb, edcb);
        anotherEnemyInRange.setPosition(new Position(4, 5)); // Range 1.0 < 2
        anotherEnemyInRange.setDefensePoints(0);

        enemyOutOfRange = new Monster("Archer", 'a', 50, 10, 2, 5, 20);
        enemyOutOfRange.setCallbacks(mcb, pcb, edcb);
        enemyOutOfRange.setPosition(new Position(8, 8)); // Range > 2
    }

    @Nested
    @DisplayName("Constructor and Initial State")
    class ConstructorAndState {

        @Test
        @DisplayName("should correctly initialize all rogue attributes and energy")
        void testRogueConstructor() {
            assertEquals("Arya Stark", rogue.getName());
            assertEquals(150, rogue.getHealth().getCapacity());
            assertEquals(20, rogue.getCost());

            // Energy should start full
            assertEquals(100, rogue.getEnergy().getCapacity(), "Energy capacity should be 100.");
            assertEquals(100, rogue.getEnergy().getAmount(), "Initial energy should be full.");
        }

        @Test
        @DisplayName("description method should include energy status")
        void testDescription() {
            String desc = rogue.description();
            assertTrue(desc.contains("Arya Stark"), "Description should contain name.");
            assertTrue(desc.contains("Health: 150/150"), "Description should contain health.");
            assertTrue(desc.contains("Energy: 100/100"), "Description should contain energy status.");
        }
    }

    @Nested
    @DisplayName("Leveling Up")
    class LevelingUp {

        @Test
        @DisplayName("onLevelUp should increase stats and restore energy")
        void testOnLevelUp_IncreasesStatsAndRestoresEnergy() {
            // Deplete some energy before level up
            rogue.getEnergy().setAmount(50);

            // Level up
            rogue.gainXP(50);

            assertEquals(2, rogue.getLevel(), "Rogue should be level 2.");

            // Check Attack increase for level 2:
            // Old Attack: 40. New Attack: 40 + (4 * 2) + (3 * 2)  = 54
            assertEquals(54, rogue.getAttackPoints(), "Attack points should increase correctly.");

            // Check Energy is restored
            assertEquals(100, rogue.getEnergy().getAmount(), "Energy should be fully restored on level up.");

            assertTrue(messageLog.stream().anyMatch(s -> s.contains("reached level 2")), "Level up message should be logged.");
        }
    }

    @Nested
    @DisplayName("Game Tick Logic")
    class GameTick {

        @Test
        @DisplayName("gameTick should restore 10 energy")
        void testGameTick_RestoresEnergy() {
            rogue.getEnergy().setAmount(50);
            rogue.gameTick();
            assertEquals(60, rogue.getEnergy().getAmount(), "Should restore 10 energy.");
        }

        @Test
        @DisplayName("gameTick should not restore energy beyond its capacity")
        void testGameTick_EnergyDoesNotExceedCapacity() {
            rogue.getEnergy().setAmount(95);
            rogue.gameTick();
            assertEquals(100, rogue.getEnergy().getAmount(), "Energy should not exceed its capacity.");
        }
    }

    @Nested
    @DisplayName("Ability: Fan of Knives")
    class AbilityCasting {

        @Test
        @DisplayName("should fail to cast when energy is insufficient")
        void castAbility_notEnoughEnergy_shouldFail() {
            rogue.getEnergy().setAmount(19); // Cost is 20
            int initialEnergy = rogue.getEnergy().getAmount();

            rogue.castAbility(List.of(enemyInRange));

            assertEquals(initialEnergy, rogue.getEnergy().getAmount(), "Energy should not be consumed.");
            assertEquals(80, enemyInRange.getHealth().getAmount(), "Enemy should not take damage.");
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("not enough energy")), "Insufficient energy message should be logged.");
        }

        @Test
        @DisplayName("should succeed and damage all enemies in range")
        void castAbility_withMultipleEnemiesInRange_shouldDamageAll() {
            // Rogue Atk: 40, Enemy Def: 0 -> Damage is 40
            int initialHealth = enemyInRange.getHealth().getAmount(); // 80

            rogue.castAbility(List.of(enemyInRange, anotherEnemyInRange, enemyOutOfRange));

            assertEquals(100 - rogue.getCost(), rogue.getEnergy().getAmount(), "Energy should be consumed.");
            assertEquals(initialHealth - 40, enemyInRange.getHealth().getAmount(), "First enemy in range should take damage.");
            assertEquals(initialHealth - 40, anotherEnemyInRange.getHealth().getAmount(), "Second enemy in range should take damage.");
            assertEquals(50, enemyOutOfRange.getHealth().getAmount(), "Enemy out of range should not take damage.");

            assertTrue(messageLog.stream().anyMatch(s -> s.contains("cast Fan of Knives")), "Cast message should be logged.");
            long hitCount = messageLog.stream().filter(s -> s.contains("ability damage")).count();
            assertEquals(2, hitCount, "Should log a damage message for each in-range enemy.");
        }

        @Test
        @DisplayName("should consume energy even if no targets are in range")
        void castAbility_withNoEnemiesInRange_shouldStillConsumeEnergy() {
            rogue.castAbility(List.of(enemyOutOfRange));

            assertEquals(100 - rogue.getCost(), rogue.getEnergy().getAmount(), "Energy should be consumed.");
            assertEquals(50, enemyOutOfRange.getHealth().getAmount(), "Enemy should not be damaged.");
            long hitCount = messageLog.stream().filter(s -> s.contains("ability damage")).count();
            assertEquals(0, hitCount, "No damage messages should be logged.");
        }

        @Test
        @DisplayName("should kill multiple enemies and trigger death callbacks")
        void castAbility_shouldKillEnemies() {
            // Set health low enough to be killed by the ability (damage is 40)
            enemyInRange.getHealth().setAmount(30);
            anotherEnemyInRange.getHealth().setAmount(40);

            rogue.castAbility(List.of(enemyInRange, anotherEnemyInRange));

            assertTrue(enemyInRange.isDead(), "First enemy should be dead.");
            assertTrue(anotherEnemyInRange.isDead(), "Second enemy should be dead.");

            assertEquals(2, deadEnemies.size(), "Two enemies should be reported as dead.");
            assertTrue(deadEnemies.contains(enemyInRange), "First dead enemy should be in the list.");
            assertTrue(deadEnemies.contains(anotherEnemyInRange), "Second dead enemy should be in the list.");
        }
    }
}