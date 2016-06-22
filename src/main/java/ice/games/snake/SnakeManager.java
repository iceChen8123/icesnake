package ice.games.snake;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeManager {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ConcurrentHashMap<Integer, Snake> playingSnakes = new ConcurrentHashMap<Integer, Snake>();

	private final LinkedList<Snake> waitqueue = new LinkedList<Snake>();

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
}
