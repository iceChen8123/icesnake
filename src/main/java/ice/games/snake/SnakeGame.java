package ice.games.snake;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeGame {

	private final static Logger logger = LoggerFactory.getLogger(SnakeGame.class);

	protected static final AtomicInteger snakeIds = new AtomicInteger(0);
	protected final SnakeBroadcaster snakeBroadcaster;

	private OnopenProccesor onopenProccesor;
	private OncloseProccesor oncloseProccesor;

	public SnakeGame() {
		snakeBroadcaster = new SnakeBroadcaster(BroadcasterFactory.getDefault().lookup("/snake", true));
		this.onopenProccesor = new OnopenProccesor(snakeBroadcaster);
		this.oncloseProccesor = new OncloseProccesor(snakeBroadcaster);
	}

	public void onOpen(AtmosphereResource resource) throws IOException {
		onopenProccesor.onOpen(resource);
	}

	public void onClose(AtmosphereResource resource) {
		oncloseProccesor.onClose(resource);
	}

	protected Snake snake(AtmosphereResource resource) {
		return (Snake) resource.session().getAttribute("snake");
	}

}
