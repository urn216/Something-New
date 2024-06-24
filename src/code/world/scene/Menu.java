package code.world.scene;

import java.awt.Graphics2D;
import java.util.List;

import code.world.Tile;
import code.world.fixed.Decal;

public class Menu extends Scene {

  static final Menu MENU = new Menu();

  private Menu() {
    super(
      new Tile[0][0],
      List.of(),
      List.of(),
      List.of(new Decal(1920, 1080, "BG/Space.png", false, MENU)),
      List.of()
    );
  }

  @Override
  public void reset() {}

  @Override
  public void draw(Graphics2D g) {
    for (Decal d : bgDecals) {
      d.draw2D(g);
    }
  }
}
