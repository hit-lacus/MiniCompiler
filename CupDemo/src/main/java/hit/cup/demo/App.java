package hit.cup.demo;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws Exception {
		System.out.println("Please type your arithmethic expression:");
		Parser p = new Parser(new SimpleScanner());
		p.parse();
	}
}
