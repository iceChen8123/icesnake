package ice.games.snake;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeGame {

	private final static Logger logger = LoggerFactory.getLogger(SnakeGame.class);

	protected static final AtomicInteger snakeIds = new AtomicInteger(0);
	protected final SnakeBroadcaster snakeBroadcaster;

	public SnakeGame() {
		snakeBroadcaster = new SnakeBroadcaster(BroadcasterFactory.getDefault().lookup("/snake", true));
	}

	public void onOpen(AtmosphereResource resource) throws IOException {
		int id = snakeIds.getAndIncrement();
		resource.session().setAttribute("id", id);
		Snake snake = new Snake(id, resource);
		resource.session().setAttribute("snake", snake);

		Snake snaketemp;
		StringBuilder sb = new StringBuilder();
		for (Iterator<Snake> iterator = snakeBroadcaster.getPlayingSnakes().iterator(); iterator.hasNext();) {
			snaketemp = iterator.next();
			sb.append(String.format("{id: %d, color: '%s'}", Integer.valueOf(snaketemp.getId()),
					snaketemp.getHexColor()));
			if (iterator.hasNext()) {
				sb.append(',');
			}
		}
		snake.sendMessage(String.format("{'type': 'playinginfo','data':[%s]}", sb.toString()));

		snakeBroadcaster.addSnake(snake);
	}

	public void onClose(AtmosphereResource resource) {
		snakeBroadcaster.removeResource(resource);
		snakeBroadcaster.removeOffLineSnake(snake(resource));
		snakeBroadcaster.broadcast(String.format("{'type': 'leave', 'id': %d}", ((Integer) resource.session()
				.getAttribute("id"))));
	}

	protected Snake snake(AtmosphereResource resource) {
		return (Snake) resource.session().getAttribute("snake");
	}

	protected void onMessage(AtmosphereResource resource, String message) {
		Snake snake = snake(resource);
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
