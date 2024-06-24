package code.world.fixed.dividers;

import mki.math.vector.Vector3;
import mki.world.Material;
import mki.world.Model;
import mki.world.RigidBody;
import code.core.Core;
import code.world.Tile;
import code.world.fixed.Direction;

/**
* Walls and stuff
*/
public class WallWindow extends Wall {
  private static final Model modelNS = Model.generateMesh("models/wall_window.obj");
  private static final Model modelEW = Model.generateMesh("models/wall_window.obj");

  static {
    WallWindow.modelEW.setRadius(WallWindow.modelNS.calculateRadius());
    Material mat = new Material(Core.FULL_BRIGHT);
    WallWindow.modelNS.setMat(mat);
    WallWindow.modelEW.setMat(mat);

    RigidBody.removeBody(new RigidBody(new Vector3(), modelEW) {
      {setYaw(90);}
    });
  }
  
  /**
  * Constructor for Wall objects
  */
  public WallWindow(Tile tile, Direction direction) {
    super(tile, direction, modelNS, modelEW);
  }
}
