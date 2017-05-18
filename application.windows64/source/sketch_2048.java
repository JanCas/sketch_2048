import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class sketch_2048 extends PApplet {

public final int GRID_X_OFFSET = 15;      // distance from left to start drawing grid
public final int GRID_Y_OFFSET = 85;      // distance from top to start drawing grid
public final int BLOCK_SIZE = 120;   // width and height of a block
public final int BLOCK_MARGIN = 15;  // separation between blocks
public final int BLOCK_RADIUS = 5;   // for making blocks look slightly rounded in corners
public final int Y_TEXT_OFFSET = 7;  // for centering the numbers when drawn on blocks
public final int GRID_SIZE = 4;      // number of rows and columns
public final int COLS = GRID_SIZE;
public final int ROWS = GRID_SIZE;
public PFont blockFont;

public final int SCORE_Y_OFFSET = 36;
public PFont scoreFont;

public final int BACKGROUND_COLOR = color(189, 195, 199);
public final int BLANK_COLOR = color(203, 208, 210);
public Grid grid = new Grid(COLS, ROWS);
public Grid backup_grid = new Grid(COLS, ROWS);

// Every time a key is pressed, blocks need to move visually (if there is a way to move).
// All of the animations to be carried out are stored in anims.
public ArrayList<Animation> anims = new ArrayList<Animation>();
// number of movements to complete a block animation
public final int TICK_STEPS = 20;
// counter for number of iterations have been used to animate block movement
// if animation_ticks == TICK_STEPS, then the display is not moving blocks
public int animation_ticks = TICK_STEPS;  


// setup() is analogous to init() in Java.  It is done once at the start of the
// program.  (It is more applet-like than application-like, but if you want to think
// of it as analogous to public static void main(), I won't quibble.)
public void setup()
{
    // Do not include P3D as third input as it prevents keyDown; no idea why
  background(BACKGROUND_COLOR);
  noStroke();
  
  blockFont = createFont("LucidaSans", 50);;
  textFont(blockFont);
  textAlign(CENTER, CENTER); 
  
  scoreFont = createFont("LucidaSans", 42);

  // Comment out this setBlock() and replace it with a placeBlock() once you have
  // written placeBlock().
  //grid.setBlock(2,0,2,false);
  grid.placeBlock();
  System.out.print(grid);
  backup_grid.gridCopy(grid);   // save grid in backup_grid in case Undo is needed
}

// This is where the animation will take place
// draw() exhibits the behavior of being inside an infinite loop
public void draw() {  
  background(BACKGROUND_COLOR);
  grid.computeScore();
  grid.showScore();
  grid.showBlocks();
  
  // Don't show GAME OVER during an animation
  if (animation_ticks == TICK_STEPS && grid.isGameOver()) {
    grid.showGameOver();
  }
  
  if (animation_ticks < TICK_STEPS) {
    for(int row = 0; row < ROWS; row++) {
      for(int col = 0; col < COLS; col++) {
        fill(BLANK_COLOR);
        rect(GRID_X_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*col, 
             GRID_Y_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*row, 
             BLOCK_SIZE, BLOCK_SIZE, BLOCK_RADIUS);
      }
    }
    
    
    // animation_ticks is used to count up to TICK_STEPS, which
    // determines how many
    
    // Iterate on the anims ArrayList to 
    for(int i = 0; i < anims.size(); i++) {
      Animation a = anims.get(i);
      float col = 1.0f * ((a.getToCol() - a.getFromCol())*animation_ticks)/TICK_STEPS + a.getFromCol();
      float row = 1.0f * ((a.getToRow() - a.getFromRow())*animation_ticks)/TICK_STEPS + a.getFromRow();
      float adjustment = (log(a.getFromValue()) / log(2)) - 1;
      fill(color(242 , 241 - 8*adjustment, 239 - 8*adjustment));
      rect(GRID_X_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*col, GRID_Y_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*row, BLOCK_SIZE, BLOCK_SIZE, BLOCK_RADIUS);
      fill(color(108, 122, 137));
      text(str(a.getFromValue()), GRID_X_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*col + BLOCK_SIZE/2, GRID_Y_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*row + BLOCK_SIZE/2 - Y_TEXT_OFFSET);
    }
    animation_ticks += 1;   
  }
}

public void gameUpdate(DIR direction)
{  
  //BEGIN MOVEMENT SECTION
  Grid newGrid = new Grid(COLS, ROWS);
  newGrid.gridCopy(grid);   // 
  anims = new ArrayList<Animation>();
  
  // EAST-WEST movement
  if (direction == DIR.WEST || direction == DIR.EAST) {
    int startingCol = direction == DIR.EAST ? GRID_SIZE-1 : 0;
    int endingCol = direction == DIR.EAST ? -1 : GRID_SIZE;
    int colAdjust = direction == DIR.EAST ? 1 : -1;
    
    for (int row = 0; row < ROWS; row++) {
      for (int col = startingCol; col != endingCol; col -= colAdjust) {
        int colPos = col;
        int val = newGrid.getBlock(col, row).getValue();
        if (!newGrid.getBlock(col,row).isEmpty()) {
          // While the position being inspected is in the grid and does not contain a block
          // whose values has already been changed this move
          while(newGrid.isValid(colPos + colAdjust, row) && !newGrid.getBlock(colPos, row).hasChanged()) {
            if (newGrid.getBlock(colPos+colAdjust,row).isEmpty()) {
            // if (newGrid[colPos + colAdjust][row].getValue() == -1) {
              // Move the block into the empty space and create an empty space where the block was
              newGrid.swap(colPos,row,colPos+colAdjust,row);
            } else if (newGrid.canMerge(colPos + colAdjust, row, colPos, row)) {
                if (!newGrid.getBlock(colPos + colAdjust, row).hasChanged()) {
                  newGrid.setBlock(colPos + colAdjust, row, newGrid.getBlock(colPos, row).getValue()*2, true);
                  newGrid.setBlock(colPos, row);
              }
            } else {  // Nowhere to move to
              break;  // Exit while loop
            }
            colPos += colAdjust;
          }
          // If a block moves, add its information to the list of blocks that must be animated
          anims.add(new Animation(col,row,val,colPos,row,val));
        }
      }
    }
  }
  
  // NORTH-SOUTH movement
  // 
  // Analogous to EAST-WEST movement
  if (direction == DIR.NORTH || direction == DIR.SOUTH) {
    int startingRow = direction == DIR.SOUTH ? GRID_SIZE-1 : 0;
    int endingRow = direction == DIR.SOUTH ? -1 : GRID_SIZE;
    int rowAdjust = direction == DIR.SOUTH ? 1 : -1;

    for (int col = 0; col < COLS; col++) {
      for (int row = startingRow; row != endingRow; row -= rowAdjust) {
        int rowPos = row;
        int val = newGrid.getBlock(col, rowPos).getValue();
        if (!newGrid.getBlock(col,rowPos).isEmpty()) {
          while(newGrid.isValid(col, rowPos + rowAdjust) && !newGrid.getBlock(col, rowPos).hasChanged()) {
            if (newGrid.getBlock(col, rowPos + rowAdjust).isEmpty()) {
              newGrid.swap(col, rowPos, col, rowPos+rowAdjust);
            } else if(newGrid.canMerge(col, rowPos + rowAdjust, col, rowPos)) {
              if(!newGrid.getBlock(col, rowPos + rowAdjust).hasChanged()) {
                newGrid.setBlock(col, rowPos + rowAdjust, newGrid.getBlock(col, rowPos).getValue()*2, true);
                newGrid.setBlock(col, rowPos);
              }
            } else {
              break;  // Exit while loop
            }
            rowPos += rowAdjust;
          }
          // If a block moves, add its information to the list of blocks that must be animated
          anims.add(new Animation(col,row,val,col,rowPos,val)); 
        }
      }
    }
  }
  
  newGrid.clearChangedFlags();
  if (newGrid.canPlaceBlock()) {
    newGrid.placeBlock();
  }
  
  backup_grid.gridCopy(grid);  // Copy the grid to backup in case Undo is needed
  grid.gridCopy(newGrid);      // The newGrid should now be made the main grid
  
  //END MOVEMENT SECTION
  
  startAnimations();
}

public void startAnimations() {
  // Effectively turns draw() into a for loop with animation_ticks as the index
  animation_ticks = 0;
}
// Used to store grid information about blocks that need to move after a key press.
//
// Do not change this file.
public class Animation
{
  private int fromCol;
  private int fromRow;
  private int fromValue;
  private int toCol;
  private int toRow;
  private int toValue;
  
  Animation(int fCol, int fRow, int fv, int tcol, int trow, int tv) {
    fromCol = fCol;
    fromRow = fRow;
    fromValue = fv;
    toCol = tcol;
    toRow = trow;
    toValue = tv;
  }
  
  public int getFromCol() { return fromCol; }
  public int getFromRow() { return fromRow; }
  public int getFromValue() { return fromValue; }
  public int getToCol() { return toCol; }
  public int getToRow() { return toRow; }
  public int getToValue() { return toValue; }
}
// You are not allowed to change this file.

public class Block
{
  private final int EMPTY = 0;
  private int value = EMPTY;              // Power of 2 for the block; 0 means blank
  // If a block moves, it could merge with another block of like value.
  // hasChanged being set to true means that for this keypress, the block has changed
  // already and other blocks cannot merge with it.
  private boolean hasChanged = false;
  
  public Block() {
    this(0, false);
  }
  
  public Block(int val, boolean changed) {
    value = val;
    hasChanged = changed;
  }
  
  public void setValue(int val) { value = val; }
  public int getValue() { return value; }
  public void setChanged(boolean changed) { hasChanged = changed; };
  public boolean hasChanged() { return hasChanged; };
  public boolean isEmpty() { return value == EMPTY; }
}
// You are not allowed to change this file.
//
// An enum is useful for providing symbols to describe things.
// In this case we want a way to refer to directions and compass
// directions such as WEST, NORTH, EAST, and SOUTH are a lot nicer
// than numbers where you have to remember what each number means.
//
// It's another way of doing abstraction.
//
// If you are curious as to the details of enum, I'll leave it to you
// to look it up.  For now, it isn't critical to do so because the
// big point of an abstraction is that we don't have to worry about
// the implementation details.
//
// ALL_CAPS are used because these things are constants.
//
// To use a symbol: DIR.WEST, DIR.NORTH, etc.

enum DIR { WEST, NORTH, EAST, SOUTH };
public class Grid {
  Block[][] block;
  private final int COLS;
  private final int ROWS;
  private int score;

  public Grid(int cols, int rows) {
    COLS = cols;
    ROWS = rows;
    block = new Block[COLS][ROWS];
    initBlocks();  // initializes all blocks to empty blocks
  }

  public Block getBlock(int col, int row) {
    return block[col][row];
  }

  public void setBlock(int col, int row, int value, boolean changed) {
    block[col][row] = new Block(value, changed);
  }

  public void setBlock(int col, int row, int value) {
    setBlock(col, row, value, false);
  }

  public void setBlock(int col, int row) {
    setBlock(col, row, 0, false);
  }

  public void setBlock(int col, int row, Block b) {
    block[col][row] = b;
  }

  public void initBlocks() {
    for (int col = 0; col < COLS; col++)
      for (int row = 0; row < ROWS; row++)
        setBlock(col, row, 0);
  }

  //checks if the values are valid
  public boolean isValid(int col, int row) {
    return (0 <=  row) && (row <  ROWS) && (0 <= col) && (col < COLS) ? true : false;
  }

  //swaps 2 blocks
  public void swap(int col1, int row1, int col2, int row2) {
    Block temp = block[col1][row1];
    block [col1][row1] = block[col2][row2];
    block [col2][row2] = temp;
  }

  //checks if 2 blocks are the Same value
  public boolean canMerge(int col1, int row1, int col2, int row2) {
    return (block[col1][row1].getValue() == block[col2][row2].getValue()) ? true : false;
  }

  public void clearChangedFlags() {
    for (int col = 0; col < COLS; col++) {
      for (int row = 0; row < ROWS; row++) {
        block[col][row].setChanged(false);
      }
    }
  }

  //goes through the whole grid and checks if there is an open space
  public boolean canPlaceBlock() {
    for (int col = 0; col < COLS; col++)
      for (int row = 0; row < ROWS; row++)
        if (block[col][row].isEmpty())
          return true;
    return false; // stub
  }

  public ArrayList<Location> getEmptyLocations() {
    // Put all locations that are currently empty into locs
    ArrayList<Location> locs = new ArrayList<Location>();
    for (int col = 0; col < COLS; col++)
      for (int row = 0; row < ROWS; row++)
        if (block[col][row].isEmpty())
          locs.add(new Location(col, row));
    return locs; // stub
  }

  public Location selectLocation(ArrayList<Location> locs) {
    if (locs != null)
      return locs.get((int) (locs.size() * Math.random()));
    else
      return null; // stub
  }

  // Randomly select an open location to place a block.
  public void placeBlock() {
    ArrayList<Location> loc = new ArrayList<Location>();
    loc = getEmptyLocations();
    Location l = selectLocation(loc);

    if ((int) 8 * Math.random() <= 7)
      block[l.getCol()][l.getRow()].setValue(2);
    else
      block[l.getCol()][l.getRow()].setValue(4);
  }

  // Are there any adjacent blocks that contain the same value?
  public boolean hasCombinableNeighbors() {
   for(int col = 0; col < COLS; col++){
     for(int row = 0; row < ROWS; row++){
       if(isValid(col+1,row) && canMerge(col,row,col+1,row))
         return true;
       else if(isValid(col-1,row) && canMerge(col,row,col-1,row))
         return true;
       else if(isValid(col,row+1) && canMerge(col,row,col,row+1))
         return true;
       else if(isValid(col,row-1) && canMerge(col,row,col,row-1))
         return true;
     }
   }
   return false;
  }

  // Notice how an enum can be used as a data type
  //
  // This is called ) method  the KeyEvents tab
  //only checking for one block or checking for more??
  public boolean someBlockCanMoveInDirection(DIR dir) {
    //bored
    switch(dir){
      case NORTH:
        for(int col = 0; col < COLS; col++){
          for(int row = 1; row < ROWS; row++){
            if(!block[col][row].isEmpty()){
              if(block[col][row-1].isEmpty() || canMerge(col,row,col,row-1)){
                return true;
              }
            }
          }
        }
        break;
      case EAST:
        for(int col = 0; col < COLS-1; col++){
          for(int row = 0; row < ROWS; row++){
            if(!block[col][row].isEmpty()){
              if(block[col+1][row].isEmpty() || canMerge(col,row,col+1,row)){
                return true;
              }
            }
          }
        }
        break;
     case SOUTH:
       for(int col = 0; col < COLS; col++){
          for(int row = 0; row < ROWS-1; row++){
            if(!block[col][row].isEmpty()){
              if(block[col][row+1].isEmpty() || canMerge(col,row,col,row+1)){
                return true;
              }
            }
          }
        }
        break;
    case WEST:
      for(int col = 1; col < COLS; col++){
          for(int row = 0; row < ROWS; row++){
            if(!block[col][row].isEmpty()){
              if(block[col-1][row].isEmpty() || canMerge(col,row,col-1,row)){
                return true;
              }
            }
          }
        }
        break;
    default:
      return false;
     }
     return false;
  } //<>// //<>// //<>// //<>// //<>// //<>//

        // Computes the number of points that the player has scored
        public void computeScore() {
          score = 0;
          for (int col = 0; col < COLS; col++)
          for (int row = 0; row < ROWS; row++)
          score += block[col][row].getValue();
        }

        public int getScore() {
          return score;
        }

        public void showScore() {
          textFont(scoreFont);
          fill(0xff000000);
          text("Score: " + getScore(), width/2, SCORE_Y_OFFSET);
          textFont(blockFont);
        }

        public void showBlocks() {
          for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
              Block b = block[col][row];
              if (!b.isEmpty()) {
                float adjustment = (log(b.getValue()) / log(2)) - 1;
                fill(color(242, 241 - 8*adjustment, 239 - 8*adjustment));
                rect(GRID_X_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*col, GRID_Y_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*row, BLOCK_SIZE, BLOCK_SIZE, BLOCK_RADIUS);
                fill(color(108, 122, 137));
                text(str(b.getValue()), GRID_X_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*col + BLOCK_SIZE/2, GRID_Y_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*row + BLOCK_SIZE/2 - Y_TEXT_OFFSET);
              } else {
                fill(BLANK_COLOR);
                rect(GRID_X_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*col, GRID_Y_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN)*row, BLOCK_SIZE, BLOCK_SIZE, BLOCK_RADIUS);
              }
            }
          }
        }

        // Copy the contents of another grid to this one
        public void gridCopy(Grid other) {
          for (int col = 0; col < COLS; col++)
            for(int row = 0; row < ROWS; row++)
              block[col][row] = other.block[col][row];
        }

        public boolean isGameOver() {
          return canPlaceBlock() || hasCombinableNeighbors() ? false : true;
        }

        public void showGameOver() {
          fill(0xff0000BB);
          text("GAME OVER", GRID_X_OFFSET + 2*BLOCK_SIZE + 15, GRID_Y_OFFSET + 2*BLOCK_SIZE + 15);
        }

        //public String toString() {
        //  String str = "";
        //  for (int row = 0; row < ROWS; row++) {
        //    for (int col = 0; col < COLS; col++) {
        //      str += block[col][row].getValue() + " ";
        //    }
        //    str += "\n";   // "\n" is a newline character
        //  }
        //  return str;
        //}
      }
class Location {
  private int col;
  private int row;
  
  public Location(int col, int row) {
    this.col = col;
    this.row = row;
  }
  
  public int getCol() { return col; }
  public int getRow() { return row; }
}
public boolean isBetween(int n, int lower, int upper) {
  return (n >= lower && n <= upper);
}
// The only keys (and corresponding keyCodes) that are used to control the game are:
// * RETURN (10)--Restarts game if Game Over is being displayed
// * LEFT ARROW (37)--Move blocks to the left
// * UP ARROW (38)--Move blocks up
// * RIGHT ARROW (39)--Move blocks right
// * DOWN ARROW (40)--Move blocks down
// * Upper-case 'U' (85)--Undo (revert one keypress)

public void keyPressed() {
  if (grid.isGameOver()) {
    // If RETURN is pressed, then start a fresh game with one block
    if (keyCode == 10) { 
      grid.initBlocks();
      grid.placeBlock();
    } 
    return;
  }
  
  // If a key is pressed and it isn't LEFT (arrow), RIGHT, UP, DOWN, or U,
  // then ignore it by returning immediately
  if (!(isBetween(keyCode, 37, 40) || keyCode == 85)) return;

  if (keyCode == 85) {  // ASCII value for upper case U (for Undo)
     grid.gridCopy(backup_grid);  // Copy the backup grid to the main grid
     return;
  }
  
  // If you are curious about keyCodes, there are two things to look at:
  //
  // First, look up Unicode values (you can Google this easily)
  // Second, look at the documentation at Processing.org
  DIR dir;
  DIR[] dirs = { DIR.WEST, DIR.NORTH, DIR.EAST, DIR.SOUTH };
  // Key codes for LEFT ARROW, UP ARROW, RIGHT ARROW, and DOWN ARROW are 37--40.
  // By subtracting 37, we get an appropriate index for the dirs array that converts
  // LEFT ARROW to DIR.WEST, UP ARROW to DIR. 
  dir = dirs[keyCode-37];
  
  if (!grid.someBlockCanMoveInDirection(dir)) return;
  else gameUpdate(dir);
}
  public void settings() {  size(555, 625); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "sketch_2048" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}