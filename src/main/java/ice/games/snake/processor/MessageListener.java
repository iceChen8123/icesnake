package ice.games.snake.processor;

import ice.games.snake.Snake;
import ice.games.snake.base.Direction;

import org.atmosphere.cpr.AtmosphereResource;

public class MessageListener {

	public static void onMessage(AtmosphereResource resource, String message) {
		Snake snake = (Snake) resource.session().getAttribute("snake");
		if ("left".equals(message)) {
			snake.setDirection(Direction.LEFT);
		} else if ("up".equals(message)) {
			snake.setDirection(Direction.UP);
		} else if ("right".equals(message)) {
			snake.setDirection(Direction.RIGHT);
		} else if ("down".equals(message)) {
			snake.setDirection(Direction.DOWN);
		}
	}

}
