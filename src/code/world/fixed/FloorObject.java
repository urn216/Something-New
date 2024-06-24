package code.world.fixed;

import java.awt.Graphics2D;

import code.world.Tile;

public abstract class FloorObject extends WorldObject {
  
  public FloorObject(Tile tile) {
    super(tile);
  }

  @Override
  public int getShape() {
    return 1<<(Tile.OFFSET_FLOOR+Tile.OBJECTS_FLOOR/2);
  }

  @Override
  public abstract void draw2D(Graphics2D g);
}
