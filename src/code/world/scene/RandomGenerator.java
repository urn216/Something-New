package code.world.scene;

import code.world.fixed.Direction;

public abstract class RandomGenerator {
  public abstract Scene generate();

  protected final boolean removeDivider(Scene s, int x, int y, Direction d) {
    return false;
  }
}
