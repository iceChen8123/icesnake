package ice.snake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.atmosphere.nettosphere.Config;
import org.atmosphere.nettosphere.Nettosphere;
import org.nettosphere.samples.games.SnakeManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeServer {

	private static final int PORT = 18080;
	private static final Logger logger = LoggerFactory.getLogger(Nettosphere.class);

	public static void main(String[] args) throws IOException {
		Config.Builder b = new Config.Builder();
		b.resource(SnakeManagedService.class).resource("D:\\workspacem\\icesnake\\src\\main\\resources").port(PORT)
				.host("127.0.0.1").build();
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
