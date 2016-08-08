package ice.games.snake.processor;

import ice.games.snake.Snake;
import ice.games.snake.base.Direction;

import org.atmosphere.cpr.AtmosphereResource;

public class MessageListener {

	public static void onMessage(AtmosphereResource resource, String message) {
		Snake snake = (Snake) resource.session().getAttribute("snake");
		if (message.startsWith("name")) {
			snake.setName(message.split(":")[1].trim());
			System.out.println("小蛇名字: " + message);
		} else {
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

}
