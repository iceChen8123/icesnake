package ice.games.snake;

import java.util.Collection;
import java.util.Iterator;
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

	public SnakeBroadcaster(Broadcaster broadcaster) {
		this.broadcaster = broadcaster;
		startTimer();
	}

	public SnakeBroadcaster broadcast(String message) {
		broadcaster.broadcast(message);
		return this;
	}

	private String tick() {
		return String.format("{'type': 'update', 'data' : [%s]}", getSnakeInfo());
	}

	private String getSnakeInfo() {
		Collection<Snake> snakes = SnakeGame.getSnakes();
		if (snakes == null || snakes.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Iterator<Snake> iterator = SnakeGame.getSnakes().iterator(); iterator.hasNext();) {
			Snake snake = iterator.next();
			sb.append(snake.getLocationsJson());
			if (iterator.hasNext()) {
				sb.append(',');
			}
		}
		return sb.toString();
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
