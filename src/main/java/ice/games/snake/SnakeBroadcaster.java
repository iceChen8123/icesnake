package ice.games.snake;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up the timer for the multi-player snake game WebSocket example.
 */
public class SnakeBroadcaster {

	private static final int MAX_ALIVE_SNAKE = 2;

	private final static Logger logger = LoggerFactory.getLogger(SnakeBroadcaster.class);

	private final long TICK_DELAY = 100;

	private final ConcurrentHashMap<Integer, Snake> snakes = new ConcurrentHashMap<Integer, Snake>();

	private final LinkedList<Snake> waitqueue = new LinkedList<Snake>();

	private final Broadcaster broadcaster;

	public SnakeBroadcaster(Broadcaster broadcaster) {
		this.broadcaster = broadcaster;
		startTimer();
	}

	synchronized void addSnake(Snake snake) {
		if (snakes.size() >= MAX_ALIVE_SNAKE) {
			snake.sendMessage(String.format("{'type': 'wait', 'data' : '请稍等,蛇满为患了,您前面还有  %s 条小蛇蛇在焦急等待...'}",
					waitqueue.size()));
			waitqueue.add(snake);
		} else {
			snake.startPlay();
			snakes.put(Integer.valueOf(snake.getId()), snake);
		}
	}

	synchronized void removeOffLineSnake(Snake snake) {
		snakes.remove(Integer.valueOf(snake.getId()));
		waitqueue.remove(snake);
	}

	Collection<Snake> getPlayingSnakes() {
		return Collections.unmodifiableCollection(snakes.values());
	}

	private ReentrantLock broadcastLock = new ReentrantLock();

	void broadcast(String message) {
		broadcastLock.lock();
		try {
			Future<Object> broadcast = broadcaster.broadcast(message);
			broadcast.get();
		} catch (Exception e) {
			logger.warn("broadcast message:[" + message + "] failed", e);
		} finally {
			broadcastLock.unlock();
		}
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

			private String tick() {
				StringBuilder sb = new StringBuilder();
				for (Iterator<Snake> iterator = getPlayingSnakes().iterator(); iterator.hasNext();) {
					Snake snake = iterator.next();
					snake.update(getPlayingSnakes());
					sb.append(snake.getLocationsJson());
					if (iterator.hasNext()) {
						sb.append(',');
					}
				}
				return String.format("{'type': 'update', 'data' : [%s]}", sb.toString());
			}
		}, TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	synchronized void removeResource(AtmosphereResource resource) {
		broadcaster.removeAtmosphereResource(resource);
	}
}
