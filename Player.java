import java.util.*;


public class Player {
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
            int best_value_move=-1;
            int tmp_value_move;
            GameState best_move=nextStates.firstElement();
            for(GameState tmp_state: nextStates){ 
                tmp_value_move=searchMinMax(tmp_state, Constants.CELL_O, 0); 
                    //System.err.println(tmp_state.toString(Constants.CELL_O));
                    //System.err.println(tmp_value_move);
                    //System.err.println(end(tmp_state));
                if( tmp_value_move>best_value_move){
                    best_value_move=tmp_value_move;
                    best_move=tmp_state;

                }
            }
                    
            return best_move;
        }

        else{
            Random random = new Random();
            GameState move=nextStates.elementAt(random.nextInt(nextStates.size()));
            return move;
        }
    }

    public int searchMinMax(GameState game_state, int player, int depth){
        Vector<GameState> nextStates = new Vector<>();
        game_state.findPossibleMoves(nextStates);
        int best_move;

        if(game_state.isOWin()){
            return -1;
        }
        else if(game_state.isXWin()){
            return 1;
        }
        else if(depth==3){
            return 0;
        }
        else{
        
        if(Constants.CELL_X==player){
            best_move=-1;
            for(GameState state:nextStates){
                int move = searchMinMax(state, Constants.CELL_O, depth+1);
                best_move=Math.max(move, best_move);
            }
            return best_move;
        }
        else{
            best_move=1;
            for(GameState state:nextStates){
                int move = searchMinMax(state, Constants.CELL_X, depth+1);
                best_move=Math.min(move, best_move);
            }
            return best_move;
            
        }
        }

    }
    public boolean end(GameState game_state){
        if(game_state.isOWin()){
            return true;
        }
        else return game_state.isXWin();
           
    }
    
    
    public int value(GameState game_state){
        if(game_state.isXWin()){
            return 1;
        }
        else{
            return -1;
        }



    
}
