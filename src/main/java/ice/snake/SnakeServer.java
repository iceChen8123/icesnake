package ice.snake;

import ice.games.snake.base.SnakeManagedService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.atmosphere.nettosphere.Config;
import org.atmosphere.nettosphere.Nettosphere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeServer {

	private static final int PORT = 18080;
	private static final Logger logger = LoggerFactory.getLogger(Nettosphere.class);

	public static void main(String[] args) throws IOException {
		Config.Builder b = new Config.Builder();
		String resourcepath = "D:\\download\\icesnake\\src\\main\\resources";
		if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") < 0) {
			resourcepath = "/home/ice/jyzq/icesnake/target/classes";
		}
		b.resource(SnakeManagedService.class).resource(resourcepath).port(PORT).host("127.0.0.1").build();
		Nettosphere s = new Nettosphere.Builder().config(b.build()).build();
		s.start();
		String a = "";

		logger.info("NettoSphere Games Server started on port {}", PORT);
		logger.info("Type quit to stop the server");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (!(a.equals("quit"))) {
			a = br.readLine();
		}
		System.exit(-1);
	}

}
