package ice.games.snake.processor;

import ice.games.snake.Snake;
import ice.games.snake.SnakeManager;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.atmosphere.cpr.AtmosphereResource;

public class OnopenProccesor {

	protected static final AtomicInteger snakeIds = new AtomicInteger(0);

	private SnakeManager snakeManager;

	public OnopenProccesor(SnakeManager snakeManager) {
		super();
		this.snakeManager = snakeManager;
	}

	public void onOpen(AtmosphereResource resource) throws IOException {
		int id = snakeIds.getAndIncrement();
		resource.session().setAttribute("id", id);
		Snake snake = new Snake(id, resource);
		resource.session().setAttribute("snake", snake);

		snakeManager.addNewPlaySnake(snake);
	}

}
