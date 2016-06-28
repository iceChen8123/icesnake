package ice.games.snake;

import ice.games.snake.base.Direction;
import ice.games.snake.base.Location;
import ice.games.snake.base.Settings;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import org.atmosphere.cpr.AtmosphereResource;

public class Snake {

	@Override
	public String toString() {
		return "Snake [id=" + id + "]";
	}

	public enum SnakeStatus {
		wait, start, dead;
	}

	private final int id;
	private SnakeStatus status;
	private String headColor;
	private final String bodyColor;
	private Location head;
	private Direction direction;
	private int length = Settings.DEFAULT_SNAKE_LENGTH;
	private final Deque<Location> tail = new ArrayDeque<Location>();

	private final AtmosphereResource resource;

	public Snake(int id, AtmosphereResource resource) {
		this.id = id;
		this.bodyColor = ColorGenerator.getRandomHexColor();
		this.headColor = ColorGenerator.getRandomHeadColor();
		this.resource = resource;
		resetState();
		this.status = SnakeStatus.wait;
	}

	public synchronized void setDirection(Direction direction) {
		if (status != SnakeStatus.wait) {
			this.direction = direction;
		}
	}

	int getId() {
		return id;
	}

	String getBodyColor() {
		return bodyColor;
	}

	String getHeadColor() {
		return headColor;
	}

	void setBoss() {
		headColor = "white";
	}

	boolean isDead() {
		return status == SnakeStatus.dead;
	}

	synchronized String getLocationsJson() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("{x: %d, y: %d}", Integer.valueOf(head.x), Integer.valueOf(head.y)));
		for (Location location : tail) {
			sb.append(',');
			sb.append(String.format("{x: %d, y: %d}", Integer.valueOf(location.x), Integer.valueOf(location.y)));
		}
		return String.format("{'id':%d,'body':[%s]}", Integer.valueOf(id), sb.toString());
	}

	void startPlay() {
		status = SnakeStatus.start;
		sendMessage(String.format("{'type': 'info', 'data' : '%s'}", id));
	}

	void sendMessage(String msg) {
		resource.getResponse().write(msg);
	}

	synchronized void update(Collection<Snake> snakes) {
		Location nextLocation = head.getAdjacentLocation(direction);
		if (nextLocation.x >= Settings.PLAYFIELD_WIDTH) {
			nextLocation.x = 0;
		}
		if (nextLocation.y >= Settings.PLAYFIELD_HEIGHT) {
			nextLocation.y = 0;
		}
		if (nextLocation.x < 0) {
			nextLocation.x = Settings.PLAYFIELD_WIDTH;
		}
		if (nextLocation.y < 0) {
			nextLocation.y = Settings.PLAYFIELD_HEIGHT;
		}
		if (direction != Direction.NONE) {
			tail.addFirst(head);
			if (tail.size() > length) {
				tail.removeLast();
			}
			head = nextLocation;
		}

		handleCollisions(snakes);
	}

	private void resetState() {
		this.direction = Direction.NONE;
		this.head = PositionGenerator.getRandomLocation();
		this.tail.clear();
		this.length = Settings.DEFAULT_SNAKE_LENGTH;
	}

	private synchronized void suicide() {
		status = SnakeStatus.dead;
		resetState();
		sendMessage(Settings.MESSAGE_SUICIDE);

	}

	private void handleCollisions(Collection<Snake> snakes) {
		for (Snake snake : snakes) {
			if (id != snake.id) {
				boolean headCollision = snake.head.equals(head);
				boolean tailCollision = snake.tail.contains(head);
				if (headCollision || tailCollision) {
					dead();
					snake.reward();
				}
			} else {
				boolean tailCollision = snake.tail.contains(head);
				if (tailCollision) {
					suicide();
				}
			}
		}
	}

	private void dead() {
		status = SnakeStatus.dead;
		resetState();
	}

	private void reward() {
		length++;
		sendMessage(Settings.MESSAGE_KILL);
	}

}
