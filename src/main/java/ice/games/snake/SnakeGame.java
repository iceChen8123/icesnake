package ice.games.snake;

import java.io.IOException;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.BroadcasterFactory;

public class SnakeGame {

	private final SnakeBroadcaster snakeBroadcaster;
	private OnopenProccesor onopenProccesor;
	private OncloseProccesor oncloseProccesor;

	public SnakeGame() {
		snakeBroadcaster = new SnakeBroadcaster(BroadcasterFactory.getDefault().lookup("/snake", true));
		this.onopenProccesor = new OnopenProccesor(snakeBroadcaster);
		this.oncloseProccesor = new OncloseProccesor(snakeBroadcaster);
	}

	public void onOpen(AtmosphereResource resource) throws IOException {
		onopenProccesor.onOpen(resource);
	}

	public void onClose(AtmosphereResource resource) {
		oncloseProccesor.onClose(resource);
	}

	public void onMessage(AtmosphereResource resource) throws IOException {
		// Here we need to find the suspended AtmosphereResource
		MessageListener.onMessage(AtmosphereResourceFactory.getDefault().find(resource.uuid()), resource.getRequest()
				.getReader().readLine());
	}

}
