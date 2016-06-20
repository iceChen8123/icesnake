package ice.games.snake.adjudgement;

import ice.games.snake.Snake;

import java.util.Collection;

public interface Adjudicator {

	public void judge(Snake me, Collection<Snake> allSnakes);

}
