package ice.games.snake.adjudgement;

import ice.games.snake.Snake;

import java.util.Collection;

public class Rule1 implements Adjudicator {

	@Override
	public void judge(Snake me, Collection<Snake> allSnakes) {
		int id = me.getId();
		for (Snake snake : allSnakes) {
			boolean headCollision = id != snake.getId() && snake.getHead().equals(me.getHead());
			boolean tailCollision = snake.getTail().contains(me.getHead());
			if (headCollision || tailCollision) {
				if (id != snake.getId()) {
					me.kill();
					snake.reward();
				} else {
					me.suicide();
				}
			}
		}
	}

}
