package code.world.fixed;

import java.awt.Color;
import java.awt.Graphics2D;

import code.world.Tile;

public abstract class Divider extends WorldObject {

  public static final double DIVIDER_THICKNESS_U = 18;
  public static final double DIVIDER_COLLIDER_LENGTH_U = Tile.TILE_SIZE_U+Divider.DIVIDER_THICKNESS_U*0.75;

  public static final double DIVIDER_HEIGHT_M = 2.5;

  public static final Color DIVIDER_COLOUR = new Color(50, 50, 50);
  
  public Divider(Tile tile) {
    super(tile);
  }

  @Override
  public int getShape() {
    return 1<<((this.direction.ordinal()/2)+Tile.OFFSET_BORDER);
  }

  @Override
  public abstract void draw2D(Graphics2D g);
}
