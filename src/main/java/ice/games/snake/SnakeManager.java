package ice.games.snake;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class SnakeManager {

	private final ConcurrentHashMap<Integer, Snake> playingSnakes = new ConcurrentHashMap<Integer, Snake>();

	private final LinkedList<Snake> waitqueue = new LinkedList<Snake>();

	Collection<Snake> getPlayingSnakes() {
		return Collections.unmodifiableCollection(playingSnakes.values());
	}

	void addNewPlayingSnake(Snake firstWait) {
		playingSnakes.put(Integer.valueOf(firstWait.getId()), firstWait);
	}

	int playingSnakesNum() {
		return playingSnakes.size();
	}

	void removePlayingSnake(int snakeId) {
		playingSnakes.remove(snakeId);
	}

	int waitSnakesNum() {
		return waitqueue.size();
	}

	void waitForPlay(Snake snake) {
		waitqueue.add(snake);
	}

	Snake getFirstWaitSnake() {
		return waitqueue.removeFirst();
	}

	boolean removeWaitSnake(Snake snake) {
		return waitqueue.remove(snake);
	}
}
