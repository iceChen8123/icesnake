package ice.games.snake;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.atmosphere.config.service.Get;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Post;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.HeaderConfig;

@ManagedService(path = "/snake")
public class SnakeManagedService extends SnakeGame {

	private final ConcurrentLinkedQueue<String> uuids = new ConcurrentLinkedQueue<String>();

	@Get
	public void onOpen(final AtmosphereResource resource) {
		resource.addEventListener(new AtmosphereResourceEventListenerAdapter() {
			@Override
			public void onSuspend(AtmosphereResourceEvent event) {
				try {
					if (!uuids.contains(resource.uuid())) {
						SnakeManagedService.super.onOpen(resource);
						uuids.add(resource.uuid());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onDisconnect(AtmosphereResourceEvent event) {
				AtmosphereRequest request = event.getResource().getRequest();
				String s = request.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
				// if (s != null && s.equalsIgnoreCase(HeaderConfig.DISCONNECT))
				// {
				SnakeManagedService.super.onClose(resource);
				uuids.remove(resource.uuid());
				// }
			}
		});
	}

	@Post
	public void onMessage(AtmosphereResource resource) {
		try {
			// Here we need to find the suspended AtmosphereResource
			MessageListener.onMessage(AtmosphereResourceFactory.getDefault().find(resource.uuid()), resource
					.getRequest().getReader().readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
