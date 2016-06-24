package ice.games.snake;

import ice.games.snake.base.Direction;
import ice.games.snake.base.Location;
import ice.games.snake.base.Settings;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Random;

import org.atmosphere.cpr.AtmosphereResource;

public class Snake {

	public enum SnakeStatus {
		wait, start, dead;
	}

	private static final Random randomForPosition = new Random();
	private static final Random randomForColor = new Random();

	private final int id;
	private int length = Settings.DEFAULT_SNAKE_LENGTH;
	private final Deque<Location> tail = new ArrayDeque<Location>();
	private SnakeStatus status;
	private Direction direction;
	private Location head;
	private final String hexColor;
	private String headColor;

	private final AtmosphereResource resource;

	public Snake(int id, AtmosphereResource resource) {
		this.id = id;
		this.hexColor = getRandomHexColor();
		this.headColor = genHeadColor();
		this.resource = resource;
		resetState();
		this.status = SnakeStatus.wait;
	}

	public int getId() {
		return id;
	}

	public String getHexColor() {
		return hexColor;
	}

	public SnakeStatus getStatus() {
		return status;
	}

	public synchronized void setDirection(Direction direction) {
		if (status != SnakeStatus.wait) {
			this.direction = direction;
		}
	}

	protected synchronized String getLocationsJson() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("{x: %d, y: %d}", Integer.valueOf(head.x), Integer.valueOf(head.y)));
		for (Location location : tail) {
			sb.append(',');
			sb.append(String.format("{x: %d, y: %d}", Integer.valueOf(location.x), Integer.valueOf(location.y)));
		}
		return String.format("{'id':%d,'body':[%s]}", Integer.valueOf(id), sb.toString());
	}

	protected void startPlay() {
		status = SnakeStatus.start;
	}

	protected void sendMessage(String msg) {
		resource.getResponse().write(msg);
	}

	protected synchronized void update(Collection<Snake> snakes) {
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

	private synchronized Location getHead() {
		return head;
	}

	private synchronized Collection<Location> getTail() {
		return tail;
	}

	private static String genHeadColor() {
		String[] colors = new String[] { "OrangeRed", "DarkOrange", "Lime", "Aqua", "DodgerBlue", "Fuchsia" };
		return colors[randomForColor.nextInt(colors.length)];
	}

	private static String getRandomHexColor() {
		float hue = randomForColor.nextFloat();
		// sat between 0.1 and 0.3
		float saturation = (randomForColor.nextInt(2000) + 1000) / 10000f;
		float luminance = 0.9f;
		Color color = Color.getHSBColor(hue, saturation, luminance);
		return '#' + Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
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

	private synchronized void suicide() {
		status = SnakeStatus.dead;
		resetState();
		sendMessage(Settings.MESSAGE_SUICIDE);

	}

	private synchronized void dead() {
		status = SnakeStatus.dead;
		resetState();
	}

	private synchronized void reward() {
		length++;
		sendMessage(Settings.MESSAGE_KILL);
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

	public String getHeadColor() {
		return headColor;
	}

	public void setHeadcolor(String string) {
		headColor = string;
	}

}
