package code.core;

import code.math.IOHelp;

import code.world.Tile;

import code.world.fixed.dividers.Door;
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
public class GenerateRandom
{

  private long seed;
  private Random rand;

  private Tile[][] map;
  private int width;
  private int height;

  private Set<WorldObject> fixedObj = new HashSet<WorldObject>();
  private Set<Unit> units = new HashSet<Unit>();

  private static final byte[] WHICH_DIR = {1, 0, 0, 1, -1, 0, 0, -1};

  private static final int DEFAULT_MAP_SIZE = 50;
  private static final float FAIL_CHANCE_INCREMENT = 0.01f;

  private int dirChances = 4;
  private float failChance = 0.0f;
  private float connectChance = 0.25f;
  private float AIChance = 0.1f;
  private float lightChance = 0.05f;

  public void save(String title, String saveName) {
    generate();
    StringBuilder b = new StringBuilder(width*height);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        b.append(map[x][y].isActive() ? 1+" " : 0+" ");
      }
      b.append("\n");
    }
    IOHelp.saveToFile("../saves/"+saveName+"/scenes/"+title+"/Tiles.txt", b.toString());
    IOHelp.saveToFile("../saves/"+saveName+"/scenes/"+title+"/Decals.txt", "BG 1920 1080 Space.png false\ndecal 32 32 test.png true");
    b = new StringBuilder(width*height);
    for (WorldObject obj : fixedObj) {
      b.append(obj+"\n");
    }
    IOHelp.saveToFile("../saves/"+saveName+"/scenes/"+title+"/Fixed.txt", b.toString());
    b = new StringBuilder(width*height);
    for (Unit unit : units) {
      b.append(unit+"\n");
    }
    IOHelp.saveToFile("../saves/"+saveName+"/scenes/"+title+"/Units.txt", b.toString());
  }

  public Tile[][] generate() {
    seed = System.nanoTime();
    return generate(seed, DEFAULT_MAP_SIZE, DEFAULT_MAP_SIZE);
  }

  public Tile[][] generate(long seed, int width, int height) {
    this.width = width;
    this.height = height;
    rand = new Random(seed);
    map = new Tile[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        map[x][y] = new Tile((int) (x-width/2), (int) (y-height/2), 0);
      }
    }

    System.out.println("Starting...");
    startingPad();
    return map;
  }

  private void startingPad() {
    for (int i = -2; i <= 2; i++) {
      for (int j = -2; j <= 2; j++) {
        if (Math.abs(i)+Math.abs(j)==4) {continue;}
        map[i+width/2][j+height/2].activate();
        if (Math.abs(j) == 2) {
          fixedObj.add(new Wall(i, j==2?3:-2, "Up", "Wall", null));
          fixedObj.add(new Wall(j==2?3:-2, i, "Left", "Wall", null));
          if (Math.abs(i) == 1) {
            fixedObj.add(new Wall(i==1?2:-1, j, "Left", "Wall", null));
            fixedObj.add(new Wall(j, i==1?2:-1, "Up", "Wall", null));
          }
        }
      }
    }
    units.add(new Player(Tile.TILE_SIZE/2, Tile.TILE_SIZE/2, null));
    for (int i = 0; i < 4; i++) {
      failChance *= 0.5f;
      System.out.println("Attempting new chain...");
      int newDir = rand.nextInt(4);
      populate(width/2+WHICH_DIR[newDir*2]*3, height/2+WHICH_DIR[newDir*2+1]*3, newDir);
    }
  }

  private void populate(int x, int y, int dir) {
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
        if (rand.nextFloat() < AIChance) {units.add(new TestAI((cX-width/2)*Tile.TILE_SIZE+Tile.TILE_SIZE/2, (cY-height/2)*Tile.TILE_SIZE+Tile.TILE_SIZE/2, null));}
        if (rand.nextFloat() < lightChance) {fixedObj.add(new Light(cX-width/2, cY-height/2, "Static", null));}
        if (i == -(rWidth/2)) {if (!fixedObj.add(new Wall(cX-width/2, cY-height/2, "Left", "Wall", null)) && connect) {fixedObj.remove(new Wall(cX-width/2, cY-height/2, "Left", "Wall", null));}}
        if (i == rWidth/2) {if (!fixedObj.add(new Wall(cX-width/2+1, cY-height/2, "Left", "Wall", null)) && connect) {fixedObj.remove(new Wall(cX-width/2+1, cY-height/2, "Left", "Wall", null));}}
        if (j == -(rHeight/2)) {if (!fixedObj.add(new Wall(cX-width/2, cY-height/2, "Up", "Wall", null)) && connect) {fixedObj.remove(new Wall(cX-width/2, cY-height/2, "Up", "Wall", null));}}
        if (j == rHeight/2) {if (!fixedObj.add(new Wall(cX-width/2, cY-height/2+1, "Up", "Wall", null)) && connect) {fixedObj.remove(new Wall(cX-width/2, cY-height/2+1, "Up", "Wall", null));}}
      }
    }
    switch(dir) {
      case 0:
      if (fixedObj.remove(new Wall(x-width/2, y-height/2, "Left", "Wall", null))) {fixedObj.add(new Door(x-width/2, y-height/2, "Left", "Door", null));}
      break;
      case 1:
      if (fixedObj.remove(new Wall(x-width/2, y-height/2, "Up", "Wall", null))) {fixedObj.add(new Door(x-width/2, y-height/2, "Up", "Door", null));}
      break;
      case 2:
      if (fixedObj.remove(new Wall(x-width/2+1, y-height/2, "Left", "Wall", null))) {fixedObj.add(new Door(x-width/2+1, y-height/2, "Left", "Door", null));}
      break;
      case 3:
      if (fixedObj.remove(new Wall(x-width/2, y-height/2+1, "Up", "Wall", null))) {fixedObj.add(new Door(x-width/2, y-height/2+1, "Up", "Door", null));}
      break;
    }

    System.out.println("Success!");

    for (int i = 0; i < dirChances; i++) {
      System.out.println("Attempting...");
      int newDir = rand.nextInt(4);
      int LCR = rand.nextInt(3)-1;
      populate(x+WHICH_DIR[newDir*2]*(rWidth/2+1)+WHICH_DIR[dir*2]*(rWidth/2)+WHICH_DIR[newDir*2+1]*(rWidth/2)*LCR,
      y+WHICH_DIR[newDir*2+1]*(rHeight/2+1)+WHICH_DIR[dir*2+1]*(rHeight/2)+WHICH_DIR[newDir*2]*(rHeight/2)*LCR,
      newDir);
    }
  }
}
