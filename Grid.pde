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
  public boolean hasCombinableNeighbors() { //<>//
    boolean b = false;
    for (int col = 0; col < COLS; col++) {
      for (int row = 0; row < ROWS; row++) {
        if (row == 0) {
          if (col == 0) {
            if ((block[col][row].getValue() == block[col+1][row].getValue()) || (block[col][row].getValue() == block[col][row-1].getValue()))
              b = true;
          } else if (col == COLS) {
            if (((block[col][row].getValue() == block[col-1][row].getValue()) || (block[col][row].getValue() == block[col][row-1].getValue())))
              b = true;
          } else {
            if ((block[col][row].getValue() == block[col+1][row].getValue()) || (block[col][row].getValue() == block[col][row-1].getValue()) || (block[col][row].getValue() == block[col-1][row].getValue()))
              b = true;
          }
        } else if (col == 0) {
          if (row == ROWS)
            if ((block[col][row].getValue() == block[col+1][row].getValue()) || (block[col][row].getValue() == block[col][row-1].getValue()))
              b = true;
            else
              if ((block[col][row].getValue() == block[col+1][row].getValue()) || (block[col][row].getValue() == block[col][row-1].getValue()) || (block[col][row].getValue() == block[col][row+1].getValue()))
                b = true;
        } else if (col == COLS) {
          if (row == ROWS)
            if ((block[col][row].getValue() == block[col][row+1].getValue()) || (block[col][row].getValue() == block[col-1][row].getValue()))
              b = true;
            else
              if ((block[col][row].getValue() == block[col-1][row].getValue()) || (block[col][row].getValue() == block[col][row+1].getValue()) || (block[col][row].getValue() == block[col][row-1].getValue()))
                b = true;
        } else if (row == ROWS) {
          if ((block[col][row].getValue() == block[col-1][row].getValue()) || (block[col][row].getValue() == block[col][row+1].getValue()) || (block[col][row].getValue() == block[col+1][row].getValue()))
            b = true;
        } else {
          if ((block[col][row].getValue() == block[col-1][row].getValue()) || (block[col][row].getValue() == block[col][row+1].getValue()) || (block[col][row].getValue() == block[col+1][row].getValue()) || (block[col][row].getValue() == block[col][row -1].getValue()))
            b = true;
        }
        
        if(b)
          return b;
      }
    }
    return false; // stub
  }

  // Notice how an enum can be used as a data type
  //
  // This is called ) method  the KeyEvents tab
  //only checking for one block or checking for more??
  public boolean someBlockCanMoveInDirection(DIR dir) { //<>//
   boolean b = false; //<>//
   ArrayList<Location> loc = new ArrayList<Location>();
    //might not need for loop if only goign for one block
    for (int col = 0; col < COLS; col++) {
      for (int row = 0; row < ROWS; row++) {
        if(!block[col][row].isEmpty())
          loc.add(new Location(col, row)); //<>//
      }
    }
    
    //does not the check the whole row ol collum so it doesnt work. Also i cant have a boolean beacause then maybe the last value in the 
    //ArrayList is not working
    for(Location l : loc){ //<>//
        switch(dir) { //<>//
          case SOUTH: 
            if (l.getRow() == ROWS-1)
              b = false;
            else if (isValid(l.getCol(), l.getRow()-1) && (canMerge(l.getCol(), l.getRow(), l.getCol(), l.getRow()-1) || block[l.getCol()][l.getRow()-1].isEmpty()))
              b = true;
          else
            b = false;
            
            break;
          case EAST: //<>//
          if (l.getCol() == COLS-1)
            b = false;
          else if (isValid(l.getCol()-1, l.getRow()) && (block[l.getCol()-1][l.getRow()].isEmpty() || canMerge(l.getCol(), l.getRow(), l.getCol()-1, l.getRow())))
            b = true;
          else
            b = false;
          
          break;
         case NORTH: //<>//
           if(l.getRow() == 0)
             b = false;
           else if (isValid(l.getCol(),l.getRow()+1) && (block[l.getCol()][l.getRow()+1].isEmpty() || canMerge(l.getCol(), l.getRow(), l.getCol(), l.getRow()+1)))
             b = true;
           else
             b = false;
            
             break;
          case WEST: //<>//
            if(l.getCol() == 0)
              b = false;
            else if(isValid(l.getCol()-1, l.getRow()) && (block[l.getCol()-1][l.getRow()].isEmpty() || canMerge(l.getCol(), l.getRow(), l.getCol()-1, l.getRow())))
              b = true;
            else
              b = false;

          break;
          default:
            b = false;
            
            break;
        }
        
        if(b)
          return b;
       }
       return b;
      }

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
              block[col][row] = other.block[col][row];
        }

        public boolean isGameOver() {
          return canPlaceBlock() && hasCombinableNeighbors() ? false : true;
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