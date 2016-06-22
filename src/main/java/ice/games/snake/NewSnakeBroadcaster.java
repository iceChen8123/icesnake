package ice.games.snake;

import ice.games.snake.Snake.SnakeStatus;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewSnakeBroadcaster {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final long TICK_DELAY = 100;

	private final Broadcaster broadcaster;

	private final SnakeManager snakeManager;

	private boolean isFirst = true;

	public NewSnakeBroadcaster(Broadcaster broadcaster, SnakeManager snakeManager) {
		this.broadcaster = broadcaster;
		this.snakeManager = snakeManager;
	}

	private ReentrantLock broadcastLock = new ReentrantLock();

	void broadcast(String message) {
		broadcastLock.lock();
		if (isFirst) {
			isFirst = false; // 只有广播过，才开始，因为会初始化两个广播类，这样就能少一个空跑的线程
			startTimer();
		}
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
				for (Iterator<Snake> iterator = snakeManager.getPlayingSnakes().iterator(); iterator.hasNext();) {
					Snake snake = iterator.next();
					snake.update(snakeManager.getPlayingSnakes()); // TODO
																	// 这边需要注意下，在一个tick内，是否要多次获取所有蛇???
					if (snake.getStatus() == SnakeStatus.dead) {
						snakeManager.removeDeadSnake(snake);
					} else {
						sb.append(snake.getLocationsJson());
						if (iterator.hasNext()) {
							sb.append(',');
						}
					}
				}
				return String.format("{'type': 'update', 'data' : [%s]}", sb.toString());
			}
		}, TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	public void removeAtmosphereResource(AtmosphereResource resource) {
		broadcaster.removeAtmosphereResource(resource);
	}
}
