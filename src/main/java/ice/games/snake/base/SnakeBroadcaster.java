package ice.games.snake.base;

import ice.games.snake.SnakeManager;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeBroadcaster {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final long TICK_DELAY = 100;

	private final Broadcaster broadcaster;

	private final SnakeManager snakeManager;

	private boolean isFirst = true;

	public SnakeBroadcaster(Broadcaster broadcaster, SnakeManager snakeManager) {
		this.broadcaster = broadcaster;
		this.snakeManager = snakeManager;
	}

	private ReentrantLock broadcastLock = new ReentrantLock();

	public void removeOffLineSnake(AtmosphereResource resource) {
		broadcaster.removeAtmosphereResource(resource);
	}

	public void broadcast(String message) {
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
		broadcaster.scheduleFixedBroadcast(snakeManager, TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

}
