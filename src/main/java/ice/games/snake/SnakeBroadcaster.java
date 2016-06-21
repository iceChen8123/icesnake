package ice.games.snake;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
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

	private final ConcurrentHashMap<Integer, Snake> snakes = new ConcurrentHashMap<Integer, Snake>();

	private final LinkedList<Snake> waitqueue = new LinkedList<Snake>();

	private final Broadcaster broadcaster;

	public SnakeBroadcaster(Broadcaster broadcaster) {
		this.broadcaster = broadcaster;
	}

	protected synchronized void addSnake(Snake snake) {
		if (snakes.size() == 0) {
			startTimer();
		}
		if (snakes.size() >= 2) {
			logger.info("超额了...");
			snake.sendMessage(String.format("{'type': 'wait', 'data' : '请稍等,您前面还有  %s 条蛇蛇在焦急等待...'}", waitqueue.size()
					+ ""));
			waitqueue.add(snake);
		} else {
			snakes.put(Integer.valueOf(snake.getId()), snake);
		}
	}

	protected Collection<Snake> getSnakes() {
		return Collections.unmodifiableCollection(snakes.values());
	}

	protected synchronized void removeSnake(Snake snake) {
		snakes.remove(Integer.valueOf(snake.getId()));
		waitqueue.remove(snake);
	}

	protected String tick() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Snake> iterator = getSnakes().iterator(); iterator.hasNext();) {
			Snake snake = iterator.next();
			snake.update(getSnakes());
			sb.append(snake.getLocationsJson());
			if (iterator.hasNext()) {
				sb.append(',');
			}
		}
		return String.format("{'type': 'update', 'data' : [%s]}", sb.toString());
	}

	public SnakeBroadcaster broadcast(String message) {
		broadcaster.broadcast(message);
		return this;
	}

	public void startTimer() {
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
