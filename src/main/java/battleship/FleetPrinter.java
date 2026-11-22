package battleship;

import java.util.List;

public class FleetPrinter {
    private final Fleet fleet;

    public FleetPrinter(Fleet fleet) {
        this.fleet = fleet;
    }

    /**
     * This operation prints all the given ships
     *
     * @param ships The list of ships
     */
    public void printShips(List<IShip> ships) {
        assert ships != null;

        for (IShip ship : ships)
            System.out.println(ship);
    }

    /**
     * This operation shows the state of a fleet
     */
    public void printStatus() {
        System.out.println("Estado da Frota: " + fleet.getFloatingShips().size() + " a flutuar, " + fleet.getSunkShips().size() + " afundados!");
//		printAllShips();
//		printFloatingShips();
//		printShipsByCategory("Galeao");
//		printShipsByCategory("Fragata");
//		printShipsByCategory("Nau");
//		printShipsByCategory("Caravela");
//		printShipsByCategory("Barca");
    }

    /**
     * This operation prints all the ships of a fleet belonging to a particular
     * category
     *
     * @param category The category of ships of interest
     */
    public void printShipsByCategory(String category) {
        assert category != null;

        printShips(fleet.getShipsLike(category));
    }

    /**
     * This operation prints all the ships of a fleet but not yet shot
     */
    public void printFloatingShips() {
        printShips(fleet.getFloatingShips());
    }

    /**
     * This operation prints all the ships of a fleet
     */
    void printAllShips() {
        printShips(fleet.getShips());
    }
}