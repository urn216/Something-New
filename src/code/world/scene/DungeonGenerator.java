package code.world.scene;

import code.world.Tile;

import code.world.fixed.dividers.Door;
import code.world.fixed.Decal;
import code.world.fixed.Direction;
import code.world.fixed.Light;
import code.world.fixed.dividers.Wall;
import code.world.fixed.WorldObject;

import code.world.unit.Player;
import code.world.unit.TestAI;
import code.world.unit.Unit;

import mki.math.vector.Vector2;

import java.util.*;

/**
* class for generating a random map for playing the game
*/
public class DungeonGenerator implements RandomGenerator {

  private static final byte[] WHICH_DIR = {1, 0, 0, 1, -1, 0, 0, -1};

  private static final int DEFAULT_MAP_SIZE = 50;

  private static final float FAIL_CHANCE_INCREMENT = 0.01f;
  private static final float INITIAL_FAILURE_CHANCE = 0.0f;
  private static final float ROOM_CONNECTION_CHANCE = 0.25f;
  private static final float TEST_AI_SPAWN_CHANCE = 0.1f;
  private static final float BROKEN_LIGHT_CHANCE = 0.125f;

  private static final int LIGHT_SPACING = 3;
  private static final int DIRECTION_CHANCES = 4;

  private final long seed;
  private final int width;
  private final int height;

  private float failChance = INITIAL_FAILURE_CHANCE;

  public DungeonGenerator(long seed, int width, int height) {
    this.seed = seed;
    this.width = width;
    this.height = height;
  }

  public DungeonGenerator() {
    this(System.nanoTime(), DEFAULT_MAP_SIZE, DEFAULT_MAP_SIZE);
  }

  public Scene generate() {
    Tile[][] map = new Tile[this.width][this.height];
    Set<WorldObject> fixedObj = new HashSet<WorldObject>();
    Set<Unit> units = new HashSet<Unit>();
    Set<Decal> bgDecals = new HashSet<Decal>();
    Set<Decal> fgDecals = new HashSet<Decal>();

    Scene result = new Scene(map, fixedObj, units, bgDecals, fgDecals);
    bgDecals.add(new Decal(1920, 1080, "BG/Space.png", false, result));
    // fgDecals.add(new Decal(Tile.TILE_SIZE/2, Tile.TILE_SIZE/2, "decal/test.png", true, result));

    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        map[x][y] = new Tile((int) (x-this.width/2), (int) (y-this.height/2), 0);
      }
    }

    this.failChance = INITIAL_FAILURE_CHANCE;

    System.out.println("Starting...");
    startingPad(result, new Random(seed), map, fixedObj, units);
    result.bigUpdate();
    return result;
  }

  private void startingPad(Scene result, Random rand, Tile[][] map, Set<WorldObject> fixedObj, Set<Unit> units) {

    for (int i = -2; i <= 2; i++) {
      for (int j = -2; j <= 2; j++) {
        if (Math.abs(i)+Math.abs(j)==4) {continue;}
        map[i+this.width/2][j+this.height/2].activate();
        if (Math.abs(j) == 2) {
          fixedObj.add(new Wall(i, j==2?3:-2, Direction.North, result));
          fixedObj.add(new Wall(j==2?3:-2, i, Direction.West, result));
          if (Math.abs(i) == 1) {
            fixedObj.add(new Wall(i==1?2:-1, j, Direction.West, result));
            fixedObj.add(new Wall(j, i==1?2:-1, Direction.North, result));
          }
        }
      }
    }

    fixedObj.add(new Light(0, 0, (rand.nextFloat() > BROKEN_LIGHT_CHANCE), result));

    result.player = new Player(result, null, new Vector2(Tile.TILE_SIZE_U/2, Tile.TILE_SIZE_U/2), new Vector2(), 0, 0, 160);
    result.cam.setTarU(result.player);
    units.add(result.player);

    for (int i = 0; i < 4; i++) {
      failChance *= 0.5f;
      System.out.println("Attempting new chain...");
      int newDir = rand.nextInt(4);
      populate(result, rand, map, width/2+WHICH_DIR[newDir*2]*3, height/2+WHICH_DIR[newDir*2+1]*3, newDir, fixedObj, units);
    }
  }

  private void populate(Scene result, Random rand, Tile[][] map, int x, int y, int dir, Set<WorldObject> fixedObj, Set<Unit> units) {
    int width = map.length;
    int height = map[0].length;
    if (rand.nextFloat() < failChance) {System.out.println("Failed by chance"); return;}
    else {failChance+=FAIL_CHANCE_INCREMENT;}
    int rWidth = rand.nextInt(4)*2+1;
    int rHeight = rand.nextInt(4)*2+1;
    for (int i = -(rWidth/2); i <= rWidth/2; i++) {
      int cX = x+i+WHICH_DIR[dir*2]*rWidth/2;
      for (int j = -(rHeight/2); j <= rHeight/2; j++) {
        int cY = y+j+WHICH_DIR[dir*2+1]*rHeight/2;
        if (cX<0 || cX>=width-1 || cY<0 || cY>=height-1) {System.out.println("Failed by out of bounds"); return;}
        if (map[cX][cY].isActive()) {System.out.println("Failed by collision"); return;}
      }
    }
    boolean connect = false;
    if (rand.nextFloat() < ROOM_CONNECTION_CHANCE && rWidth > 0 && rHeight > 0) {connect = true;}
    for (int i = -(rWidth/2); i <= rWidth/2; i++) {
      int cX = x+i+WHICH_DIR[dir*2]*rWidth/2;
      for (int j = -(rHeight/2); j <= rHeight/2; j++) {
        int cY = y+j+WHICH_DIR[dir*2+1]*rHeight/2;
        map[cX][cY].activate();
        // chance for doors to be added
        if (rand.nextFloat() < TEST_AI_SPAWN_CHANCE) {units.add(new TestAI(result, null, new Vector2((cX-width/2)*Tile.TILE_SIZE_U+Tile.TILE_SIZE_U/2, (cY-height/2)*Tile.TILE_SIZE_U+Tile.TILE_SIZE_U/2), new Vector2()));}
        if (i%LIGHT_SPACING==0 && j%LIGHT_SPACING==0) {fixedObj.add(new Light(cX-width/2, cY-height/2, (rand.nextFloat() > BROKEN_LIGHT_CHANCE), result));}
        if (i == -(rWidth/2)) {if (!fixedObj.add(new Wall(cX-width/2, cY-height/2, Direction.West, result)) && connect) {fixedObj.remove(new Wall(cX-width/2, cY-height/2, Direction.West, result));}}
        if (i == rWidth/2) {if (!fixedObj.add(new Wall(cX-width/2+1, cY-height/2, Direction.West, result)) && connect) {fixedObj.remove(new Wall(cX-width/2+1, cY-height/2, Direction.West, result));}}
        if (j == -(rHeight/2)) {if (!fixedObj.add(new Wall(cX-width/2, cY-height/2, Direction.North, result)) && connect) {fixedObj.remove(new Wall(cX-width/2, cY-height/2, Direction.North, result));}}
        if (j == rHeight/2) {if (!fixedObj.add(new Wall(cX-width/2, cY-height/2+1, Direction.North, result)) && connect) {fixedObj.remove(new Wall(cX-width/2, cY-height/2+1, Direction.North, result));}}
      }
    }
    switch(dir) {
      case 0:
      if (fixedObj.remove(new Wall(x-width/2, y-height/2, Direction.West, result))) {fixedObj.add(new Door(x-width/2, y-height/2, Direction.West, result));}
      break;
      case 1:
      if (fixedObj.remove(new Wall(x-width/2, y-height/2, Direction.North, result))) {fixedObj.add(new Door(x-width/2, y-height/2, Direction.North, result));}
      break;
      case 2:
      if (fixedObj.remove(new Wall(x-width/2+1, y-height/2, Direction.West, result))) {fixedObj.add(new Door(x-width/2+1, y-height/2, Direction.West, result));}
      break;
      case 3:
      if (fixedObj.remove(new Wall(x-width/2, y-height/2+1, Direction.North, result))) {fixedObj.add(new Door(x-width/2, y-height/2+1, Direction.North, result));}
      break;
    }

    System.out.println("Success!");

    for (int i = 0; i < DIRECTION_CHANCES; i++) {
      System.out.println("Attempting...");
      int newDir = rand.nextInt(4);
      int LCR = rand.nextInt(3)-1;
      populate(result, rand, map, x+WHICH_DIR[newDir*2]*(rWidth/2+1)+WHICH_DIR[dir*2]*(rWidth/2)+WHICH_DIR[newDir*2+1]*(rWidth/2)*LCR,
      y+WHICH_DIR[newDir*2+1]*(rHeight/2+1)+WHICH_DIR[dir*2+1]*(rHeight/2)+WHICH_DIR[newDir*2]*(rHeight/2)*LCR,
      newDir, fixedObj, units);
    }
  }
}
