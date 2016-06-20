package ice.games.snake;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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

	private final Broadcaster broadcaster;

	public SnakeBroadcaster(Broadcaster broadcaster) {
		this.broadcaster = broadcaster;
	}

	public SnakeBroadcaster broadcast(String message) {
		broadcaster.broadcast(message);
		return this;
	}

	protected synchronized void addSnake(Snake snake) {
		if (snakes.size() == 0) {
			startTimer();
		}
		snakes.put(Integer.valueOf(snake.getId()), snake);

		StringBuilder sb = new StringBuilder();
		for (Iterator<Snake> iterator = getSnakes().iterator(); iterator.hasNext();) {
			// 新添加一条时,需要将所有的广播一遍,其实只需要向新增加的广播所有，而像其他广播新增一条,但是这样写，比较容易。因为页面上直接用id去找蛇的，所以如果新增后，不广播所有，那么新增的蛇，会看不到其他的蛇。
			snake = iterator.next();
			sb.append(String.format("{id: %d, color: '%s'}", Integer.valueOf(snake.getId()), snake.getHexColor()));
			if (iterator.hasNext()) {
				sb.append(',');
			}
		}
		broadcast(String.format("{'type': 'join','data':[%s]}", sb.toString()));
	}

	protected Collection<Snake> getSnakes() {
		return Collections.unmodifiableCollection(snakes.values());
	}

	protected synchronized void removeSnake(Snake snake) {
		snakes.remove(Integer.valueOf(snake.getId()));
		broadcast(String.format("{'type': 'leave', 'id': %d}", snake.getId()));
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
