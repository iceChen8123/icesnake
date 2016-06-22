package ice.games.snake;

import ice.games.snake.Snake.SnakeStatus;

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

	private final ConcurrentHashMap<Integer, Snake> playingSnakes = new ConcurrentHashMap<Integer, Snake>();

	private final LinkedList<Snake> waitqueue = new LinkedList<Snake>();

	private final Broadcaster broadcaster;

	private boolean isFirst = true;

	public SnakeBroadcaster(Broadcaster broadcaster) {
		this.broadcaster = broadcaster;
	}

	synchronized void addSnake(Snake snake) {
		if (isFirst) {
			isFirst = false; // 只有添加过蛇，才开始，因为会初始化两个广播类，这样就能少一个空跑的线程
			startTimer();
		}
		waitForPlay(snake);
		if (playingSnakesNum() >= MAX_ALIVE_SNAKE) {
			snake.sendMessage(String.format("{'type': 'wait', 'data' : '请稍等,蛇满为患了,您前面还有  %s 条小蛇蛇在焦急等待...'}",
					waitSnakesNum() - 1)); // 因为一来，就进等待队列，所以里面总会多一个。
		} else {
			activeWaitSnake();
		}
	}

	private synchronized void removeDeadSnake(Snake snake) {
		int snakeId = snake.getId();
		removePlayingSnake(snakeId);
		broadcast(String.format("{'type': 'dead', 'id': %d}", snakeId));
		addSnake(snake);
		activeWaitSnake();
	}

	private void broadcastPlayingSnakeInfo() {
		Snake snake;
		StringBuilder sb = new StringBuilder();
		for (Iterator<Snake> iterator = getPlayingSnakes().iterator(); iterator.hasNext();) {
			snake = iterator.next();
			sb.append(String.format("{id: %d, color: '%s'}", Integer.valueOf(snake.getId()), snake.getHexColor()));
			if (iterator.hasNext()) {
				sb.append(',');
			}
		}
		broadcast(String.format("{'type': 'join','data':[%s]}", sb.toString()));
	}

	private void activeWaitSnake() {
		if (waitSnakesNum() > 0) {
			Snake firstWait = getFirstWaitSnake();
			firstWait.startPlay();
			addNewPlayingSnake(firstWait);
			broadcastPlayingSnakeInfo();
		}
	}

	synchronized void removeOffLineSnake(AtmosphereResource resource) {
		broadcaster.removeAtmosphereResource(resource);
		Snake snake = (Snake) resource.session().getAttribute("snake");
		removePlayingSnake(snake.getId());
		removeWaitSnake(snake);

		broadcast(String.format("{'type': 'leave', 'id': %d}", ((Integer) resource.session().getAttribute("id"))));
	}

	private int waitSnakesNum() {
		return waitqueue.size();
	}

	private void waitForPlay(Snake snake) {
		waitqueue.add(snake);
	}

	private Snake getFirstWaitSnake() {
		return waitqueue.removeFirst();
	}

	private boolean removeWaitSnake(Snake snake) {
		return waitqueue.remove(snake);
	}

	private void addNewPlayingSnake(Snake firstWait) {
		playingSnakes.put(Integer.valueOf(firstWait.getId()), firstWait);
	}

	private int playingSnakesNum() {
		return playingSnakes.size();
	}

	private void removePlayingSnake(int snakeId) {
		playingSnakes.remove(snakeId);
	}

	Collection<Snake> getPlayingSnakes() {
		return Collections.unmodifiableCollection(playingSnakes.values());
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
					if (snake.getStatus() == SnakeStatus.dead) {
						removeDeadSnake(snake);
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

}
