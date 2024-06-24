package code.world.fixed;

import java.awt.Graphics2D;

import code.world.Tile;

public abstract class CeilingObject extends WorldObject {
  
  public CeilingObject(Tile tile) {
    super(tile);
  }

  @Override
  public int getShape() {
    return 1<<Tile.OFFSET_CEILING;
  }

  @Override
  public abstract void draw2D(Graphics2D g);
}
