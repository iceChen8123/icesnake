package ice.games.snake;

import java.io.IOException;
import java.util.Iterator;
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

		sendPlayingSnakeInfoToNew(snake);

		snakeManager.addSnake(snake);
	}

	private void sendPlayingSnakeInfoToNew(Snake snake) {
		Snake snaketemp;
		StringBuilder sb = new StringBuilder();
		for (Iterator<Snake> iterator = snakeManager.getPlayingSnakes().iterator(); iterator.hasNext();) {
			snaketemp = iterator.next();
			sb.append(String.format("{id: %d, color: '%s'}", Integer.valueOf(snaketemp.getId()),
					snaketemp.getHexColor()));
			if (iterator.hasNext()) {
				sb.append(',');
			}
		}
		snake.sendMessage(String.format("{'type': 'playinginfo','data':[%s]}", sb.toString()));
	}
}
