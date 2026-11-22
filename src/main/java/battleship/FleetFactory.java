package battleship;

import java.util.function.Function;

final class FleetFactory {
	private FleetFactory() {
	}

	static IFleet createRandom() {
		return createRandom(type -> Ship.buildShip(type, Compass.randomBearing(), Position.randomPosition()));
	}

	static IFleet createRandom(Function<String, Ship> shipFactory) {
		IFleet randomFleet = new Fleet();
		String[] shipTypes = {
				"galeao",
				"fragata",
				"nau", "nau",
				"caravela", "caravela", "caravela",
				"barca", "barca", "barca", "barca"
		};

		int fleetSize = 0;
		while (fleetSize < shipTypes.length) {
			Ship ship = shipFactory.apply(shipTypes[fleetSize]);
			if (ship != null && randomFleet.addShip(ship)) {
				fleetSize++;
			}
		}
		return randomFleet;
	}
}
