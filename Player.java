import java.util.*;


public class Player {
    int value_loss=-1000;
    int value_win=1000;
    int value_tie=0;
    int boardSize=4;

    public GameState play(final GameState gameState, final Deadline deadline) {
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        if (nextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(gameState, new Move());
        }
                    
        int best_value_move=value_loss;
        int tmp_value_move;
        GameState best_move=nextStates.firstElement();
        for(GameState tmp_state: nextStates){
            if(gameState.getNextPlayer()==Constants.CELL_O){
                tmp_value_move=-negaMax(tmp_state, 5,value_loss, value_win, -1);
            }
            else{
               tmp_value_move=negaMax(tmp_state, 5,value_loss, value_win, -1); 
            }
            if( tmp_value_move>=best_value_move){
                if(tmp_state.isXWin()){
                    best_move=tmp_state;
                    break;
                }
                best_value_move=tmp_value_move;
                best_move=tmp_state;

            }
        }
                    
        return best_move;
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
        return valueRows(game_state, Constants.CELL_X)
            + valueColumns(game_state, Constants.CELL_X)
                +valueDiagonals(game_state, Constants.CELL_X)
                -valueRows(game_state, Constants.CELL_O)
                -valueColumns(game_state, Constants.CELL_O)
                -valueDiagonals(game_state, Constants.CELL_O);
    }
    /**
    public int valueDiagonals(GameState game_state, int player){
        return valueDiagonal(game_state, player, 0,-1)+
                valueDiagonal(game_state, player, 3,1);
    }
    * */
    public int valueDiagonals(GameState game_state, int player){
        // 1 for go up and -1 for go down
        int value_diagonals=0;
        int tmp_value=0;
        int cell;
        for(int index=0;index<boardSize; index++){
            cell=game_state.at(index, index);
            if(cell==player){
                tmp_value++;
            }
            else if(cell!= Constants.CELL_EMPTY){
                tmp_value=0;
                break;
            }
        }
        value_diagonals+= tmp_value*tmp_value;
        tmp_value=0;
        for(int index=boardSize-1;index>-1; index--){
            cell=game_state.at(index, index);
            if(cell==player){
                tmp_value++;
            }
            else if(cell!= Constants.CELL_EMPTY){
                tmp_value=0;
                break;
            }
        }
        value_diagonals+= tmp_value*tmp_value;
        return value_diagonals;
    }
    public int valueRows(GameState game_state, int player){
        int tmp_value=0;
        int tmp_value_row;
        int cell;
        for(int row=0; row<boardSize;row++){
            tmp_value_row=0;
            for(int col=0;col<boardSize;col++){
                cell=game_state.at(row, col);
                if(cell==player){
                    tmp_value_row++;
                }
                else if(cell!=Constants.CELL_EMPTY){
                    tmp_value_row=0;
                    break; 
                }
                
            }
            tmp_value+=tmp_value_row*tmp_value_row;
        }
        return tmp_value;
    }
    public int valueColumns(GameState game_state, int player){
        int tmp_value=0;
        int tmp_value_column;
        int cell;
        for(int col=0;col<boardSize;col++){
            tmp_value_column=0;
            for(int row=0;row<boardSize;row++){
                cell=game_state.at(row, col);
                if(cell==player){
                    tmp_value_column++;
                }
                else if(cell!=Constants.CELL_EMPTY){
                    tmp_value_column=0;
                    break;
                }
                
            }
            tmp_value+=tmp_value_column*tmp_value_column;

        }
        return tmp_value;
    }
    public int negaMax(GameState gameState, int depth, int alpha, int beta,
            int colour){
        int tmpValue;
        if(gameState.isEOG()){
            return valueEnd(gameState)*colour;
        }
        else if(depth==0){
            return value(gameState)*colour;
        }
        else{
            Vector<GameState> lNextStates = new Vector<>();
            gameState.findPossibleMoves(lNextStates);
            
            int bestMove=-1000;
            for(GameState childState:lNextStates){
                tmpValue=-negaMax(childState, depth-1,-beta,-alpha, -colour);
                bestMove=Math.max(bestMove, tmpValue);
                alpha=Math.max(alpha, tmpValue);
                if(alpha>=beta){
                    break;
                }
                
            }
            
            return bestMove;
            
        }
    }



    
}
