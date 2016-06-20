/*
 * Copyright 2012 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * Copyright 2012 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ice.games.snake;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterFactory;

public class SnakeGame {

	protected static final AtomicInteger snakeIds = new AtomicInteger(0);

	private final ConcurrentHashMap<Integer, Snake> snakes = new ConcurrentHashMap<Integer, Snake>();

	protected final SnakeBroadcaster snakeBroadcaster;

	public SnakeGame() {
		snakeBroadcaster = new SnakeBroadcaster(BroadcasterFactory.getDefault().lookup("/snake", true), this);
	}

	public Collection<Snake> getSnakes() {
		return Collections.unmodifiableCollection(snakes.values());
	}

	public void onOpen(AtmosphereResource resource) throws IOException {
		int id = snakeIds.getAndIncrement();
		resource.session().setAttribute("id", id);
		Snake snake = new Snake(id, resource);
		resource.session().setAttribute("snake", snake);

		addSnake(snake);
	}

	private synchronized void addSnake(Snake snake) {
		snakes.put(Integer.valueOf(snake.getId()), snake);

		StringBuilder sb = new StringBuilder();
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

	public void onClose(AtmosphereResource resource) {
		Integer id = Integer.parseInt(resource.session().getAttribute("id").toString());
		snakes.remove(id);
		snakeBroadcaster.broadcast(String.format("{'type': 'leave', 'id': %d}", id));
	}

	public String getSnakeInfo() {
		if (getSnakes() == null || getSnakes().isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Iterator<Snake> iterator = getSnakes().iterator(); iterator.hasNext();) {
			Snake snake = iterator.next();
			snake.update(getSnakes());
			sb.append(snake.getLocationsJson());
			if (iterator.hasNext()) {
				sb.append(',');
			}
		}
		return sb.toString();
	}

	private Snake snake(AtmosphereResource resource) {
		return (Snake) resource.session().getAttribute("snake");
	}

	protected void onMessage(AtmosphereResource resource, String message) {
		Snake snake = snake(resource);
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
}
