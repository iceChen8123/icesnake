package ice.games.snake;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeManager {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ConcurrentHashMap<Integer, Snake> playingSnakes = new ConcurrentHashMap<Integer, Snake>();

	private final LinkedList<Snake> waitqueue = new LinkedList<Snake>();

	private static final int MAX_ALIVE_SNAKE = 2;

	private NewSnakeBroadcaster snakeBroadcaster;

	public SnakeManager() {
		this.snakeBroadcaster = new NewSnakeBroadcaster(BroadcasterFactory.getDefault().lookup("/snake", true), this);
	}

	Collection<Snake> getPlayingSnakes() {
		return Collections.unmodifiableCollection(playingSnakes.values());
	}

	void addNewPlayingSnake(Snake firstWait) {
		playingSnakes.put(Integer.valueOf(firstWait.getId()), firstWait);
		logger.info("蛇 {} 开始游戏...", firstWait.getId());
	}

	int playingSnakesNum() {
		return playingSnakes.size();
	}

	void removePlayingSnake(int snakeId) {
		playingSnakes.remove(snakeId);
		logger.info("蛇 {} 退出游戏...", snakeId);
	}

	int waitSnakesNum() {
		return waitqueue.size();
	}

	void waitForPlay(Snake snake) {
		waitqueue.add(snake);
		logger.info("蛇 {} 等待开始...", snake.getId());
	}

	Snake getFirstWaitSnake() {
		return waitqueue.removeFirst();
	}

	boolean removeWaitSnake(Snake snake) { // TODO 当等待的人多了以后，这里可能会出问题
		return waitqueue.remove(snake);
	}

	public void removeOffLineSnake(AtmosphereResource resource) {
		snakeBroadcaster.removeAtmosphereResource(resource);
		Snake snake = (Snake) resource.session().getAttribute("snake");
		removePlayingSnake(snake.getId());
		removeWaitSnake(snake);

		Integer snakeId = (Integer) resource.session().getAttribute("id");
		snakeBroadcaster.broadcast(String.format("{'type': 'leave', 'id': %d}", snakeId));
	}

	synchronized void addSnake(Snake snake) {
		waitForPlay(snake);
		if (playingSnakesNum() >= MAX_ALIVE_SNAKE) {
			snake.sendMessage(String.format("{'type': 'wait', 'data' : '请稍等,蛇满为患了,您前面还有  %s 条小蛇蛇在焦急等待...'}",
					waitSnakesNum() - 1)); // 因为一来，就进等待队列，所以里面总会多一个。
		} else {
			activeWaitSnake();
		}
	}

	synchronized void removeDeadSnake(Snake snake) {
		int snakeId = snake.getId();
		removePlayingSnake(snakeId);
		snakeBroadcaster.broadcast(String.format("{'type': 'dead', 'id': %d}", snakeId));
		addSnake(snake);
		activeWaitSnake();
	}

	private void activeWaitSnake() {
		if (waitSnakesNum() > 0) {
			Snake firstWait = getFirstWaitSnake();
			firstWait.startPlay();
			addNewPlayingSnake(firstWait);
			broadcastPlayingSnakeInfo();
		}
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
		snakeBroadcaster.broadcast(String.format("{'type': 'join','data':[%s]}", sb.toString()));
	}
}
