package ice.games.snake;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.atmosphere.cpr.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up the timer for the multi-player snake game WebSocket example.
 */
public class SnakeBroadcaster {

	private final static Logger logger = LoggerFactory.getLogger(SnakeBroadcaster.class);

	private final long TICK_DELAY = 100;

	private final Broadcaster broadcaster;

	private SnakeGame snakeGame;

	public SnakeBroadcaster(Broadcaster broadcaster, SnakeGame snakeGame) {
		this.snakeGame = snakeGame;
		this.broadcaster = broadcaster;
		startTimer();
	}

	public SnakeBroadcaster broadcast(String message) {
		broadcaster.broadcast(message);
		return this;
	}

	private String tick() {
		return String.format("{'type': 'update', 'data' : [%s]}", snakeGame.getSnakeInfo());
	}

	private void startTimer() {
		broadcaster.scheduleFixedBroadcast(new Callable<String>() {
			@Override
			public String call() {
				try {
					return tick();
				} catch (RuntimeException e) {
					logger.error("Caught to prevent timer from shutting down", e);
				}
				return "";
			}
		}, TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}
}
