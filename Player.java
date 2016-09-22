import java.util.*;

/*
We need symmetry breaking
We need oredering for alpha-beta pruning
Maybe we need to take deadline into account, return best hand if close to deadline
Something about storing best results in hashmap...
*/


public class Player {
    int value_loss=-1000;
    int value_win=1000;
    int value_tie=-100;
    int BOARD_SIZE = 4;
    int max_depth = 2;
    int extra_depth = 2; // used for iterative NegaMax
    boolean repeated_states_checking = false;
    int hash_capacity = 1000000;
    HashMap<String, Integer> visited_states = new HashMap<String, Integer>(hash_capacity);
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
        long initialTimeLeft = deadline.timeUntil(); // time left at start until move must be made
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        if (nextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(gameState, new Move());
        }

        /*int nr_occupied_cells = nrOccupiedCells(gameState);
        if(nr_occupied_cells < 20){
            max_depth = 2;
        }else if(nr_occupied_cells < 34){
            max_depth = 3;
        }else{
            max_depth = 4;
        }*/
                    
        if(gameState.getNextPlayer()==Constants.CELL_O){
            //GameState best_move = iterativeNegaMax(nextStates, deadline, initialTimeLeft);
            GameState best_move = maxDepthNegaMax(nextStates, deadline, initialTimeLeft);
                    
            return best_move;
        }

        else{
            Random random = new Random();
            GameState move=nextStates.elementAt(
                    random.nextInt(nextStates.size()));
            return move;
        }
    }

    public GameState maxDepthNegaMax(Vector<GameState> nextStates, Deadline deadline, long initialTimeLeft){
        int tmp_value_move;
        int best_value_move = value_loss;
        GameState best_move = nextStates.firstElement();

        for(GameState tmp_state:nextStates){
            tmp_value_move=-negaMax(tmp_state, max_depth, value_loss, value_win, -1, deadline, initialTimeLeft);
            if( tmp_value_move>=best_value_move){

                if(tmp_state.isXWin()){
                    best_move=tmp_state;
                    break;
                }
                best_value_move=tmp_value_move;
                best_move=tmp_state;
            }
            /*if((double)deadline.timeUntil()/initialTimeLeft < 0.1){
                //System.err.println("TIME RAN OUT");
                break;
            }*/
        }

        return best_move;
    }

    public GameState iterativeNegaMax(Vector<GameState> nextStates, Deadline deadline, long initialTimeLeft){
        Vector<GameState> newBestStates = new Vector<GameState>();
        Vector<GameState> lastBestStates = nextStates;
        int tmp_value_move;
        int best_value_move;
        GameState best_move = lastBestStates.firstElement();

        for(int plus_depth = 0; plus_depth < extra_depth; plus_depth++){
            best_value_move = value_loss;
            newBestStates = new Vector<GameState>();

            for(GameState tmp_state:lastBestStates){
                tmp_value_move=-negaMax(tmp_state, max_depth+plus_depth,value_loss, value_win, -1, deadline, initialTimeLeft);
                if( tmp_value_move>=best_value_move){

                    if(tmp_value_move > best_value_move){
                        newBestStates.clear();
                        newBestStates.add(tmp_state);
                    }else{ // this means tmp_value_move == best_value_move
                        newBestStates.add(tmp_state);
                    }
                    best_value_move = tmp_value_move;
                }
                /*if((double)deadline.timeUntil()/initialTimeLeft < 0.1){
                    //System.err.println("TIME RAN OUT");
                    break;
                }*/
            }
            lastBestStates = newBestStates;
        }

        best_value_move = value_loss;
        //int counter = 0;
        for(GameState tmp_state:newBestStates){
            tmp_value_move=-negaMax(tmp_state, max_depth+extra_depth, value_loss, value_win, -1, deadline, initialTimeLeft);
            if( tmp_value_move>=best_value_move){

                if(tmp_state.isXWin()){
                    best_move=tmp_state;
                    break;
                }
                best_value_move=tmp_value_move;
                best_move=tmp_state;
            }
            /*counter ++;
            if(counter > 4){
                break;
            }*/
            /*if((double)deadline.timeUntil()/initialTimeLeft < 0.1){
                //System.err.println("TIME RAN OUT");
                break;
            }*/
        }

        return best_move;

    }

    public int negaMax(GameState gameState, int depth, int alpha, int beta,
            int colour, Deadline deadline, long initialTimeLeft){
        int tmpValue;
        if(gameState.isEOG()){
            return valueEnd(gameState)*colour;
        }
        else if(depth==0){// || (double)deadline.timeUntil()/initialTimeLeft < 0.6){ 
            // If at depth threshold, or less than 5% of time left
            return value(gameState)*colour;
        }
        else{
            Vector<GameState> lNextStates = new Vector<>();
            gameState.findPossibleMoves(lNextStates);
            
            int bestMove=-1000;
            for(GameState childState:lNextStates){
                // Check visited states
                if(repeated_states_checking){
                    String childStateString = getGamestateString(childState);
                    if(isVisited(childStateString)){
                        tmpValue = getVisitedValue(childStateString, colour);
                    }else{
                        // if not in hashMap, compute value and store in hashMap
                        tmpValue=-negaMax(childState, depth-1,-beta,-alpha, -colour, deadline, initialTimeLeft);
                        storeVisited(childStateString,colour,tmpValue);
                    }
                }else{
                    tmpValue=-negaMax(childState, depth-1,-beta,-alpha, -colour, deadline, initialTimeLeft);
                }

                bestMove=Math.max(bestMove, tmpValue);
                alpha=Math.max(alpha, tmpValue);
                if(alpha>=beta){
                    break;
                }
                
            }
            
            return bestMove;
            
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
    public int value(GameState game_state){
        // Returns value of all 76 rows. In total 76*4 cells are checked, right?
        return valueRows(game_state, Constants.CELL_X, Constants.CELL_O)
                + valueColumns(game_state, Constants.CELL_X, Constants.CELL_O)
                + valueLayers(game_state, Constants.CELL_X, Constants.CELL_O)
                + valueDiagonals(game_state, Constants.CELL_X, Constants.CELL_O)
                - valueRows(game_state, Constants.CELL_O, Constants.CELL_X)
                - valueColumns(game_state, Constants.CELL_O, Constants.CELL_X)
                - valueLayers(game_state, Constants.CELL_O, Constants.CELL_X)
                - valueDiagonals(game_state, Constants.CELL_O, Constants.CELL_X);

    }
    public int valueDiagonals(GameState game_state, int player, int opposite){
        // Returns the value of the 24 normal diagonals and the 4 main diagonals
        return valueDiagonalRows(game_state, player, opposite)
            + valueDiagonalColumns(game_state, player, opposite)
            + valueDiagonalLayers(game_state, player, opposite)
            + valueMainDiagonals(game_state, player, opposite);
    }
    public int valueMainDiagonals(GameState game_state, int player, int opposite){
        // Returns the value of the 4 main diagonals
        int tmp_value = 0;
        int tmp_player_cells;
        int tmp_opposite_cells;
        int cell;
        int row;
        int col;
        for(int direction_row = -1; direction_row < 3; direction_row += 2){
            for(int direction_col = -1; direction_col < 3; direction_col += 2){
                // row and col start from top or bottom
                row = (direction_row == -1) ? BOARD_SIZE-1 : 0;
                col = (direction_col == -1) ? BOARD_SIZE-1 : 0;
                tmp_player_cells = 0;
                tmp_opposite_cells = 0;
                for(int layer = 0; layer<BOARD_SIZE; layer++){
                    cell = game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_player_cells++;
                    }else if(cell == opposite){
                        tmp_opposite_cells++;
                    }
                    row += direction_row;
                    col += direction_col;
                }
                if(tmp_opposite_cells == 3 && tmp_player_cells == 0){ 
                    // If opponent can win in next move, row is worth a loss
                    tmp_value += (player == Constants.CELL_X) ? value_loss : value_win;
                }else if(tmp_opposite_cells != 0){
                    // If opponent has a cell on the current row, row is worth 0
                    tmp_value += 0;
                }else{
                    tmp_value += tmp_player_cells;
                    //tmp_value += heuristicMultiplier(tmp_player_cells);
                }
            }
        }    
        return tmp_value;
    }
    public int valueDiagonalRows(GameState game_state, int player, int opposite){
        // Returns value of the 8 normal diagonals over the rows
        int tmp_value = 0;
        int tmp_player_cells;
        int tmp_opposite_cells;
        int cell;
        int layer;
        for(int row=0; row<BOARD_SIZE; row++){
            for(int direction = -1; direction<3; direction += 2){ 
                // 'direction' determines direction of diagonal, up or down
                tmp_player_cells = 0;
                tmp_opposite_cells = 0;
                // Layer starts from top or bottom
                layer = (direction == -1) ? BOARD_SIZE-1 : 0;
                for(int col=0; col<BOARD_SIZE; col++){
                    cell = game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_player_cells++;
                    }else if(cell == opposite){
                        tmp_opposite_cells++;
                    }
                    layer += direction;
                }
                tmp_value += heuristicRowValue(tmp_player_cells, tmp_opposite_cells, player, opposite);
            }

        }
        return tmp_value;
    }
    public int valueDiagonalColumns(GameState game_state, int player, int opposite){
        // Returns value of the 8 normal diagonals over the columns
        int tmp_value = 0;
        int tmp_player_cells;
        int tmp_opposite_cells;
        int cell;
        int layer;
        for(int col=0; col<BOARD_SIZE; col++){
            for(int direction = -1; direction<3; direction += 2){ 
                // 'direction' determines direction of diagonal, up or down
                tmp_player_cells = 0;
                tmp_opposite_cells = 0;
                // Layer starts from top or bottom
                layer = (direction == -1) ? BOARD_SIZE-1: 0;
                for(int row=0; row<BOARD_SIZE; row++){
                    cell = game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_player_cells++;
                    }else if(cell == opposite){
                        tmp_opposite_cells++;
                    }
                    layer += direction;
                }
                tmp_value += heuristicRowValue(tmp_player_cells, tmp_opposite_cells, player, opposite);
            }

        }
        return tmp_value;
    }
    public int valueDiagonalLayers(GameState game_state, int player, int opposite){
        // Returns value of the 8 normal diagonals over the layers
        int tmp_value = 0;
        int tmp_player_cells;
        int tmp_opposite_cells;
        int cell;
        int col;
        for(int layer=0; layer<BOARD_SIZE; layer++){
            for(int direction = -1; direction<3; direction += 2){ 
                // 'direction' determines direction of diagonal, up or down
                tmp_player_cells = 0;
                tmp_opposite_cells = 0;
                // Column starts from top or bottom
                col = (direction == -1) ? BOARD_SIZE-1 : 0;
                for(int row=0; row<BOARD_SIZE; row++){
                    cell = game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_player_cells++;
                    }else if(cell == opposite){
                        tmp_opposite_cells++;
                    }
                    col += direction;
                }
                
                tmp_value += heuristicRowValue(tmp_player_cells, tmp_opposite_cells, player, opposite);
            }

        }
        return tmp_value;
    }

    public int valueRows(GameState game_state, int player, int opposite){
        // Rewritten for 3D
        // Returns value of 16 rows
        int tmp_value = 0;
        int tmp_player_cells;
        int tmp_opposite_cells;
        int cell;
        for(int layer=0; layer<BOARD_SIZE; layer++){
            for(int row=0; row<BOARD_SIZE; row++){
                tmp_player_cells=0;
                tmp_opposite_cells = 0;
                for(int col=0; col<BOARD_SIZE; col++){
                    cell = game_state.at(row,col,layer);
                    if(cell == player){
                        tmp_player_cells++;
                    }else if(cell == opposite){
                        tmp_opposite_cells++;
                    }
                }
                tmp_value += heuristicRowValue(tmp_player_cells, tmp_opposite_cells, player, opposite);
            }
        }
        return tmp_value;
    }

    public int valueColumns(GameState game_state, int player, int opposite){
        //Rewritten for 3D
        // Returns value of 16 rows
        int tmp_value=0;
        int tmp_player_cells;
        int tmp_opposite_cells;
        int cell;
        for(int layer=0; layer<BOARD_SIZE; layer++){
            for(int col=0; col<BOARD_SIZE; col++){
                tmp_player_cells=0;
                tmp_opposite_cells = 0;
                for(int row=0; row<BOARD_SIZE; row++){
                    cell=game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_player_cells++;
                    }else if(cell == opposite){
                        tmp_opposite_cells++;
                    }
                    
                }
                tmp_value += heuristicRowValue(tmp_player_cells, tmp_opposite_cells, player, opposite);
            }
        }
        return tmp_value;
    }

    public int valueLayers(GameState game_state, int player, int opposite){
        // Returns value of 16 rows
        int tmp_value=0;
        int tmp_player_cells;
        int tmp_opposite_cells;
        int cell;
        for(int row=0; row<BOARD_SIZE; row++){
            for(int col=0; col<BOARD_SIZE; col++){
                tmp_player_cells = 0;
                tmp_opposite_cells = 0;
                for(int layer=0; layer<BOARD_SIZE; layer++){
                    cell=game_state.at(row,col,layer);
                    if(cell==player){
                        tmp_player_cells++;
                    }else if(cell == opposite){
                        tmp_opposite_cells++;
                    }
                }
                tmp_value += heuristicRowValue(tmp_player_cells, tmp_opposite_cells, player, opposite);

            }
        }
        return tmp_value;
    }

    public int heuristicRowValue(int player_cells, int opposite_cells, int player, int opposite){
        if(opposite_cells == 3 && player_cells == 0){ 
            // If opponent can win in next move, row is worth a loss
            return (player == Constants.CELL_X) ? value_loss : value_win;
        }else if(opposite_cells != 0){
            // If opponent has a cell on the current row, row is worth 0
            return 0;
        }else{
            return heuristicMultiplier(player_cells);
        }
    }

    public int heuristicMultiplier(int part_value){
        return part_value*part_value; 
        //double pow_factor = 2.5;
        //return (int)Math.ceil(Math.pow(part_value, pow_factor)); 
    }

    public String getGamestateString(GameState gamestate){
        // NEEDS TO IMPLEMENT SYMMETRY BREAKING
        String return_str = "";
        int nr_cells = BOARD_SIZE*BOARD_SIZE*BOARD_SIZE;
        for(int i = 0; i< nr_cells; i++){
            return_str += Integer.toString(gamestate.at(i));
        }
        return return_str;
    }

    public boolean isVisited(String gamestate_string){
        return visited_states.containsKey(gamestate_string);
    }

    public int getVisitedValue(String gamestate_string, int colour){
        // Only call this function if containsVisited has returned true first
        return visited_states.get(gamestate_string)*colour; 
    }

    public void storeVisited(String gamestate_string, int colour, int value){
        visited_states.put(gamestate_string, value*colour);
        return;
    }

    public int nrOccupiedCells(GameState gamestate){
        int nr_cells = BOARD_SIZE*BOARD_SIZE*BOARD_SIZE;
        int cell;
        int occupied_cells = 0;
        for(int idx=0; idx<nr_cells; idx++){
            cell = gamestate.at(idx);
            if(cell != Constants.CELL_EMPTY){
                occupied_cells++;
            }
        }
        return occupied_cells;
    }

    /*public void orderStateList(Vector<GameState> nextStates){
        Collections.sort();
    }*/
}

/*public class stateValuePair{
    public GameState gamestate;
    public int value;
}
*/