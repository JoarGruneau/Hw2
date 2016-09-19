import java.util.*;


public class Player {
    int value_loss=-1000;
    int value_win=1000;
    int value_tie=-100;
    int BOARD_SIZE = 4;
    int max_depth = 2;
    /**
     * Performs a mo
     *
     * @param gameState
     *            the current state of the board
     * @param deadline
     *            time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play(final GameState gameState, final Deadline deadline) {
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        if (nextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(gameState, new Move());
        }
                    
        if(gameState.getNextPlayer()==Constants.CELL_O){
            int best_value_move=value_loss;
            int tmp_value_move;
            GameState best_move=nextStates.firstElement();
            System.err.println("new turn -------------------------------------------------");
            for(GameState tmp_state: nextStates){ 
                tmp_value_move=searchMinMax(tmp_state, 
                        Constants.CELL_O, max_depth, value_loss,value_win);
                    System.err.println(tmp_value_move);
                    System.err.println(tmp_state.toString(Constants.CELL_O));
                if( tmp_value_move>=best_value_move){
                    if(tmp_state.isXWin()){ // Prohibits slow play, otherwise alg will value a win 3 moves away same as this
                        best_move=tmp_state;
                        break;
                    }
                    best_value_move=tmp_value_move;
                    best_move=tmp_state;

                }
            }
                    
            return best_move;
        }

        else{
            Random random = new Random();
            GameState move=nextStates.elementAt(
                    random.nextInt(nextStates.size()));
            return move;
        }
    }

    public int searchMinMax(GameState game_state, int player, int depth, 
            int alpha, int beta){
        Vector<GameState> nextStates = new Vector<>();
        game_state.findPossibleMoves(nextStates); // I guess you can run this after depth check and EOG check to save time?
        int best_move;

        if(game_state.isEOG()){
            return valueEnd(game_state);
        }
        else if(depth==0){
                    
            if(player==Constants.CELL_X){
                return value(game_state, Constants.CELL_X)
                        -value(game_state, Constants.CELL_O);
            }
            else{
                return -(value(game_state, Constants.CELL_O)
                        -value(game_state, Constants.CELL_X));
            }
        
        }
        else{
        
        if(player==Constants.CELL_X){
            best_move=value_loss;
            for(GameState state:nextStates){
                int move = searchMinMax(state,
                        Constants.CELL_O, depth-1, alpha, beta);
                if(move>=beta){
                    return beta;
                }
                else if(move>alpha){
                    alpha=move;
                }
                //best_move=Math.max(move, best_move);
            }
            return alpha;
        }
        else{
            best_move=value_win;
            for(GameState state:nextStates){
                int move = searchMinMax(state,
                        Constants.CELL_X, depth-1, alpha,beta);
                if(move<=alpha){
                    return alpha;
                }
                else if(move<beta){
                    beta=move;
                }
            }
            return beta;
            
        }
        }

    }

    
    
    public int valueEnd(GameState game_state){
        if(game_state.isXWin()){
            return value_win;
        }
        else if(game_state.isOWin()){
            return value_loss;
        }
        else{
            return value_tie;
        }
        
    }
    public int value(GameState game_state, int player){
        // Returns value of all 76 rows. In total 76*4 cells are checked, right?
        return valueRows(game_state, player)
                + valueColumns(game_state, player)
                + valueLayers(game_state, player)
                + valueDiagonals(game_state, player);
    }
    public int valueDiagonals(GameState game_state, int player){
        // Returns the value of the 24 normal diagonals and the 4 main diagonals
        return valueDiagonalRows(game_state, player)
            + valueDiagonalColumns(game_state, player)
            + valueDiagonalLayers(game_state, player)
            + valueMainDiagonals(game_state, player);
    }
    public int valueMainDiagonals(GameState game_state, int player){
        // Returns the value of the 4 main diagonals
        int tmp_value = 0;
        int tmp_value_diagonal;
        int cell;
        int row;
        int col;
        for(int direction_row = -1; direction_row < 3; direction_row += 2){
            for(int direction_col = -1; direction_col < 3; direction_col += 2){
                // row and col start from top or bottom
                row = (direction_row == -1) ? BOARD_SIZE-1 : 0;
                col = (direction_col == -1) ? BOARD_SIZE-1 : 0;
                tmp_value_diagonal = 0;
                for(int layer = 0; layer<BOARD_SIZE; layer++){
                    cell = game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_value_diagonal++;
                    }
                    else if(cell== Constants.CELL_EMPTY){}  // Why not include this in the next else?
                    else{
                        tmp_value_diagonal=0;
                        break;
                    }
                    row += direction_row;
                    col += direction_col;
                }
                tmp_value += tmp_value_diagonal;
            }
        }    
        return tmp_value;
    }
    public int valueDiagonalRows(GameState game_state, int player){
        // Returns value of the 8 normal diagonals over the rows
        int tmp_value = 0;
        int tmp_value_diagonal;
        int cell;
        int layer;
        for(int row=0; row<BOARD_SIZE; row++){
            for(int direction = -1; direction<3; direction += 2){ 
                // 'direction' determines direction of diagonal, up or down
                tmp_value_diagonal = 0;
                // Layer starts from top or bottom
                layer = (direction == -1) ? BOARD_SIZE-1 : 0;
                for(int col=0; col<BOARD_SIZE; col++){
                    cell = game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_value_diagonal++;
                    }
                    else if(cell== Constants.CELL_EMPTY){}  // Why not include this in the next else?
                    else{
                        tmp_value_diagonal=0;
                        break;
                    }
                    layer += direction;
                }
                tmp_value += tmp_value_diagonal*tmp_value_diagonal;
            }

        }
        return tmp_value;
    }
    public int valueDiagonalColumns(GameState game_state, int player){
        // Returns value of the 8 normal diagonals over the columns
        int tmp_value = 0;
        int tmp_value_diagonal;
        int cell;
        int layer;
        for(int col=0; col<BOARD_SIZE; col++){
            for(int direction = -1; direction<3; direction += 2){ 
                // 'direction' determines direction of diagonal, up or down
                tmp_value_diagonal = 0;
                // Layer starts from top or bottom
                layer = (direction == -1) ? BOARD_SIZE-1: 0;
                for(int row=0; row<BOARD_SIZE; row++){
                    cell = game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_value_diagonal++;
                    }
                    else if(cell== Constants.CELL_EMPTY){}  // Why not include this in the next else?
                    else{
                        tmp_value_diagonal=0;
                        break;
                    }
                    layer += direction;
                }
                tmp_value += tmp_value_diagonal*tmp_value_diagonal;
            }

        }
        return tmp_value;
    }
    public int valueDiagonalLayers(GameState game_state, int player){
        // Returns value of the 8 normal diagonals over the layers
        int tmp_value = 0;
        int tmp_value_diagonal;
        int cell;
        int col;
        for(int layer=0; layer<BOARD_SIZE; layer++){
            for(int direction = -1; direction<3; direction += 2){ 
                // 'direction' determines direction of diagonal, up or down
                tmp_value_diagonal = 0;
                // Column starts from top or bottom
                col = (direction == -1) ? BOARD_SIZE-1 : 0;
                for(int row=0; row<BOARD_SIZE; row++){
                    cell = game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_value_diagonal++;
                    }
                    else if(cell== Constants.CELL_EMPTY){}  // Why not include this in the next else?
                    else{
                        tmp_value_diagonal=0;
                        break;
                    }
                    col += direction;
                }
                tmp_value += tmp_value_diagonal*tmp_value_diagonal;
            }

        }
        return tmp_value;
    }

    public int valueRows(GameState game_state, int player){
        // Rewritten for 3D
        // Returns value of 16 rows
        int tmp_value = 0;
        int tmp_value_row;
        int cell;
        for(int layer=0; layer<BOARD_SIZE; layer++){
            for(int row=0; row<BOARD_SIZE; row++){
                tmp_value_row=0;
                for(int col=0; col<BOARD_SIZE; col++){
                    cell = game_state.at(row,col,layer);
                    if(cell == player){
                        tmp_value_row++;
                    }
                    else if(cell==Constants.CELL_EMPTY){} // why not include this in next else?
                    else{
                        tmp_value_row=0;
                        break; 
                    }
                }
                tmp_value+=tmp_value_row*tmp_value_row; // Generalize with Math.pow
            }
        }
        return tmp_value;
    }

    public int valueColumns(GameState game_state, int player){
        //Rewritten for 3D
        // Returns value of 16 rows
        int tmp_value=0;
        int tmp_value_column;
        int cell;
        for(int layer=0; layer<BOARD_SIZE; layer++){
            for(int col=0; col<BOARD_SIZE; col++){
                tmp_value_column=0;
                for(int row=0; row<BOARD_SIZE; row++){
                    cell=game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_value_column++;
                    }
                    else if(cell==Constants.CELL_EMPTY){} // why not include this in next else?
                    else{
                        tmp_value_column=0;
                        break;
                    }
                    
                }
                tmp_value+=tmp_value_column*tmp_value_column; // generalize with math.pow
            }
        }
        return tmp_value;
    }

    public int valueLayers(GameState game_state, int player){
        // Returns value of 16 rows
        int tmp_value=0;
        int tmp_value_layer_part;
        int cell;
        for(int row=0; row<BOARD_SIZE; row++){
            for(int col=0; col<BOARD_SIZE; col++){
                tmp_value_layer_part = 0;
                for(int layer=0; layer<BOARD_SIZE; layer++){
                    cell=game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_value_layer_part++;
                    }
                    else if(cell==Constants.CELL_EMPTY){} // why not include this in next else?
                    else{
                        tmp_value_layer_part=0;
                        break;
                    }
                }
                tmp_value+=tmp_value_layer_part*tmp_value_layer_part; // generalize with math.pow
            }
        }
        return tmp_value;
    }
}
