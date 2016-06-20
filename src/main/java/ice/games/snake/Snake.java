package ice.games.snake;

import ice.games.snake.adjudgement.Adjudicator;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Random;

import org.atmosphere.cpr.AtmosphereResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Snake {

	private final static Logger logger = LoggerFactory.getLogger(Snake.class);

	private static final Random randomForPosition = new Random();

	private static final Random randomForColor = new Random();

	private final AtmosphereResource resource;

	private final int id;
	private Direction direction;
	private final String hexColor;
	private Location head;
	private final Deque<Location> tail = new ArrayDeque<Location>();
	private int length = Settings.DEFAULT_SNAKE_LENGTH;

	public Snake(int id, AtmosphereResource resource) {
		this.id = id;
		this.hexColor = getRandomHexColor();
		this.resource = resource;
		resetState();
	}

	private static String getRandomHexColor() {
		float hue = randomForColor.nextFloat();
		// sat between 0.1 and 0.3
		float saturation = (randomForColor.nextInt(2000) + 1000) / 10000f;
		float luminance = 0.9f;
		Color color = Color.getHSBColor(hue, saturation, luminance);
		return '#' + Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
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

	public synchronized void update(Adjudicator adjudicator) {
		moveOneStep();
		handleCollisions(adjudicator);
	}

	private void moveOneStep() {
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
	}

	private void handleCollisions(Adjudicator adjudicator) {
		try {
			adjudicator.judge(this, SnakeGame.getSnakes());
		} catch (Exception e) {
			logger.error("handleCollisions: ", e);
		}
	}

	public synchronized void suicide() {
		resetState();
		sendMessage(Settings.MESSAGE_SUICIDE);

	}

	public synchronized void kill() {
		resetState();
		sendMessage(Settings.MESSAGE_DEAD);
	}

	public synchronized void reward() {
		length++;
		sendMessage(Settings.MESSAGE_KILL);
	}

	private void sendMessage(String msg) {
		resource.getResponse().write(msg);
	}

}
