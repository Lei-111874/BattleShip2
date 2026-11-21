package battleship;

import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - fire: 7
 * - getShots: 1
 * - getRepeatedShots: 1
 * - getInvalidShots: 1
 * - getHits: 1
 * - getSunkShips: 1
 * - getRemainingShips: 1
 * - validShot: 3
 * - repeatedShot: 2
 * - printBoard: 1
 * - printValidShots: 1
 * - printFleet: 1
 */
public class GameTest {

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet()); // Assuming Fleet is a concrete implementation of IFleet
	}

	@AfterEach
	void tearDown() {
		game = null;
	}

	@Test
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	void fire2() {
		Position invalidPosition = new Position(-1, 5);
		game.fireSingleShot(invalidPosition, false);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	void fire3() {
		Position position = new Position(2, 3);
		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}
	@Test
	void gettersAndOver() {
		assertNotNull(game.getMyFleet());
		assertNotNull(game.getAlienFleet());
		assertNotNull(game.getMyMoves());
		assertTrue(game.getMyMoves().isEmpty());
		assertEquals(0, game.getInvalidShots());
		assertEquals(0, game.getHits());
		assertEquals(0, game.getSunkShips());
		assertDoesNotThrow(() -> game.over());
	}
	@Test
	void fireSingleShotHitAndSink() {
		Ship barge = new Barge(Compass.NORTH, new Position(1, 1));
		game.getMyFleet().addShip(barge);

		assertNotNull(game.fireSingleShot(new Position(0, 0), false));
		assertEquals(0, game.getHits());
		assertEquals(0, game.getSunkShips());

		assertNotNull(game.fireSingleShot(new Position(1, 1), false));
		assertEquals(1, game.getHits());
		assertEquals(1, game.getSunkShips());
	}
	@Test
	void fireSingleShotHitWithoutSinking() {
		Ship caravel = new Caravel(Compass.EAST, new Position(3, 3));
		game.getMyFleet().addShip(caravel);

		assertNotNull(game.fireSingleShot(new Position(3, 3), false));
		assertEquals(1, game.getHits());
		assertEquals(0, game.getSunkShips());
	}
	@Test
	void fireSingleShotRepeatedFromPreviousMove() {
		game.fireShots(List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5)));
		assertNotNull(game.fireSingleShot(new Position(2, 3), false));
		assertEquals(1, game.getRepeatedShots());
	}
	@Test
	void fireShotsInvalidCount() {
		assertThrows(IllegalArgumentException.class,
				() -> game.fireShots(List.of(new Position(0, 0), new Position(0, 1))));
	}
	@Test
	void fireShotsDuplicateInSameMove() {
		Position p = new Position(4, 4);
		game.fireShots(List.of(p, p, new Position(4, 5)));
		assertEquals(1, game.getRepeatedShots());
		assertEquals(1, game.getAlienMoves().size());
	}
	@Test
	void readEnemyFireValidFormats() {
		String json1 = game.readEnemyFire(new java.util.Scanner("A1 B2 C3\n"));
		assertTrue(json1.contains("\"row\" : \"A\""));

		String json2 = game.readEnemyFire(new java.util.Scanner("D 4 E 5 F 6\n"));
		assertTrue(json2.contains("\"row\" : \"D\""));
		assertEquals(2, game.getAlienMoves().size());
	}
	@Test
	void readEnemyFireInvalidInputs() {
		assertThrows(IllegalArgumentException.class,
				() -> game.readEnemyFire(new java.util.Scanner("A\n")));
		assertThrows(IllegalArgumentException.class,
				() -> game.readEnemyFire(new java.util.Scanner("A1 B2\n")));
	}
	@Test
	void randomEnemyFireScenarios() {
		Ship barge = new Barge(Compass.NORTH, new Position(0, 0));
		game.getMyFleet().addShip(barge);
		barge.sink();

		assertDoesNotThrow(() -> game.randomEnemyFire());

		for (int r = 0; r < Game.BOARD_SIZE; r++) {
			List<IPosition> batch = new java.util.ArrayList<>();
			for (int c = 0; c < Game.BOARD_SIZE; c++) {
				batch.add(new Position(r, c));
				if (batch.size() == Game.NUMBER_SHOTS) {
					game.fireShots(batch);
					batch = new java.util.ArrayList<>();
				}
			}
		}
		List<IPosition> remaining = new java.util.ArrayList<>();
		for (int r = 0; r < Game.BOARD_SIZE; r++) {
			for (int c = 0; c < Game.BOARD_SIZE; c++) {
				Position candidate = new Position(r, c);
				if (!game.repeatedShot(candidate)) {
					remaining.add(candidate);
				}
			}
		}
		if (remaining.size() >= Game.NUMBER_SHOTS) {
			game.fireShots(remaining.subList(0, Game.NUMBER_SHOTS));
		}
		assertDoesNotThrow(() -> game.randomEnemyFire());
	}
	@Test
	void printBoardsAndJsonShots() {
		Ship barge = new Barge(Compass.NORTH, new Position(1, 1));
		game.getMyFleet().addShip(barge);
		barge.sink();

		Move moveWithOutsideAndInside = new Move(
				1,
				List.of(new Position(1, 1), new Position(0, 0), new Position(-1, 0)),
				List.of()
		);
		game.getAlienMoves().add(moveWithOutsideAndInside);

		assertDoesNotThrow(() -> Game.printBoard(game.getMyFleet(), game.getAlienMoves(), false, false));
		assertDoesNotThrow(() -> Game.printBoard(game.getMyFleet(), game.getAlienMoves(), true, true));
		assertDoesNotThrow(() -> game.printMyBoard(true, true));
		assertDoesNotThrow(() -> game.printAlienBoard(false, false));

		String json = Game.jsonShots(List.of(new Position(0, 0), new Position(1, 1)));
		assertTrue(json.contains("\"column\" : 1"));
	}


}