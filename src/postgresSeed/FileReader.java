package postgresSeed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileReader {
	
	public static List<String> readFile(String name) {
		File file = new File("src/resources/" + name);
		try {
			List<String> s = Files.readAllLines(file.toPath());
			return s;
		} catch (IOException e) {
			return null;
		}
	}
}
