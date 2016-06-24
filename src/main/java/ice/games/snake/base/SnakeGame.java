package ice.games.snake.base;

import ice.games.snake.SnakeManager;
import ice.games.snake.processor.MessageListener;
import ice.games.snake.processor.OncloseProccesor;
import ice.games.snake.processor.OnopenProccesor;

import java.io.IOException;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;

public class SnakeGame {

	private OnopenProccesor onopenProccesor;
	private OncloseProccesor oncloseProccesor;
	private SnakeManager snakeManager;

	public SnakeGame() {
		this.snakeManager = new SnakeManager();
		this.onopenProccesor = new OnopenProccesor(snakeManager);
		this.oncloseProccesor = new OncloseProccesor(snakeManager);
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

}
