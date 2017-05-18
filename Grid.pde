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

  //initializes all the blocks at a value of 0
  public void initBlocks() {
    for (int col = 0; col < COLS; col++)
      for (int row = 0; row < ROWS; row++)
        setBlock(col, row); //no need to set value and change as the approriate method does it already
  }

  //checks if the values col/row are valid
  public boolean isValid(int col, int row) {
    return (0 <=  row) && (row <  ROWS) && (0 <= col) && (col < COLS);
  }

  //swaps 2 blocks
  public void swap(int col1, int row1, int col2, int row2) {
    Block temp = block[col1][row1];
    block [col1][row1] = block[col2][row2];
    block [col2][row2] = temp;
  }

  //checks if 2 blocks at given col and row have the Same value
  public boolean canMerge(int col1, int row1, int col2, int row2) {
    return (block[col1][row1].getValue() == block[col2][row2].getValue());
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
    return false;
  }
  
  //gets all the empty locations and put it into a 
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
    //if locs != null then give a random value from the array otherwise null
    return locs != null ? locs.get((int) (locs.size() * Math.random())) : null;
  }

  // Randomly select an open location to place a block.
  public void placeBlock() {
    ArrayList<Location> loc = new ArrayList<Location>();
    loc = getEmptyLocations();
    Location l = selectLocation(loc);

    if ((int) 8 * Math.random() <= 6)
      block[l.getCol()][l.getRow()].setValue(2);
    else
      block[l.getCol()][l.getRow()].setValue(4);
  }

  // Are there any adjacent blocks that contain the same value?
  public boolean hasCombinableNeighbors() { //<>//
   for(int col = 0; col < COLS; col++){
     for(int row = 0; row < ROWS; row++){
       if((isValid(col+1,row) && canMerge(col,row,col+1,row)) ||
          (isValid(col-1,row) && canMerge(col,row,col-1,row)) ||
          (isValid(col,row+1) && canMerge(col,row,col,row+1)) ||
          (isValid(col,row-1) && canMerge(col,row,col,row-1)))
             return true;
     }
   }
   return false;
  }

  // Notice how an enum can be used as a data type
  //
  // This is called ) method  the KeyEvents tab
  public boolean someBlockCanMoveInDirection(DIR dir) { //<>//
    
    switch(dir){
      case NORTH: // only row 1 and subseq can move north
        for(int col = 0; col < COLS; col++){
          for(int row = 1; row < ROWS; row++){
            if(!block[col][row].isEmpty() &&
               (block[col][row-1].isEmpty() || canMerge(col,row,col,row-1)))
                return true;
            }
          }
        break;
      case EAST:
        for(int col = 0; col < COLS-1; col++){
          for(int row = 0; row < ROWS; row++){
            if(!block[col][row].isEmpty() &&
               (block[col+1][row].isEmpty() || canMerge(col,row,col+1,row)))
                return true;
            }
          }
        break;
     case SOUTH:
       for(int col = 0; col < COLS; col++){
          for(int row = 0; row < ROWS-1; row++){
            if(!block[col][row].isEmpty() &&
               (block[col][row+1].isEmpty() || canMerge(col,row,col,row+1)))
                return true;
              }
            }
        break;
    case WEST:
      for(int col = 1; col < COLS; col++){
          for(int row = 0; row < ROWS; row++){
            if(!block[col][row].isEmpty() && 
               (block[col-1][row].isEmpty() || canMerge(col,row,col-1,row)))
                return true;
              }
            }
        break;
    default:
      return false;
     }
     return false;
  } //<>// //<>// //<>// //<>// //<>// //<>// //<>//

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
          fill(#000000);
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
              block[col][row] = other.getBlock(col,row);
        }

        public boolean isGameOver() {
          return !(canPlaceBlock() || hasCombinableNeighbors());
        }

        public void showGameOver() {
          fill(#0000BB);
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