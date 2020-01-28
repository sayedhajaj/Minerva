import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Minerva {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
				System.out.println("Usage: Minerva [script]");
		} else {
			runFile(args[0]);
		}
	}

	public static void runFile(String fileName) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(fileName));
		run(new String(bytes, Charset.defaultCharset()));
	}

	public static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();
		for (Stmt statement : statements) {
			System.out.println(statement.toString());
		}
	}
}