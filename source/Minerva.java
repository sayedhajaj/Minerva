

public class Minerva {

	public static void main(String[] args) {
		switch (args.length) {
			case 0:
				runRepl();
				break;
			case 1: 
				runFile(args[0]);
				break;
			default:
				System.out.println("Usage: Minerva [script]");
		}
	}
	public static void runRepl() {

	}

	public static void runFile(String fileName) {

	}

	public static void run(String source) {
		Scanner scanner = new Scanner(source);

	}
}