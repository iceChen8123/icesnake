package ice.games.snake;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Random;

import org.atmosphere.cpr.AtmosphereResource;

public class Snake {

	private final int id;
	private final AtmosphereResource resource;

	private Direction direction;
	private int length = Settings.DEFAULT_SNAKE_LENGTH;
	private Location head;
	private final Deque<Location> tail = new ArrayDeque<Location>();
	private final String hexColor;

	private static final Random randomForPosition = new Random();
	private static final Random randomForColor = new Random();

	public Snake(int id, AtmosphereResource resource) {
		this.id = id;
		this.hexColor = getRandomHexColor();
		this.resource = resource;
		resetState();
	}

	private void resetState() {
		this.direction = Direction.NONE;
		this.head = getRandomLocation();
		this.tail.clear();
		this.length = Settings.DEFAULT_SNAKE_LENGTH;
	}

	private static String getRandomHexColor() {
		float hue = randomForColor.nextFloat();
		// sat between 0.1 and 0.3
		float saturation = (randomForColor.nextInt(2000) + 1000) / 10000f;
		float luminance = 0.9f;
		Color color = Color.getHSBColor(hue, saturation, luminance);
		return '#' + Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
	}

	private static Location getRandomLocation() {
		int x = roundByGridSize(randomForPosition.nextInt(Settings.PLAYFIELD_WIDTH));
		int y = roundByGridSize(randomForPosition.nextInt(Settings.PLAYFIELD_HEIGHT));
		return new Location(x, y);
	}

	private static int roundByGridSize(int value) {
		value = value + (Settings.GRID_SIZE / 2);
		value = value / Settings.GRID_SIZE;
		value = value * Settings.GRID_SIZE;
		return value;
	}

	private synchronized void suicide() {
		resetState();
		sendMessage(Settings.MESSAGE_SUICIDE);

	}

	private synchronized void dead() {
		resetState();
		sendMessage(Settings.MESSAGE_DEAD);
	}

	private synchronized void reward() {
		length++;
		sendMessage(Settings.MESSAGE_KILL);
	}

	protected void sendMessage(String msg) {
		resource.getResponse().write(msg);
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
			if (id != snake.id) {
				boolean headCollision = snake.getHead().equals(head);
				boolean tailCollision = snake.getTail().contains(head);
				if (headCollision || tailCollision) {
					dead();
					snake.reward();
				}
			} else {
				boolean tailCollision = snake.getTail().contains(head);
				if (tailCollision) {
					suicide();
				}
			}
		}
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

	public int getId() {
		return id;
	}

	public String getHexColor() {
		return hexColor;
	}
}
