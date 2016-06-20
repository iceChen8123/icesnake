package ice.games.snake;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Random;

import org.atmosphere.cpr.AtmosphereResource;

public class Snake {

	private static final Random random = new Random();

	private final AtmosphereResource resource;

	private final int id;
	private Direction direction;
	private final String hexColor;
	private Location head;
	private final Deque<Location> tail = new ArrayDeque<Location>();
	private int length = Settings.DEFAULT_SNAKE_LENGTH;

	public Snake(int id, AtmosphereResource resource) {
		this.id = id;
		this.hexColor = SnakeWebSocket.getRandomHexColor();
		this.resource = resource;
		resetState();
	}

	public int getId() {
		return id;
	}

	public String getHexColor() {
		return hexColor;
	}

	public synchronized Location getHead() {
		return head;
	}

	public synchronized Collection<Location> getTail() {
		return tail;
	}

	public synchronized void setDirection(Direction direction) {
		this.direction = direction;
	}

	public synchronized String getLocationsJson() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("{x: %d, y: %d}", Integer.valueOf(head.x), Integer.valueOf(head.y)));
		for (Location location : tail) {
			sb.append(',');
			sb.append(String.format("{x: %d, y: %d}", Integer.valueOf(location.x), Integer.valueOf(location.y)));
		}
		return String.format("{'id':%d,'body':[%s]}", Integer.valueOf(id), sb.toString());
	}

	private void resetState() {
		this.direction = Direction.NONE;
		this.head = getRandomLocation();
		this.tail.clear();
		this.length = Settings.DEFAULT_SNAKE_LENGTH;
	}

	private static Location getRandomLocation() {
		int x = roundByGridSize(random.nextInt(Settings.PLAYFIELD_WIDTH));
		int y = roundByGridSize(random.nextInt(Settings.PLAYFIELD_HEIGHT));
		return new Location(x, y);
	}

	private static int roundByGridSize(int value) {
		value = value + (Settings.GRID_SIZE / 2);
		value = value / Settings.GRID_SIZE;
		value = value * Settings.GRID_SIZE;
		return value;
	}

	public synchronized void update(Collection<Snake> snakes) {
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

	private void handleCollisions(Collection<Snake> snakes) {
		for (Snake snake : snakes) {
			boolean headCollision = id != snake.id && snake.getHead().equals(head);
			boolean tailCollision = snake.getTail().contains(head);
			if (headCollision || tailCollision) {
				if (id != snake.id) {
					kill();
					snake.reward();
				} else {
					suicide();
				}
			}
		}
	}

	private void suicide() {
		resetState();
		sendMessage(Settings.MESSAGE_SUICIDE);

	}

	private synchronized void kill() {
		resetState();
		sendMessage(Settings.MESSAGE_DEAD);
	}

	private synchronized void reward() {
		length++;
		sendMessage(Settings.MESSAGE_KILL);
	}

	private void sendMessage(String msg) {
		resource.getResponse().write(msg);
	}

}
