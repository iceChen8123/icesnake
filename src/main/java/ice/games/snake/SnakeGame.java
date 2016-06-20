package ice.games.snake;

import ice.games.snake.adjudgement.Adjudicator;
import ice.games.snake.adjudgement.Rule1;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeGame {

	private static final AtomicInteger snakeIds = new AtomicInteger(0);

	private static final ConcurrentHashMap<Integer, Snake> snakes = new ConcurrentHashMap<Integer, Snake>();

	private final SnakeBroadcaster snakeBroadcaster;

	private static Adjudicator adjudicator = new Rule1();

	private final static Logger logger = LoggerFactory.getLogger(SnakeGame.class);

	static {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (Iterator<Snake> iterator = getSnakes().iterator(); iterator.hasNext();) {
					Snake snake = iterator.next();
					snake.update(adjudicator);
				}
			}
		}).start();
		logger.info("游戏跑起来....");
	}

	public SnakeGame() {
		snakeBroadcaster = new SnakeBroadcaster(BroadcasterFactory.getDefault().lookup("/snake", true));
	}

	void onOpen(AtmosphereResource resource) throws IOException {
		int id = snakeIds.getAndIncrement();
		resource.session().setAttribute("id", id);
		Snake snake = new Snake(id, resource);
		resource.session().setAttribute("snake", snake);
		logger.info(id + " 上线了......");
		addSnake(snake);
	}

	void onMessage(AtmosphereResource resource, String message) {
		Snake snake = (Snake) resource.session().getAttribute("snake");
		if ("left".equals(message)) {
			snake.setDirection(Direction.LEFT);
		} else if ("up".equals(message)) {
			snake.setDirection(Direction.UP);
		} else if ("right".equals(message)) {
			snake.setDirection(Direction.RIGHT);
		} else if ("down".equals(message)) {
			snake.setDirection(Direction.DOWN);
		}
	}

	synchronized void onClose(AtmosphereResource resource) {
		Integer id = Integer.parseInt(resource.session().getAttribute("id").toString());
		logger.info(id + " 下线了......");
		snakes.remove(id);
		snakeBroadcaster.broadcast(String.format("{'type': 'leave', 'id': %d}", id));
	}

	private synchronized void addSnake(Snake snake) {
		StringBuilder sb = new StringBuilder();
		snakes.put(Integer.valueOf(snake.getId()), snake);
		for (Iterator<Snake> iterator = getSnakes().iterator(); iterator.hasNext();) {
			// 新添加一条时,需要将所有的广播一遍,其实只需要向新增加的广播所有，而像其他广播新增一条,但是这样写，比较容易。因为页面上直接用id去找蛇的，所以如果新增后，不广播所有，那么新增的蛇，会看不到其他的蛇。
			snake = iterator.next();
			sb.append(String.format("{id: %d, color: '%s'}", Integer.valueOf(snake.getId()), snake.getHexColor()));
			if (iterator.hasNext()) {
				sb.append(',');
			}
		}
		snakeBroadcaster.broadcast(String.format("{'type': 'join','data':[%s]}", sb.toString()));
	}

	public static Collection<Snake> getSnakes() {
		return Collections.unmodifiableCollection(snakes.values());
	}

}
