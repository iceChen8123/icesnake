package ice.games.snake;

import java.awt.Color;
import java.util.Random;

class ColorGenerator {

	private static final Random randomForColor = new Random();

	static String getRandomHeadColor() {
		String[] colors = new String[] { "OrangeRed", "DarkOrange", "Lime", "Aqua", "DodgerBlue", "Fuchsia" };
		return colors[randomForColor.nextInt(colors.length)];
	}

	static String getRandomHexColor() {
		float hue = randomForColor.nextFloat();
		// sat between 0.1 and 0.3
		float saturation = (randomForColor.nextInt(2000) + 1000) / 10000f;
		float luminance = 0.9f;
		Color color = Color.getHSBColor(hue, saturation, luminance);
		return '#' + Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
	}

}
