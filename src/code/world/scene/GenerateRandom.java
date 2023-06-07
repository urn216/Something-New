package code.world.scene;

import code.world.Tile;

import code.world.fixed.dividers.Door;
import code.world.fixed.Decal;
import code.world.fixed.Light;
import code.world.fixed.dividers.Wall;
import code.world.fixed.WorldObject;

import code.world.unit.Player;
import code.world.unit.TestAI;
import code.world.unit.Unit;

//import java.io.*;
//import java.nio.file.*;

import java.util.*;

/**
* class for generating a random map for playing the game
*/
abstract class GenerateRandom {

  private static final byte[] WHICH_DIR = {1, 0, 0, 1, -1, 0, 0, -1};

  private static final int DEFAULT_MAP_SIZE = 50;
  private static final float FAIL_CHANCE_INCREMENT = 0.01f;

  private static final int DIRECTION_CHANCES = 4;
  private static float failChance = 0.0f;
  private static float connectChance = 0.25f;
  private static float AIChance = 0.1f;
  private static float lightChance = 0.05f;

  public static Scene generate() {
    return generate(System.nanoTime(), DEFAULT_MAP_SIZE, DEFAULT_MAP_SIZE);
  }

  public static Scene generate(long seed, int width, int height) {
    Tile[][] map = new Tile[width][height];
    Set<WorldObject> fixedObj = new HashSet<WorldObject>();
    Set<Unit> units = new HashSet<Unit>();
    Set<Decal> decals = new HashSet<Decal>();

    Scene result = new Scene(map, fixedObj, units, decals);
    decals.add(new Decal(1920, 1080, "BG/Space.png", false, result));

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        map[x][y] = new Tile((int) (x-width/2), (int) (y-height/2), 0);
      }
    }

    System.out.println("Starting...");
    startingPad(result, new Random(seed), map, fixedObj, units);
    result.bigUpdate();
    return result;
  }

  private static void startingPad(Scene result, Random rand, Tile[][] map, Set<WorldObject> fixedObj, Set<Unit> units) {
    int width = map.length;
    int height = map[0].length;
    
    for (int i = -2; i <= 2; i++) {
      for (int j = -2; j <= 2; j++) {
        if (Math.abs(i)+Math.abs(j)==4) {continue;}
        map[i+width/2][j+height/2].activate();
        if (Math.abs(j) == 2) {
          fixedObj.add(new Wall(i, j==2?3:-2, "Up", "Wall", result));
          fixedObj.add(new Wall(j==2?3:-2, i, "Left", "Wall", result));
          if (Math.abs(i) == 1) {
            fixedObj.add(new Wall(i==1?2:-1, j, "Left", "Wall", result));
            fixedObj.add(new Wall(j, i==1?2:-1, "Up", "Wall", result));
          }
        }
      }
    }
    result.player = new Player(Tile.TILE_SIZE/2, Tile.TILE_SIZE/2, result);
    result.cam.setTarU(result.player);
    units.add(result.player);
    for (int i = 0; i < 4; i++) {
      failChance *= 0.5f;
      System.out.println("Attempting new chain...");
      int newDir = rand.nextInt(4);
      populate(result, rand, map, width/2+WHICH_DIR[newDir*2]*3, height/2+WHICH_DIR[newDir*2+1]*3, newDir, fixedObj, units);
    }
  }

  private static void populate(Scene result, Random rand, Tile[][] map, int x, int y, int dir, Set<WorldObject> fixedObj, Set<Unit> units) {
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
    if (rand.nextFloat() < connectChance && rWidth > 0 && rHeight > 0) {connect = true;}
    for (int i = -(rWidth/2); i <= rWidth/2; i++) {
      int cX = x+i+WHICH_DIR[dir*2]*rWidth/2;
      for (int j = -(rHeight/2); j <= rHeight/2; j++) {
        int cY = y+j+WHICH_DIR[dir*2+1]*rHeight/2;
        map[cX][cY].activate();
        // chance for doors to be added
        if (rand.nextFloat() < AIChance) {units.add(new TestAI((cX-width/2)*Tile.TILE_SIZE+Tile.TILE_SIZE/2, (cY-height/2)*Tile.TILE_SIZE+Tile.TILE_SIZE/2, result));}
        if (rand.nextFloat() < lightChance) {fixedObj.add(new Light(cX-width/2, cY-height/2, "Static", result));}
        if (i == -(rWidth/2)) {if (!fixedObj.add(new Wall(cX-width/2, cY-height/2, "Left", "Wall", result)) && connect) {fixedObj.remove(new Wall(cX-width/2, cY-height/2, "Left", "Wall", result));}}
        if (i == rWidth/2) {if (!fixedObj.add(new Wall(cX-width/2+1, cY-height/2, "Left", "Wall", result)) && connect) {fixedObj.remove(new Wall(cX-width/2+1, cY-height/2, "Left", "Wall", result));}}
        if (j == -(rHeight/2)) {if (!fixedObj.add(new Wall(cX-width/2, cY-height/2, "Up", "Wall", result)) && connect) {fixedObj.remove(new Wall(cX-width/2, cY-height/2, "Up", "Wall", result));}}
        if (j == rHeight/2) {if (!fixedObj.add(new Wall(cX-width/2, cY-height/2+1, "Up", "Wall", result)) && connect) {fixedObj.remove(new Wall(cX-width/2, cY-height/2+1, "Up", "Wall", result));}}
      }
    }
    switch(dir) {
      case 0:
      if (fixedObj.remove(new Wall(x-width/2, y-height/2, "Left", "Wall", result))) {fixedObj.add(new Door(x-width/2, y-height/2, "Left", "Door", result));}
      break;
      case 1:
      if (fixedObj.remove(new Wall(x-width/2, y-height/2, "Up", "Wall", result))) {fixedObj.add(new Door(x-width/2, y-height/2, "Up", "Door", result));}
      break;
      case 2:
      if (fixedObj.remove(new Wall(x-width/2+1, y-height/2, "Left", "Wall", result))) {fixedObj.add(new Door(x-width/2+1, y-height/2, "Left", "Door", result));}
      break;
      case 3:
      if (fixedObj.remove(new Wall(x-width/2, y-height/2+1, "Up", "Wall", result))) {fixedObj.add(new Door(x-width/2, y-height/2+1, "Up", "Door", result));}
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
