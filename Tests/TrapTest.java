import Callbacks.ChangePositionCallback;
import Callbacks.EnemyDeathCallback;
import Callbacks.MessageCallback;
import Tiles.Tile;
import Tiles.Units.Enemies.Enemy;
import Tiles.Units.Enemies.Trap;
import Tiles.Units.Players.Player;
import Tiles.Units.Players.Warrior;
import Utils.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Trap Class Tests")
class TrapTest {

    private Trap trap;
    private Player testPlayer;
    private List<String> messageLog;
    private Enemy deadEnemy;

    // Callbacks to capture events without using mocks
    private MessageCallback mcb;
    private EnemyDeathCallback edcb;
    private ChangePositionCallback pcb;

    @BeforeEach
    void setUp() {
        // Reset callback capture variables before each test
        messageLog = new ArrayList<>();
        deadEnemy = null;

        // Initialize callbacks
        mcb = messageLog::add;
        edcb = enemy -> deadEnemy = enemy;
        pcb = (t1, t2) -> { /* Position swap not critical for most trap tests */ };

        // Create a standard trap for tests
        // Trap: Bonus Trap, char 'B', 1 HP, 1 Atk, 1 Def, 250 XP, 1 tick visible, 5 ticks invisible
        trap = new Trap("Bonus Trap", 'B', 1, 1, 1, 250, 1, 5);
        trap.setCallbacks(mcb, pcb, edcb);
        trap.setPosition(new Position(5, 5));

        // Create a player for interaction tests
        testPlayer = new Warrior("Test Player", 100, 100, 100, 5); // Strong player to ensure kills
        testPlayer.setPosition(new Position(0, 0));
        testPlayer.setCallbacks(mcb, pcb, () -> {});
        trap.setPlayerPosition(testPlayer.getPosition());
    }

    @Nested
    @DisplayName("Constructor and Initial State")
    class ConstructorAndState {

        @Test
        @DisplayName("should correctly initialize all trap attributes")
        void testTrapConstructor() {
            assertEquals("Bonus Trap", trap.getName(), "Name should be initialized correctly.");
            assertEquals('B', trap.getCharacter(), "Character tile should be initialized correctly.");
            assertEquals(1, trap.getHealth().getCapacity(), "Health capacity should be set.");
            assertEquals(1, trap.getAttackPoints(), "Attack points should be set.");
            assertEquals(1, trap.getDefensePoints(), "Defense points should be set.");
            assertEquals(250, trap.getXP(), "Experience value should be set.");
            // Private fields visibilityTime and invisibilityTime are tested via gameTick behavior.
        }

        @Test
        @DisplayName("should be visible and have its character on initialization")
        void testInitialVisibility() {
            assertTrue(trap.isVisible(), "Trap should be visible on initialization.");
            assertEquals('B', trap.getCharacter(), "getCharacter should return the trap's character when visible.");
        }

        @Test
        @DisplayName("description method should include all trap stats")
        void testDescription() {
            String desc = trap.description();
            assertTrue(desc.contains("Bonus Trap"), "Description should contain the name.");
            assertTrue(desc.contains("Health: 1/1"), "Description should contain health.");
            assertTrue(desc.contains("Attack: 1"), "Description should contain attack points.");
            assertTrue(desc.contains("Defense: 1"), "Description should contain defense points.");
            assertTrue(desc.contains("Experience Value: 250"), "Description should contain experience value.");
        }
    }

    @Nested
    @DisplayName("Visibility and Game Tick Logic")
    class VisibilityAndTick {

        @Test
        @DisplayName("should stay visible during its visibilityTime")
        void testGameTick_StaysVisibleDuringVisibilityTime() {
            // visibilityTime is 1, so it's visible only on tick 0.
            trap.gameTick(); // ticksCount becomes 1
            assertFalse(trap.isVisible(), "Trap should become invisible after visibilityTime expires.");
            assertEquals('.', trap.getCharacter(), "getCharacter should return '.' when invisible.");
        }

        @Test
        @DisplayName("should stay invisible during its invisibilityTime")
        void testGameTick_StaysInvisibleDuringInvisibilityTime() {
            trap.gameTick(); // ticksCount becomes 1, trap is invisible
            trap.gameTick(); // ticksCount becomes 2
            trap.gameTick(); // ticksCount becomes 3
            trap.gameTick(); // ticksCount becomes 4
            trap.gameTick(); // ticksCount becomes 5
            assertFalse(trap.isVisible(), "Trap should remain invisible throughout its invisibilityTime.");
            assertEquals('.', trap.getCharacter(), "getCharacter should still be '.' before the cycle resets.");
        }

        @Test
        @DisplayName("should become visible again after a full cycle")
        void testGameTick_BecomesVisibleAfterFullCycle() {
            // A full cycle is visibilityTime + invisibilityTime = 1 + 5 = 6 ticks.
            for (int i = 0; i < 6; i++) {
                trap.gameTick();
            }
            // After 6 ticks, ticksCount resets to 0.
            assertTrue(trap.isVisible(), "Trap should become visible again after the full cycle resets.");
            assertEquals('B', trap.getCharacter(), "getCharacter should return the trap's character on the new cycle.");
        }
    }

    @Nested
    @DisplayName("Targeting and Movement")
    class Targeting {

        @Test
        @DisplayName("should not move when player is out of range")
        void testGameTick_PlayerOutOfRange_DoesNotMove() {
            // Player is far away
            testPlayer.setPosition(new Position(10, 10));
            trap.setPlayerPosition(testPlayer.getPosition());
            trap.gameTick();

            // Desired position should be its own position
            assertEquals(trap.getPosition(), trap.getDesiredPosition(), "Trap should not desire to move when player is out of range.");
        }

        @Test
        @DisplayName("should target player's position when player is in range")
        void testGameTick_PlayerInRange_TargetsPlayer() {
            // Player is adjacent (range < 2)
            testPlayer.setPosition(new Position(5, 6));
            trap.setPlayerPosition(testPlayer.getPosition());
            trap.gameTick();

            // Desired position should be the player's position
            assertEquals(testPlayer.getPosition(), trap.getDesiredPosition(), "Trap should desire to move to the player's position when in range.");
        }

        @Test
        @DisplayName("should not target player's position when player is exactly 2 units away")
        void testGameTick_PlayerAtEdgeOfRange_DoesNotTargetPlayer() {
            // Player is at range 2.0, so condition range < 2 is false.
            testPlayer.setPosition(new Position(7, 5));
            trap.setPlayerPosition(testPlayer.getPosition());
            trap.gameTick();

            assertEquals(trap.getPosition(), trap.getDesiredPosition(), "Trap should not target player when distance is exactly 2.");
        }
    }

    @Nested
    @DisplayName("Interaction and Combat")
    class Interaction {

        @Test
        @DisplayName("visit(Player) should initiate combat")
        void testVisitPlayer_InitiatesCombat() {
            int playerInitialHealth = testPlayer.getHealth().getAmount();

            // Trap visits player, triggering combat
            trap.visit(testPlayer);

            assertTrue(messageLog.stream().anyMatch(s -> s.contains("engaged in combat with")), "Combat engagement message should be logged.");
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("rolled") && s.contains("attack points")), "Attack roll message should be logged.");
            // As damage can be 0, we verify combat was attempted via the logs.
            assertFalse(messageLog.isEmpty(), "Messages should be logged during combat.");
        }

        @Test
        @DisplayName("onDeath should call the EnemyDeathCallback")
        void testOnDeath_CallsCallback() {
            // Directly call onDeath to test its specific logic
            trap.onDeath(testPlayer);

            // Check that a death message was logged
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("Bonus Trap died.")), "Death message should be logged.");

            // Check that the enemy death callback was triggered with this trap instance
            assertNotNull(deadEnemy, "EnemyDeathCallback should have been called.");
            assertEquals(trap, deadEnemy, "The callback should receive the correct trap instance.");
        }

        @Test
        @DisplayName("should be killed in combat and trigger onDeath")
        void testCombat_LeadsToDeath() {
            // Player is strong enough to kill the trap in one hit
            testPlayer.setPosition(new Position(5,5)); // Ensure interaction
            trap.getHealth().setAmount(1);

            // Player visits (attacks) the trap
            testPlayer.visit(trap);

            assertTrue(trap.isDead(), "Trap should be dead after combat.");
            assertNotNull(deadEnemy, "EnemyDeathCallback should have been triggered by combat death.");
            assertEquals(trap, deadEnemy, "The correct trap should be reported as dead.");
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("Bonus Trap died.")), "Death message should be logged after combat death.");
        }
    }
}