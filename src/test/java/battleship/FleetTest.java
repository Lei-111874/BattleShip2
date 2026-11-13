package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Test class for Fleet.
 */
class FleetTest {

	private Fleet fleet;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
	}

	@Test
	void constructorCreatesAnEmptyFleet() {
		assertNotNull(fleet);
		assertTrue(fleet.getShips().isEmpty());
	}

	@Test
	void createRandomBuildsACompleteFleet() {
		IFleet randomFleet = Fleet.createRandom();

		assertNotNull(randomFleet);
		assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size());
		assertEquals(1, randomFleet.getShipsLike("Galeao").size());
		assertEquals(1, randomFleet.getShipsLike("Fragata").size());
		assertEquals(2, randomFleet.getShipsLike("Nau").size());
		assertEquals(3, randomFleet.getShipsLike("Caravela").size());
		assertEquals(4, randomFleet.getShipsLike("Barca").size());
	}

	@Test
	void addShipAcceptsValidShipsUntilFleetIsFull() {
		for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
			assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position((i / 5) * 2, (i % 5) * 2))));
		}

		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
		assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(9, 9))));
		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
	}

	@Test
	void addShipRejectsShipsOutsideBoard() {
		assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(99, 99))));
		assertTrue(fleet.getShips().isEmpty());
	}

	@Test
	void addShipRejectsShipsTooCloseToExistingShips() {
		assertTrue(fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1))));
		assertFalse(fleet.addShip(new Barge(Compass.NORTH, new Position(1, 2))));
		assertEquals(1, fleet.getShips().size());
	}

	@Test
	void getShipsReturnsTheFleetContents() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertEquals(List.of(ship), fleet.getShips());
	}

	@Test
	void getShipsLikeReturnsMatchingShipsOrAnEmptyList() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(barge);
		fleet.addShip(caravel);

		assertEquals(List.of(barge), fleet.getShipsLike("Barca"));
		assertTrue(fleet.getShipsLike("Unknown").isEmpty());
	}

	@Test
	void floatingAndSunkShipQueriesPartitionFleet() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(barge);
		fleet.addShip(caravel);

		assertEquals(List.of(barge, caravel), fleet.getFloatingShips());
		assertTrue(fleet.getSunkShips().isEmpty());

		barge.sink();
		assertEquals(List.of(caravel), fleet.getFloatingShips());
		assertEquals(List.of(barge), fleet.getSunkShips());
	}

	@Test
	void shipAtReturnsTheOccupyingShipOrNull() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertSame(ship, fleet.shipAt(new Position(1, 1)));
		assertNull(fleet.shipAt(new Position(5, 5)));
	}

	@Test
	void printMethodsPrintExpectedShipsAndStatus() {
		IShip floatingBarge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip sunkCaravel = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(floatingBarge);
		fleet.addShip(sunkCaravel);
		sunkCaravel.sink();

		assertEquals(floatingBarge + System.lineSeparator(),
				captureOutput(() -> fleet.printShips(List.of(floatingBarge))));
		assertEquals(floatingBarge + System.lineSeparator(),
				captureOutput(() -> fleet.printShipsByCategory("Barca")));
		assertEquals(floatingBarge + System.lineSeparator(), captureOutput(fleet::printFloatingShips));
		assertEquals(floatingBarge + System.lineSeparator() + sunkCaravel + System.lineSeparator(),
				captureOutput(fleet::printAllShips));
		assertEquals("Estado da Frota: 1 a flutuar, 1 afundados!" + System.lineSeparator(),
				captureOutput(fleet::printStatus));
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
}