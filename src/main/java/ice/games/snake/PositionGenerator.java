package ice.games.snake;

import ice.games.snake.base.Location;
import ice.games.snake.base.Settings;

import java.util.Random;

class PositionGenerator {

	private static final Random randomForPosition = new Random();

	static Location getRandomLocation() {
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
}
