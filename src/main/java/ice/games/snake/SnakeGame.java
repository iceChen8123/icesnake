package ice.games.snake;

import java.io.IOException;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.BroadcasterFactory;

public class SnakeGame {

	private final SnakeBroadcaster snakeBroadcaster;
	private OnopenProccesor onopenProccesor;
	private OncloseProccesor oncloseProccesor;
	private SnakeManager snakeManager;

	public SnakeGame() {
		this.snakeManager = new SnakeManager();
		snakeBroadcaster = new SnakeBroadcaster(BroadcasterFactory.getDefault().lookup("/snake", true), snakeManager);
		this.onopenProccesor = new OnopenProccesor(snakeBroadcaster, snakeManager);
		this.oncloseProccesor = new OncloseProccesor(snakeBroadcaster);
	}

	public void onOpen(AtmosphereResource resource) throws IOException {
		onopenProccesor.onOpen(resource);
	}

	public void onClose(AtmosphereResource resource) {
		oncloseProccesor.onClose(resource);
	}

	public void onMessage(AtmosphereResource resource) throws IOException {
		// Here we need to find the suspended AtmosphereResource
		MessageListener.onMessage(AtmosphereResourceFactory.getDefault().find(resource.uuid()), resource.getRequest()
				.getReader().readLine());
	}

	private static class MessageListener {

		static void onMessage(AtmosphereResource resource, String message) {
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
}
