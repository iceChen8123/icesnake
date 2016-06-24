package ice.games.snake.processor;

import ice.games.snake.SnakeManager;

import org.atmosphere.cpr.AtmosphereResource;

public class OncloseProccesor {

	private SnakeManager snakeManager;

	public OncloseProccesor(SnakeManager snakeManager) {
		super();
		this.snakeManager = snakeManager;
	}

	public void onClose(AtmosphereResource resource) {
		snakeManager.removeOffLineSnake(resource);
	}
}
