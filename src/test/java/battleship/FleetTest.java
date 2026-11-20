package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test class for class Fleet.
 * Author: fabio
 * Date: 2026-11-17 10:32
 * Cyclomatic Complexity:
 * Fleet(): 1
 * createRandom(): 1
 * createRandom(Function): 4
 * getShips(): 1
 * addShip(): 4
 * getShipsLike(): 2
 * getFloatingShips(): 2
 * getSunkShips(): 2
 * shipAt(): 2
 * colisionRisk(): 2
 * printShips(): 2
 * printStatus(): 1
 * printShipsByCategory(): 1
 * printFloatingShips(): 1
 * printAllShips(): 1
 */
class FleetTest {

	private Fleet fleet;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
	}

	@AfterEach
	void tearDown() {
		fleet = null;
	}

	@Test
	@DisplayName("Constructs an empty fleet")
	void constructorCreatesAnEmptyFleet() {
		assertNotNull(fleet, "Error: expected a constructed fleet but got null");
		assertTrue(fleet.getShips().isEmpty(), "Error: expected a new fleet to contain no ships");
	}

	@Test
	@DisplayName("Creates a complete fleet with the expected ship categories")
	void createRandomBuildsACompleteFleet() {
		IFleet randomFleet = Fleet.createRandom();

		assertNotNull(randomFleet, "Error: expected createRandom to return a fleet but got null");
		assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size(),
				"Error: expected the generated fleet to have FLEET_SIZE ships");
		assertEquals(1, randomFleet.getShipsLike("Galeao").size(),
				"Error: expected one Galeao in the generated fleet");
		assertEquals(1, randomFleet.getShipsLike("Fragata").size(),
				"Error: expected one Fragata in the generated fleet");
		assertEquals(2, randomFleet.getShipsLike("Nau").size(),
				"Error: expected two Nau ships in the generated fleet");
		assertEquals(3, randomFleet.getShipsLike("Caravela").size(),
				"Error: expected three Caravela ships in the generated fleet");
		assertEquals(4, randomFleet.getShipsLike("Barca").size(),
				"Error: expected four Barca ships in the generated fleet");
	}

	@Test
	@DisplayName("createRandom path 1: retries until all ships are added")
	void createRandom1() {
		AtomicInteger calls = new AtomicInteger();
		AtomicInteger placements = new AtomicInteger();
		IFleet randomFleet = Fleet.createRandom(type -> {
			if (calls.getAndIncrement() == 0) {
				return null;
			}
			return bargeAt(placements.getAndIncrement());
		});

		assertAll(
				() -> assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size(),
						"Error: expected retries to produce a complete fleet"),
				() -> assertEquals(Fleet.FLEET_SIZE + 1, calls.get(),
						"Error: expected a null ship to be retried without advancing the fleet")
		);
	}

	@Test
	@DisplayName("createRandom path 2: generates only in-bounds ships")
	void createRandom2() {
		AtomicInteger calls = new AtomicInteger();
		IFleet randomFleet = Fleet.createRandom(type -> {
			int attempt = calls.getAndIncrement();
			return bargeAt(attempt < 2 ? 0 : attempt - 1);
		});

		assertAll(
				() -> assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size(),
						"Error: expected the generated fleet to reach its required size"),
				() -> assertEquals(Fleet.FLEET_SIZE + 1, calls.get(),
						"Error: expected the overlapping ship to be retried")
		);
	}

	@Test
	@DisplayName("createRandom path 3: returns the expected fleet categories")
	void createRandom3() {
		AtomicInteger calls = new AtomicInteger();
		IFleet randomFleet = Fleet.createRandom(type -> bargeAt(calls.getAndIncrement()));

		assertAll(
				() -> assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size(),
						"Error: expected the generated fleet to reach its required size"),
				() -> assertEquals(Fleet.FLEET_SIZE, calls.get(),
						"Error: expected every unique ship to be accepted on its first attempt")
		);
	}

	@Test
	@DisplayName("createRandom path 4: initializes every generated ship afloat")
	void createRandom4() {
		AtomicInteger calls = new AtomicInteger();
		IFleet randomFleet = Fleet.createRandom(type -> {
			int attempt = calls.getAndIncrement();
			if (attempt == 0) {
				return null;
			}
			return bargeAt(attempt < 3 ? 0 : attempt - 2);
		});

		assertAll(
				() -> assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size(),
						"Error: expected retries to produce a complete fleet"),
				() -> assertEquals(Fleet.FLEET_SIZE + 2, calls.get(),
						"Error: expected null and overlapping ships to be retried")
		);
	}

	@Test
	@DisplayName("Adds ships until the fleet is full")
	void addShipAcceptsValidShipsUntilFleetIsFull() {
		for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
			assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position((i / 5) * 2, (i % 5) * 2))),
					"Error: expected a valid, sufficiently distant ship to be added");
		}

		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
				"Error: expected the fleet to contain FLEET_SIZE ships");
		assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(9, 9))),
				"Error: expected a full fleet to reject an additional ship");
		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
				"Error: expected the rejected ship not to change the fleet size");
	}

	@Test
	@DisplayName("Rejects a ship outside the board")
	void addShipRejectsShipsOutsideBoard() {
		assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(99, 99))),
				"Error: expected a ship outside the board to be rejected");
		assertTrue(fleet.getShips().isEmpty(),
				"Error: expected a rejected out-of-bounds ship not to be added");
	}

	@Test
	@DisplayName("Rejects a ship too close to an existing ship")
	void addShipRejectsShipsTooCloseToExistingShips() {
		assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1))),
				"Error: expected the first valid ship to be added");
		assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(1, 2))),
				"Error: expected a ship adjacent to an existing ship to be rejected");
		assertEquals(1, fleet.getShips().size(),
				"Error: expected the nearby rejected ship not to be added");
	}

	@Test
	@DisplayName("Returns the fleet contents")
	void getShipsReturnsTheFleetContents() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertEquals(List.of(ship), fleet.getShips(),
				"Error: expected getShips to return the fleet's ship");
	}

	@Test
	@DisplayName("Returns matching ships and no ships for an unknown category")
	void getShipsLikeReturnsMatchingShipsOrAnEmptyList() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(barge);
		fleet.addShip(caravel);

		assertEquals(List.of(barge), fleet.getShipsLike("Barca"),
				"Error: expected only Barca ships to match the Barca category");
		assertTrue(fleet.getShipsLike("Unknown").isEmpty(),
				"Error: expected an unknown category to match no ships");
	}

	@Test
	@DisplayName("Partitions the fleet into floating and sunk ships")
	void floatingAndSunkShipQueriesPartitionFleet() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(barge);
		fleet.addShip(caravel);

		assertEquals(List.of(barge, caravel), fleet.getFloatingShips(),
				"Error: expected both newly added ships to be floating");
		assertTrue(fleet.getSunkShips().isEmpty(),
				"Error: expected a fleet with no sunk ships to return an empty list");

		barge.sink();
		assertEquals(List.of(caravel), fleet.getFloatingShips(),
				"Error: expected only the unsunk Caravela to remain floating");
		assertEquals(List.of(barge), fleet.getSunkShips(),
				"Error: expected the sunk Barca to be returned");
	}

	@Test
	@DisplayName("Finds the occupying ship or returns null")
	void shipAtReturnsTheOccupyingShipOrNull() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertSame(ship, fleet.shipAt(new Position(1, 1)),
				"Error: expected shipAt to return the ship occupying the position");
		assertNull(fleet.shipAt(new Position(5, 5)),
				"Error: expected shipAt to return null for an unoccupied position");
	}

	@Test
	@DisplayName("Prints ships and fleet status")
	void printMethodsPrintExpectedShipsAndStatus() {
		IShip floatingBarge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip sunkCaravel = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(floatingBarge);
		fleet.addShip(sunkCaravel);
		sunkCaravel.sink();

		assertEquals(floatingBarge + System.lineSeparator(),
				captureOutput(() -> fleet.printShips(List.of(floatingBarge))),
				"Error: expected printShips to print the supplied ship and a line separator");
		assertEquals(floatingBarge + System.lineSeparator(),
				captureOutput(() -> fleet.printShipsByCategory("Barca")),
				"Error: expected printShipsByCategory to print only Barca ships");
		assertEquals(floatingBarge + System.lineSeparator(), captureOutput(fleet::printFloatingShips),
				"Error: expected printFloatingShips to print only floating ships");
		assertEquals(floatingBarge + System.lineSeparator() + sunkCaravel + System.lineSeparator(),
				captureOutput(fleet::printAllShips),
				"Error: expected printAllShips to print every ship in fleet order");
		assertEquals("Estado da Frota: 1 a flutuar, 1 afundados!" + System.lineSeparator(),
				captureOutput(fleet::printStatus),
				"Error: expected printStatus to report one floating and one sunk ship");
	}

	@Test
	@DisplayName("Returns no ships when category filtering an empty fleet")
	void getShipsLike1() {
		assertTrue(fleet.getShipsLike("Barca").isEmpty(),
				"Error: expected filtering an empty fleet to return no ships");
	}

	@Test
	@DisplayName("Returns ships matching a category")
	void getShipsLike2() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertEquals(List.of(ship), fleet.getShipsLike("Barca"),
				"Error: expected the matching Barca to be returned");
	}

	@Test
	@DisplayName("Returns no floating ships for an empty fleet")
	void getFloatingShips1() {
		assertTrue(fleet.getFloatingShips().isEmpty(),
				"Error: expected an empty fleet to have no floating ships");
	}

	@Test
	@DisplayName("Returns only floating ships when the fleet contains a sunk ship")
	void getFloatingShips2() {
		IShip floating = new Barge(Compass.NORTH, new Position(1, 1));
		IShip sunk = new Barge(Compass.NORTH, new Position(4, 4));
		fleet.addShip(floating);
		fleet.addShip(sunk);
		sunk.sink();

		assertEquals(List.of(floating), fleet.getFloatingShips(),
				"Error: expected sunk ships to be excluded from floating ships");
	}

	@Test
	@DisplayName("Returns no sunk ships for an empty fleet")
	void getSunkShips1() {
		assertTrue(fleet.getSunkShips().isEmpty(),
				"Error: expected an empty fleet to have no sunk ships");
	}

	@Test
	@DisplayName("Returns only sunk ships when the fleet contains a floating ship")
	void getSunkShips2() {
		IShip floating = new Barge(Compass.NORTH, new Position(1, 1));
		IShip sunk = new Barge(Compass.NORTH, new Position(4, 4));
		fleet.addShip(floating);
		fleet.addShip(sunk);
		sunk.sink();

		assertEquals(List.of(sunk), fleet.getSunkShips(),
				"Error: expected floating ships to be excluded from sunk ships");
	}

	@Test
	@DisplayName("Returns null when no fleet ship occupies the requested position")
	void shipAt1() {
		assertNull(fleet.shipAt(new Position(1, 1)),
				"Error: expected shipAt to return null for an empty fleet");
	}

	@Test
	@DisplayName("Returns the first ship occupying the requested position")
	void shipAt2() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertSame(ship, fleet.shipAt(new Position(1, 1)),
				"Error: expected shipAt to return the occupying ship");
	}

	@Test
	@DisplayName("Prints no output for an empty ship list")
	void printShips1() {
		assertEquals("", captureOutput(() -> fleet.printShips(List.of())),
				"Error: expected printing an empty ship list to produce no output");
	}

	@Test
	@DisplayName("Prints every supplied ship")
	void printShips2() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));

		assertEquals(ship + System.lineSeparator(), captureOutput(() -> fleet.printShips(List.of(ship))),
				"Error: expected printing a ship list to print the ship and a line separator");
	}

	@Test
	@DisplayName("Accepts a valid ship when the fleet is empty")
	void addShip1() {
		assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1))),
				"Error: expected an in-bounds ship in an empty fleet to be accepted");
	}

	@Test
	@DisplayName("Rejects an out-of-bounds ship")
	void addShip2() {
		assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(99, 99))),
				"Error: expected an out-of-bounds ship to be rejected");
	}

	@Test
	@DisplayName("Rejects a ship that collides with the existing fleet")
	void addShip3() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));

		assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(1, 2))),
				"Error: expected a ship too close to the fleet to be rejected");
	}

	@Test
	@DisplayName("Rejects a ship after the fleet reaches capacity")
	void addShip4() {
		for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
			fleet.addShip(new Barge(Compass.NORTH, new Position((i / 5) * 2, (i % 5) * 2)));
		}

		assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(9, 9))),
				"Error: expected a full fleet to reject another ship");
	}

	@Test
	@DisplayName("colisionRisk path 1: accepts ships that are not too close")
	void colisionRisk1() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));

		assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position(4, 4))),
				"Error: expected a ship outside the collision distance to be accepted");
	}

	@Test
	@DisplayName("colisionRisk path 2: rejects ships that are too close")
	void colisionRisk2() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));

		assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(1, 2))),
				"Error: expected a ship inside the collision distance to be rejected");
	}

	@Test
	@DisplayName("Rejects null arguments guarded by Java assertions")
	void rejectsNullArguments() {
		assertAll(
				() -> assertThrows(AssertionError.class, () -> fleet.addShip(null),
						"Error: expected addShip(null) to trigger its non-null assertion"),
				() -> assertThrows(AssertionError.class, () -> fleet.getShipsLike(null),
						"Error: expected getShipsLike(null) to trigger its non-null assertion"),
				() -> assertThrows(AssertionError.class, () -> fleet.shipAt(null),
						"Error: expected shipAt(null) to trigger its non-null assertion"),
				() -> assertThrows(AssertionError.class, () -> fleet.printShips(null),
						"Error: expected printShips(null) to trigger its non-null assertion"),
				() -> assertThrows(AssertionError.class, () -> fleet.printShipsByCategory(null),
						"Error: expected printShipsByCategory(null) to trigger its non-null assertion"),
				() -> assertThrows(AssertionError.class, () -> invokeColisionRisk(null),
						"Error: expected colisionRisk(null) to trigger its non-null assertion")
		);
	}

	private boolean invokeColisionRisk(IShip ship) throws Throwable {
		Method method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
		method.setAccessible(true);
		try {
			return (boolean) method.invoke(fleet, ship);
		} catch (InvocationTargetException exception) {
			throw exception.getCause();
		}
	}

	private String captureOutput(Runnable action) {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
			System.setOut(capturedOut);
			action.run();
		} finally {
			System.setOut(originalOut);
		}
		return output.toString(StandardCharsets.UTF_8);
	}

	private Barge bargeAt(int index) {
		return new Barge(Compass.NORTH, new Position((index / 5) * 2, (index % 5) * 2));
	}
}
