package ice.games.snake;

import ice.games.snake.Snake.SnakeStatus;
import ice.games.snake.base.SnakeBroadcaster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeManager implements Callable<String> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ConcurrentHashMap<Integer, Snake> playingSnakes = new ConcurrentHashMap<Integer, Snake>();

	private final LinkedList<Snake> waitqueue = new LinkedList<Snake>();

	private final int MAX_ALIVE_SNAKE = 2;

	private SnakeBroadcaster snakeBroadcaster;

	private Snake boss;

	private List<Snake> partnerList = new ArrayList<Snake>();

	public SnakeManager() {
		this.snakeBroadcaster = new SnakeBroadcaster(BroadcasterFactory.getDefault().lookup("/snake", true), this);
	}

	public synchronized void addNewPlaySnake(Snake snake) {
		snake.sendMessage(getPlayingSnakeInfo());
		waitqueue.add(snake);
		logger.info("蛇 {} 进入游戏,开始等待...", snake.getId());
		afterJoinWaitQueue(snake);
	}

	public void removeOffLineSnake(AtmosphereResource resource) {
		snakeBroadcaster.removeOffLineSnake(resource);
		Snake snake = (Snake) resource.session().getAttribute("snake");
		playingSnakes.remove(snake.getId());
		logger.info("蛇 {} 退出游戏...", snake.getId());
		waitqueue.remove(snake);// TODO 当等待的人多了以后，这里可能会出问题

		takeoffRole(snake);

		Integer snakeId = (Integer) resource.session().getAttribute("id");
		snakeBroadcaster.broadcast(String.format("{'type': 'leave', 'id': %d}", snakeId));
	}

	private Collection<Snake> getPlayingSnakes() {
		return Collections.unmodifiableCollection(playingSnakes.values());
	}

	private void activeWaitSnake() {
		if (waitqueue.size() > 0) {
			Snake firstWait = waitqueue.removeFirst();
			firstWait.startPlay();
			firstWait.sendMessage(String.format("{'type': 'info', 'data' : '%s'}", firstWait.getId()));
			setRole(firstWait);
			playingSnakes.put(Integer.valueOf(firstWait.getId()), firstWait);
			logger.info("蛇 {} 开始游戏...", firstWait.getId());
			snakeBroadcaster.broadcast(getPlayingSnakeInfo());
		}
	}

	private String getPlayingSnakeInfo() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Snake> iterator = getPlayingSnakes().iterator(); iterator.hasNext();) {
			Snake snaketemp = iterator.next();
			sb.append(String.format("{id: %d, color: '%s', headcolor: '%s'}", Integer.valueOf(snaketemp.getId()),
					snaketemp.getHexColor(), snaketemp.getHeadColor()));
			if (iterator.hasNext()) {
				sb.append(',');
			}
		}
		return String.format("{'type': 'playinginfo','data':[%s]}", sb.toString());
	}

	@Override
	public String call() {
		try {
			StringBuilder sb = new StringBuilder();
			for (Iterator<Snake> iterator = getPlayingSnakes().iterator(); iterator.hasNext();) {
				Snake snake = iterator.next();
				snake.update(getPlayingSnakes()); // TODO
													// 这边需要注意下，在一个tick内，是否要多次获取所有蛇???
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
		} catch (RuntimeException e) {
			logger.error("Caught to prevent timer from shutting down", e);
		}
		return "";
	}

	private synchronized void removeDeadSnake(Snake snake) {
		int snakeId = snake.getId();
		playingSnakes.remove(snakeId);
		takeoffRole(snake);
		logger.info("蛇 {} 死了,移出游戏队列...", snakeId);
		snakeBroadcaster.broadcast(String.format("{'type': 'dead', 'id': %d}", snakeId));
		rePlaySnake(snake);
	}

	private void setRole(Snake firstWait) {
		if (boss == null) {
			firstWait.setHeadcolor("white");
			boss = firstWait;
		} else {
			partnerList.add(firstWait);
		}
	}

	private void takeoffRole(Snake snake) {
		if (boss == snake) {
			boss = null;
		} else {
			partnerList.remove(snake);
		}
	}

	private synchronized void rePlaySnake(Snake snake) {
		waitqueue.add(snake);
		logger.info("蛇 {} 重新进入等待...", snake.getId());
		afterJoinWaitQueue(snake);
	}

	private void afterJoinWaitQueue(Snake snake) {
		if (playingSnakes.size() >= MAX_ALIVE_SNAKE) {
			snake.sendMessage(String.format("{'type': 'wait', 'data' : '请稍等,蛇满为患了,您前面还有  %s 条小蛇蛇在焦急等待...'}",
					waitqueue.size() - 1)); // 因为一来，就进等待队列，所以里面总会多一个。
		} else {
			activeWaitSnake();
		}
	}

}
