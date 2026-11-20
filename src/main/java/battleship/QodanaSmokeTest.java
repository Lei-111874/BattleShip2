package battleship;

/**
 * Small class used to verify that the Qodana workflow can analyze the project.
 */
public final class QodanaSmokeTest {

	private QodanaSmokeTest() {
	}

	/**
	 * Returns a deterministic value for a basic workflow smoke test.
	 *
	 * @return the smoke-test message
	 */
	public static String message() {
		return "Qodana workflow is working";
	}
}
