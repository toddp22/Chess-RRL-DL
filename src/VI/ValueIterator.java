// Value Iteration
// First cut

package VI;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
//import java.util.Random;

import Foil.DBase;
import Foil.Fact;
//import Foil.FoilImp;
//import Foil.Predicate;

/**/

/********
	Works Good Except:
	Done: Black should have ability to take rook
	Done:	Should not be stalemate unless white's turn (unless rook is gone)
	Done:	Need concept of whose turn it is.
	Done:	Set up values of black turn and white turn separately.
	Done:	run multiple value iterations of black and white
			   (will happen automatically if added to loop).
				Intermediate position value's seem off.
	Maybe:
	Change validMoves procedures to return an ArrayList of moves
 *	Todo : Rook's  position needs to be changed to -1,-1 if captured.
 *		 : Need to include Reward in value function and initialize absorbing states to 0.

********/






public class ValueIterator {
	public static final int SIZE = 4;             // board size
	public static final double initMinVal = 100;         // initial value for minval
	public static final double initMaxVal = -100;        // initial value for maxval
	Board B = new Board();
	double discount = 0.9;
	// public static final int VALUESIZE = SIZE * SIZE;

	//double[][][] value = new double[VALUESIZE][VALUESIZE][VALUESIZE]; // start with 4x4 chess board.
			                															 								// indicies indicate position of BK,WR,WK


	double [][][][][][][] val = new double [2][SIZE][SIZE][SIZE][SIZE][SIZE][SIZE];
									 //  color  bkr   bkc   wrr   wrc   wkr   wkc

	// Need possible moves for white.
	// King has potentially 8,
	// Rook has potentially 6. (2 x SIZE-1)

	// for each valid move for white --
	// generate each move for black, assign a random probability to

	public double value(State state){
		return val[state.turn][state.pos[Board.BK].row][state.pos[Board.BK].col]
							            [state.pos[Board.WR].row][state.pos[Board.WR].col]
							            [state.pos[Board.WK].row][state.pos[Board.WK].col];
	} // method value

	public void setValue(State state, double v){
		val[state.turn][state.pos[Board.BK].row][state.pos[Board.BK].col]
		      			   [state.pos[Board.WR].row][state.pos[Board.WR].col]
			    			   [state.pos[Board.WK].row][state.pos[Board.WK].col] = v;
	} //method setValue


	public Move bestMove(State state){
		State st = new State(state);

		if (st.turn == State.WHITE){
//			for(int i=0;i<3;i++)	st.pos[i] = state.pos[i];
			Move rmove = bestMove(st,Board.WR);
//			for(int i=0;i<3;i++)	st.pos[i] = state.pos[i];
			Move kmove = bestMove(st,Board.WK);
			if (rmove.value >= kmove.value) return rmove;
			else return kmove;
		}
		else
			return bestMove(st,Board.BK);

	} // bestMove


	public State makeMove(State state, Move move)
	{
		if (state == null || move == null) return null;
		State st = new State(state);
		st.pos[move.piece] = move.to; // need to check this
		st.changeTurn();

		return st;
	}


	// This function gives the reward from being in state s and making move m
	// This will eventually be done with captures or positional advantage
	// but now it's just 0;

	

	public double Reward(State s, Move m,Move om)
	{
		
		State st = new State(s);

		if (st.turn == State.WHITE && m == null) return 0.0;

		if (st.turn == State.WHITE)
		{
			State ost = makeMove(st, m);
			if (B.staleMate(ost)) return -1.0;  // stalemate and checkmate needs to be calculated on
			if (B.checkMate(ost)) return 1.0;	// black's turn 
			if (om != null)
			{
				State nst = makeMove(ost, om);
				if (B.staleMate(nst)) return -1.0;
				if (B.checkMate(nst)) return 1.0;
			}
		}
		else
		{
			if (B.staleMate(st)) return -1.0;
			if (B.checkMate(st)) return 1.0;
		}
		

		return 0;
	}


	public Move miniMaxMove(State state)
	{
		double maxVal = initMaxVal;      // value of current player's best move
		double minVal = initMinVal;		 // value of opponents best move
		double val;						 // working value
		int idx = -1;					 // index of current player's best move

		State st = new State(state);	 // working state (to be sure that state doesn't get damaged)
		State ost;						 // oponent's state
		State stprime;					 // s' our next state

		ArrayList<Move> ml = B.getValidMoves(st);		// move list
		for (int i = 0; i < ml.size(); i++)
		{
			Move m = (Move)ml.get(i);			// current move
			ost = makeMove(st, m);				// opponent's state

			if (st.turn == State.WHITE)
				minVal = initMinVal;			// reinitialize inner values.
			else
				maxVal = initMaxVal;
			ArrayList<Move> ol = B.getValidMoves(ost);
			for (int j = 0; j < ol.size(); j++)
			{
				Move om = (Move)ol.get(j);		// opponent's next move
				stprime = makeMove(ost, om);	// our next state
				//val = Reward(st,m,om) + discount * value(stprime);
				val = discount * value(stprime); // need to change this later
												 // above will work if initialize values of 
												 // absorbing states to 0.
				if (st.turn == State.WHITE)
				{
					if (val < minVal) minVal = val;
				}
				else
					if (val > maxVal) maxVal = val;

			}


			if (ol.size() == 0)            // if there are no moves we are in an absorbing state
										   // set values based solely on rewards.
			{
				if (st.turn == State.WHITE)
					minVal = discount * Reward(st, m, null);  // this isn't exactly correct
				else                                          // we should award the exact reward
					maxVal = discount * Reward(st, m, null);  // not a discounted reward at this state
															  // but since we are rewarded for being
															  // in the absorbing state and not for
															  // transitioning into it, this is the value.
			}


			// at this point we want to take the maximum of the minVals
			if (st.turn == State.WHITE)
			{
				if (minVal > maxVal) 
				{
					maxVal = minVal;
					idx = i;
				}
			}
			else
				if (maxVal < minVal)   // check this logic. Seems ok.
				{
					minVal = maxVal;
					idx = i;
				}
				
		} // end for

		if (ml.size() == 0)
		{
			// retMove.value = Reward(st, null, null); // if we are in an absorbing state
			// can't return a best move
			return null;
		}

		Move retMove = (Move)ml.get(idx);
		if (st.turn == State.WHITE)
			retMove.value = maxVal;
		else
			retMove.value = minVal;



		return retMove;
	} // end miniMaxMove

	/*public Move miniMaxMove(State state){
		    double maxVal = initMaxVal;      // value of current player's best move
			double minVal = initMinVal;		 // value of opponents best move
			double val;						
			int idx = -1;					 // index of current player's best move

			State st = new State(state);						 // working state (to be sure that state doesn't get damaged)

			ArrayList ml = B.getValidMoves(st); // move list

		  //System.out.println("Valid Moves: " + ml);
			for(int i=0;i<ml.size();i++){
				Move m = (Move)ml.get(i);
				st = makeMove(state, m);
				val = value(st);                  // get value of current state (in case no moves for opponent)
					                          	  	// 		    (should never happen cuz checked earlier)

				Move bestOpponentMove = bestMove(st);
				if(state.turn == State.WHITE)
					minVal = bestOpponentMove.value;
				else
					maxVal = bestOpponentMove.value;

				if (bestOpponentMove.to != null)
					if (state.turn == State.WHITE)
						val = minVal;
					else
						val = maxVal;

			//	System.out.println("Value: " + val);
				if (state.turn == State.WHITE)
				{
					if (val > maxVal)
					{   // find value of best white move, given opponent's best move
						// mini-max value
						maxVal = val;
						idx = i;
					} // if
				}
				else // state.turn == State.BLACK
				{
					if (val < minVal)
					{   // find value of best black move, given opponent's best move
						// mini-max value
						minVal = val;
						idx = i;
					} // if
				}
			}	 // for

			 if (idx == -1)
				return (Move) null;
			Move move = (Move)ml.get(idx);
			if (state.turn == State.WHITE)
				move.value = maxVal;
			else
				move.value = minVal;
			return move;

	} // method miniMaxMove
	*/
	public Move bestMove(State state,int piece){

		Move move;
		State s = new State(state);
		int idx = -1;
		double maxVal = initMaxVal;
		double minVal = initMinVal;
		double val;

		ArrayList<Move> ml = B.getValidMoves(s,piece);

		for (int i=0;i<ml.size();i++){
			move = (Move)ml.get(i);
			s.pos[move.piece] = move.to;
			s.changeTurn();
			val = value(s);

			//s.changeTurn();   /// why is this here???
			if (state.turn == State.WHITE){
				if(val > maxVal){	maxVal = val;	idx = i;} // if
			} // if
			else{
				if(val < minVal){	minVal = val; idx = i;} // if
			} // else
		} // for

		if (idx == -1) move = new Move(-1,null);
		else         	move = (Move)ml.get(idx);

		if(state.turn == State.WHITE)	
			move.value = maxVal;
		else move.value = minVal;
		return move;

	} // method bestMove

	public void Iterate(){
		//System.out.println("Iterating");
		/// assumes that reward values are already specified
		State st = new State();
		//double maxVal = initMaxVal;
		double val = 0.0;
 		//double oldVal = 0.0;
		//double valueDiff = 0.0;
		//double valueDiffSum = 0.0;

		for(int trn=0;trn<2;trn++)
		for(int bkr=0;bkr<SIZE;bkr++)
		for(int bkc=0;bkc<SIZE;bkc++)

		for(int wkr=0;wkr<SIZE;wkr++)
		for(int wkc=0;wkc<SIZE;wkc++)

		for(int wrr=0;wrr<SIZE;wrr++)
		for(int wrc=0;wrc<SIZE;wrc++){

			st.pos[Board.BK] = new Pos(bkr,bkc);
			st.pos[Board.WR] = new Pos(wrr,wrc);
			st.pos[Board.WK] = new Pos(wkr,wkc);
			st.turn = trn;

			// Do we need to check if stalemate is a valid position??
			if (B.checkMate(st)) setValue(st, 1);
			else if (B.staleMate(st)) setValue(st, -1);

			if (!B.validState(st)) continue;
			if (st.turn == State.WHITE && B.inCheck(st,Board.BK)) continue;
			val = value(st);
			if (val == 1.0 || val == -1.0) continue;

	    // B.printBoard(st);
			// System.out.println("value is " + val);

			Move bestMove = miniMaxMove(st);    // get bestMove
			val = bestMove.value; 			    // get value of best move
	//		bestMove = miniMaxMove(st);			// do again for debugging purposes.
	//		maxVal *= discount;                 // discounting has been done in miniMaxMove

			//oldVal = value(st);
			setValue(st,val);                // set the new value
//			valueDiff = maxVal - oldVal;
//			valueDiffSum += Math.abs(valueDiff);

			if(bestMove.value != 0.0){
		   //   B.printBoard(st);
			 //   System.out.println("Value set to: " + maxVal);
	//		  System.out.println("Value difference: " + valueDiff);
			} // if
		}// for
		//System.out.println("Total Value difference: " + valueDiffSum);

	} // method Iterate

	public void print(String fname) throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter(fname));
		State st = new State();
		int val;

		for(int trn=0;trn<2;trn++)
		for(int bkr=0;bkr<SIZE;bkr++)
		for(int bkc=0;bkc<SIZE;bkc++)

		for(int wkr=0;wkr<SIZE;wkr++)
		for(int wkc=0;wkc<SIZE;wkc++)

		for(int wrr=0;wrr<SIZE;wrr++)
			for (int wrc = 0; wrc < SIZE; wrc++)
			{

				st.pos[Board.BK] = new Pos(bkr, bkc);
				st.pos[Board.WR] = new Pos(wrr, wrc);
				st.pos[Board.WK] = new Pos(wkr, wkc);
				st.turn = trn;

				if (!B.validState(st)) continue;
				if (st.turn == State.WHITE && B.inCheck(st, Board.BK)) continue;
				val = (int)(value(st) * 10);
				out.println("Value(" + trn + ", " + bkr + ", " + bkc + ", " + wkr + ", " + 
									               wkc + ", " + wrr + ", " + wrc + ", " + val + ')');

			}
			out.close();
	}

	public DBase getDatabase(){
		DBase d = new DBase();
		State st = new State();
		double val;
		Fact f;
		//Random generator = new Random();
		st.pos[1] = new Pos(-100,-100);
		st.pos[2] = new Pos(-100,-100);

		for(int trn=0;trn<2;trn++)
		for(int bkr=0;bkr<SIZE;bkr++)
		for(int bkc=0;bkc<SIZE;bkc++)
		//System.out.println("BKRow = " + bkr + " BKC = " + bkc);
		//for(int wkr=0;wkr<SIZE;wkr++)
		//for(int wkc=0;wkc<SIZE;wkc++)

		//for(int wrr=0;wrr<SIZE;wrr++)
		//for(int wrc=0;wrc<SIZE;wrc++)
		{
			st.pos[Board.BK] = new Pos(bkr,bkc);
			//st.pos[Board.WR] = new Pos(wrr,wrc);
			//st.pos[Board.WK] = new Pos(wkr,wkc);
			st.turn = trn;

			//if (!B.validState(st)) continue;
			if (st.turn == State.WHITE && B.inCheck(st,Board.BK)) continue;
			// only for inCheck
			if (st.turn == State.WHITE) continue;
/*
			val = value(st);

			//if (val == -1 || val == 1){
				int[] params = {trn,bkr,bkc,wkr,wkc,wrr,wrc,(int)(val*10)};

				f = new Fact("Value",params,true);
			// System.out.println("Adding fact: " + f.toString());


			if (B.inCheck(st,B.BK))
				val = 1.0;
			else
				val = 0.0;
*/
			ArrayList<Pos> moves = B.validKMoves(st,Board.BK);
			System.out.println(moves);
			for (int r = 0;r<SIZE;r++){
				for (int c = 0;c<SIZE;c++){
					if (moves.contains(new Pos(r,c)))	val = 1.0;
					else	val = 0.0;
					//int[] params = {trn,bkr,bkc,r,c,(int)(val*10)};
					int[] params = {bkr,bkc,r,c};
					//int[] params = {bkr,bkc,wkr,wkc,r,c};
					//int[] params = {bkr,bkc,wkr,wkc,wrr,wrc,r,c};
					// int[] params = {trn,bkr,bkc,wkr,wkc,wrr,wrc,r,c,(int)(val*10)};
					if(val == 1.0)
						f = new Fact("KMove",params,true);
					else
						f = new Fact("KMove",params,false);

		//				if (val == 1.0)
					d.insert(f);
		//				else {
		//					if(generator.nextFloat() < 0.1) d.addFact(f);
		//				}
				} // for
			} // for

			//} // if
		} // for
		//} // for
		return d;
	} // method getDatabase;

	public ValueIterator() {
		// Pos[] st = new Pos[Board.NUMPIECES];
		State st = new State();
		// Initialize Values

		for(int trn=0;trn<2;trn++)
		for(int bkr=0;bkr<SIZE;bkr++)
		for(int bkc=0;bkc<SIZE;bkc++)

		for(int wkr=0;wkr<SIZE;wkr++)
		for(int wkc=0;wkc<SIZE;wkc++)

		for(int wrr=0;wrr<SIZE;wrr++)
		for(int wrc=0;wrc<SIZE;wrc++){

			st.pos[Board.BK] = new Pos(bkr,bkc);
			st.pos[Board.WR] = new Pos(wrr,wrc);
			st.pos[Board.WK] = new Pos(wkr,wkc);
			st.turn = trn;

			if      (B.checkMate(st)) setValue(st,1);
			else if (B.staleMate(st)) setValue(st,-1);
			else              				setValue(st,0);


		}// for

	} // constructor ValueIterator


	public static void main(String[] args) throws Exception{
		ValueIterator VI = new ValueIterator();

		for(int v=0;v<10;v++){
			System.out.println("\nRound: " + (v+1));
			VI.Iterate();
		}

		System.out.println("Generating database");
		DBase D = VI.getDatabase();
		System.out.println("Dumping database");
		D.dump("KMoveKR.db");
		//System.out.println(D);
		//String pname = "KMoveKR";
		//Predicate target = new Predicate (pname,8);
		System.out.println("Calculating FOL");
		//FoilImp F = new FoilImp(target,D,true);


/*
		int checkcount = 0;
		int validcount = 0;
		int invalidcount = 0;
		int matecount = 0;
		int stalematecount = 0;
		int total = 0;
		Move best;

		for(int bkr=0;bkr<VI.SIZE;bkr++)
		for(int bkc=0;bkc<VI.SIZE;bkc++)

		for(int wkr=0;wkr<VI.SIZE;wkr++)
		for(int wkc=0;wkc<VI.SIZE;wkc++)

		for(int wrr=0;wrr<VI.SIZE;wrr++)
		for(int wrc=0;wrc<VI.SIZE;wrc++){

			VI.B.state.pos[Board.BK] = new Pos(bkr,bkc);
			VI.B.state.pos[Board.WR] = new Pos(wrr,wrc);
			VI.B.state.pos[Board.WK] = new Pos(wkr,wkc);
			VI.B.state.turn = State.WHITE;
			total ++;

			if (VI.B.validState()){
				validcount++;

				if(VI.B.inCheck(Board.BK)) {
					checkcount++;
					// System.out.println ("Check!");
					 // System.out.println(VI.B.validRookMoves());
					if(VI.B.checkMate()) {
						matecount++;
					//VI.B.printBoard();
					//System.out.println("Value: " + VI.value(VI.B.state));
					}
				} // checks
				else if(VI.B.staleMate()) {
					stalematecount++;
					//VI.B.printBoard();
				} // stalemates
				else{
//					best = VI.bestMove(VI.B.state);
					best = VI.miniMaxMove(VI.B.state);
					VI.B.printBoard(best);
					System.out.println("Value: " + VI.value(VI.B.state));
					System.out.println("Best Move: " + best);
					while (VI.B.state.turn != State.WHITE || (best.value != 1.0 && best.value != -1.0)){
						VI.B.state.pos[best.piece] = best.to;
						VI.B.state.changeTurn();
						// System.out.println("Best response: " + VI.bestMove(VI.B.state,VI.B.BK));
						System.out.println("Best response: " + VI.bestMove(VI.B.state));
						System.out.println("MiniMax response: " + VI.miniMaxMove(VI.B.state));
						best = VI.miniMaxMove(VI.B.state);
						VI.B.printBoard(best);
						System.out.println("Value: " + VI.value(VI.B.state));
						System.out.println("Best Move: " + best);
					}
					VI.B.state.pos[best.piece] = best.to;
					VI.B.state.changeTurn();
//					best = VI.miniMaxMove(VI.B.state);
					VI.B.printBoard((Move)null);
					System.out.println("---------------------------------------------------------");
				}

				// VI.B.printBoard();
//			if (VI.B.validState()) System.out.println ("valid");
//			else System.out.println("not valid");
//			if (VI.B.inCheck()) System.out.println ("in check");
//			else System.out.println("not in check");
			} // if valid
			else {
				invalidcount++;
				//				VI.B.printBoard();
			}
		} // for

		System.out.println("There are " + validcount + " valid states.");
		System.out.println("There are " + checkcount + " valid checks.");
		System.out.println("There are " + matecount + " valid mates.");
		System.out.println("There are " + stalematecount + " valid stalemates.");
		System.out.println("There are " + invalidcount + " invalid states.");
		System.out.println("There are " + total + " total states.");

		*/

	} // method main

} // class ValueIterator
