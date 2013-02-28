import org.slf4j.spi.LocationAwareLogger;
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		LocationAwareLogger x;
		
		  ClassLoader loader = Main.class.getClassLoader();
        System.out.println(loader.getResource("org/slf4j/spi/LocationAwareLogger.class"));

	}
	

}
