import java.util.*;


public class Player {
    int value_loss=-1000;
    int value_win=1000;
    int value_tie=-100;
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
                        Constants.CELL_O, 5, value_loss,value_win);
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
        
        if(Constants.CELL_X==player){
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
        return valueRows(game_state, player)
            + valueColumns(game_state, player)
                +valueDiagonals(game_state, player);
    }
    public int valueDiagonals(GameState game_state, int player){
        return valueDiagonal(game_state, player, 0,-1)+
                valueDiagonal(game_state, player, 3,1);
    }
    public int valueDiagonal(GameState game_state, int player,
            int start_row, int up_or_down){
        // 1 for go up and -1 for go down
        int tmp_value_diagonal=0;
        int col=0;
        int cell=game_state.at(start_row,col);
        while(cell!=Constants.CELL_INVALID){
            if(cell==player){
                tmp_value_diagonal++;
            }
            else if(cell== Constants.CELL_EMPTY){}  // Why not include this in the next else?
            else{
                tmp_value_diagonal=0;
                break;
            }
            col++;
            start_row+=up_or_down;
            cell=game_state.at(start_row,col);
        }
        return tmp_value_diagonal*tmp_value_diagonal; // Maybe generalize to Math.pow(tmp_value_diagonal, some_factor).
    }
    public int valueRows(GameState game_state, int player){
        int col;
        int row=0;
        int tmp_value=0;
        int tmp_value_row;
        int cell=game_state.at(0, 0);
        while(cell!= Constants.CELL_INVALID){
            tmp_value_row=0;
            col=0;
            while(cell!=Constants.CELL_INVALID){
                if(cell==player){
                    tmp_value_row++;
                }
                else if(cell==Constants.CELL_EMPTY){}
                else{
                    tmp_value_row=0;
                    break; 
                }
                col++;
                cell=game_state.at(row, col); // weird to update cell in final step of loop, but w/e
                
            }
            tmp_value+=tmp_value_row*tmp_value_row; // Generalize with Math.pow
            row++;
            cell=game_state.at(row, 0);
        }
        return tmp_value;
    }
    public int valueColumns(GameState game_state, int player){
        int col=0;
        int row;
        int tmp_value=0;
        int tmp_value_column;
        int cell=game_state.at(0, 0);
        while(cell!= Constants.CELL_INVALID){
            tmp_value_column=0;
            row=0;
            while(cell!=Constants.CELL_INVALID){
                if(cell==player){
                    tmp_value_column++;
                }
                else if(cell==Constants.CELL_EMPTY){
                }
                else{
                    tmp_value_column=0;
                    break;
                }
                row++;
                cell=game_state.at(row, col);
                
            }
            tmp_value+=tmp_value_column*tmp_value_column; // generalize with math.pow
            col++;
            cell=game_state.at(0, col);
        }
        return tmp_value;
    }



    
}
