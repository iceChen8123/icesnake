package ice.games.snake;

import org.atmosphere.cpr.AtmosphereResource;

public class OncloseProccesor {
	protected final SnakeBroadcaster snakeBroadcaster;

	public OncloseProccesor(SnakeBroadcaster snakeBroadcaster) {
		super();
		this.snakeBroadcaster = snakeBroadcaster;
	}

	public void onClose(AtmosphereResource resource) {
		snakeBroadcaster.removeOffLineSnake(resource);
	}
}
