package ice.games.snake;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameRule1 {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Snake boss;

	private Map<Integer, Snake> partners = new HashMap<Integer, Snake>();

	public void addRole(Snake firstWait) {
		if (boss == null) {
			firstWait.setBoss();
			boss = firstWait;
			logger.info("新加入了 boss 是 {}", boss.getId());
		} else {
			partners.put(firstWait.getId(), firstWait);
			logger.info("partnerList 新加入 {}, 一共是 {}", new Object[] { firstWait.getId(), partners.toString() });
		}
	}

	public void removeRole(Snake snake) {
		if (boss == snake) {
			logger.info("boss {} 没了....", boss.getId());
			boss = null;
		} else {
			partners.remove(snake.getId());
			logger.info("partnerList 退出了 {}, 一共是 {}", new Object[] { snake.getId(), partners.toString() });
		}
	}
}
