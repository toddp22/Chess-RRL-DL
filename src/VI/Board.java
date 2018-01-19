package VI;

import java.util.ArrayList;

public class Board {
	public static final int SIZE = 4;
	public static final int NUMPIECES =3;
	// Pos [] state = new Pos[NUMPIECES]; // BK,WR,WK
	State state = new State();
	public static final int BK =0;
	public static final int WR =2;
	public static final int WK =1;
	public static final String[] pieceStr = {"BK","WK","WR"};


	 // constructor Board


	public boolean validState(){
		return validState(state);
	} // method validState

	public boolean validState(State state) { // BK,WK,WR
		// on the board.
		// Rooks state is same as bk if captured.
		// This will need to change if we are to add other pieces.
		for (int i=0;i<NUMPIECES;i++)
			if (state.pos[i].row < 0 || state.pos[i].row >= SIZE ||
			    state.pos[i].col < 0 || state.pos[i].col >= SIZE) return false;
		
		
		// unique positions
		if (state.pos[BK].equals(state.pos[WK]) || state.pos[WR].equals(state.pos[WK])) return false; // BK captures WR ok.
	//	if (state.pos[BK].equals(state.pos[WR])) return false; // i
		// kings not adjacent.
		if (Math.abs(state.pos[BK].row - state.pos[WK].row) <= 1 && Math.abs(state.pos[BK].col - state.pos[WK].col) <=1) return false;

		return true;

	} // method validState

	int idx(Pos p){
		return p.row*SIZE + p.col;
	} // method idx

	public boolean inCheck(int piece){
		return inCheck(state,piece);
	} // method inCheck

	public boolean inCheck(State state,int piece) {
		// assuming that this is a valid board position
		// *check by king *// (for testing checkmate etc)
		//if (
		if (Math.abs(state.pos[BK].row - state.pos[WK].row) <= 1 && Math.abs(state.pos[BK].col - state.pos[WK].col) <=1) return true;

		// no other way for WK to be checked
		if (piece == WK) return false;

		// if king captures rook
		if(state.pos[BK].equals(state.pos[WR])) {
			// System.out.println("KxR");
			return false;
		} // if

		// (-1,-1) also means captured
		//if(state.pos[WR].equals (new Pos(-1,-1))) return false;


		// same rank or file as rook
		if (!(state.pos[BK].row == state.pos[WR].row || state.pos[BK].col == state.pos[WR].col)) return false;
		// also need to check that the white king isn't blocking check.

		if (state.pos[BK].row == state.pos[WR].row){ // row check
			if (!(state.pos[WK].row == state.pos[BK].row)) return true; // wk not on same row
				if ((state.pos[WK].col < state.pos[WR].col && state.pos[WK].col < state.pos[BK].col) ||
						(state.pos[WK].col > state.pos[WR].col && state.pos[WK].col > state.pos[BK].col)) return true; // wk not inbetween
		}

		else{ // col check
			if (!(state.pos[WK].col == state.pos[BK].col)) return true; // wk not on same col
				if ((state.pos[WK].row < state.pos[WR].row && state.pos[WK].row < state.pos[BK].row) ||
	 				  (state.pos[WK].row > state.pos[WR].row && state.pos[WK].row > state.pos[BK].row)) return true; // wk not inbetween
		}

		return false;

	} // inCheck

	public boolean checkMate(){
		return checkMate(state);
	} // method checkMate

	public boolean checkMate(State state){
		// if it is white's turn then this is false.
		if (state.turn == State.WHITE) return false;
		else {
			ArrayList<Pos> kmoves = validKMoves(state,BK);
			return inCheck(state,BK) && kmoves.size() == 0;
		}
	} // method checkMate

	public boolean staleMate(){
		return staleMate(state);
	}// method staleMate

	// this only works if state is a valid board position.
	public boolean staleMate(State state){
		if (state.pos[BK].equals(state.pos[WR])) return true;   // only kings left
																// Need to check if kings are adjacent!!
		if (state.turn == State.BLACK){
			ArrayList<Pos> kmoves = validKMoves(state,BK);
			return !inCheck(state,BK) && kmoves.size() == 0;
		}
		else
			return false;
	} // method staleMate

	public ArrayList<Pos> validRookMoves(){
		return validRookMoves(state);
	} // method validRookMoves

	public ArrayList<Move> getMoveList(ArrayList<Pos> pl,int piece){
		ArrayList<Move> moveList = new ArrayList<Move>();
		for(int i=0;i< pl.size();i++){
			moveList.add(new Move(piece,(Pos)pl.get(i)));
		} // for
		return moveList;
	} // method getMoveList

	public ArrayList<Move> getValidMoves(State state,int piece){
		switch (piece) {
			case Board.BK: return getMoveList(validKMoves(state, BK),BK);
			case Board.WK: return getMoveList(validKMoves(state, WK),WK);
			case Board.WR: return getMoveList(validRookMoves(state),WR);
			default: return null;
		} // switch
	}// method getValidMoves


	public ArrayList<Move> getValidMoves(State state){
		ArrayList<Move> al;
		if (state.turn == State.WHITE){
			al = getMoveList(validRookMoves(state),WR);
			al.addAll(getMoveList (validKMoves(state,WK),WK));
		}
		else
			al = getMoveList(validKMoves(state,BK),BK);
		return al;
	} // method getValidMoves

	public ArrayList<Pos> validRookMoves(State state){
			// Rook
		Pos cur = state.pos[WR];
		// Rook can move anywhere along row/col until it hits a piece or an edge.
		// start from cur and inc  and dec until hit edge or piece.

		ArrayList<Pos> moves = new ArrayList<Pos>();  // of positions

		Pos temp = cur.Clone();
		for(temp.row++; temp.row < SIZE;temp.row++){
			if(temp.equals(state.pos[WK])) break; // ran into own piece
			if(temp.equals(state.pos[BK])){ // captured opponents piece
				moves.add(temp.Clone());
				break;
			}

			moves.add(temp.Clone()); // nothing in the way.

		}
		temp = cur.Clone();
		for(temp.row--; temp.row >= 0;temp.row--){
			if(temp.equals(state.pos[WK])) break; // ran into own piece
			if(temp.equals(state.pos[BK])){ // captured opponents piece
				moves.add(temp.Clone());
				break;
			}

			moves.add(temp.Clone()); // nothing in the way.

		}

		temp = cur.Clone();
		for(temp.col++; temp.col < SIZE;temp.col++){
			if(temp.equals(state.pos[WK])) break; // ran into own piece
			if(temp.equals(state.pos[BK])){ // captured opponents piece
				moves.add(temp.Clone());
				break;
			}

			moves.add(temp.Clone()); // nothing in the way.

		}
		temp = cur.Clone();
		for(temp.col--; temp.col >= 0;temp.col--){
			if(temp.equals(state.pos[WK])) break; // ran into own piece
			if(temp.equals(state.pos[BK])){ // captured opponents piece
				moves.add(temp.Clone());
				break;
			}

			moves.add(temp.Clone()); // nothing in the way.

		}

		return moves;
	} // method validRookMoves

	/*
	public ArrayList validKMoves (int piece){
		return validKMoves(state,piece);
	} // method validKMoves
	*/

	//*
	public ArrayList<Pos> validKMoves(State state,int piece){
		Pos cur = state.pos[piece];
		ArrayList<Pos> moves = new ArrayList<Pos>();
		State st = new State();
		st.pos[WR] = state.pos[WR];

		if(piece == BK)	st.pos[WK] = state.pos[WK];
		if(piece == WK)	st.pos[BK] = state.pos[BK];

		Pos temp = cur.Clone();

		for(temp.row=cur.row-1;temp.row <= cur.row+1;temp.row++){ // king in each row
			if (temp.row < 0 || temp.row >=SIZE) continue;
			for(temp.col=cur.col-1;temp.col <= cur.col+1;temp.col++){ // king in each col
				if(temp.equals(cur)) continue;
				if (temp.col < 0 || temp.col >=SIZE) continue;
				st.pos[piece] = temp;
				if (inCheck(st,piece)) continue;
				if (temp.equals(state.pos[BK]) ) continue; // Can't be on top of another king.
				if (piece == WK && temp.equals(state.pos[WR]) ) continue; // Can't be on top of WR
				moves.add(temp.Clone());

			}
		}

		return moves;
	} // method validKMoves
	//*/

/*
	public ArrayList validMoves(State state,int idx){
		if (idx == BK) return validKMoves(state,idx);
		if (idx == WR) return validRookMoves(state);
		if (idx == WK) return validKMoves(state,idx);
		return null;
	}// method validMoves
*/


	public void printBoard(State state){
		printBoard(state,null);
	}// method printBoard

	public void printBoard(Move bestMove){
		printBoard(state,bestMove);
	} // method printBoard


	public void printBoard(State state,Move bestMove){
		Pos p;

		for(int i = 0;i<SIZE;i++) System.out.print(" --");
		System.out.println();
		for(int r=0;r<SIZE;r++){
				System.out.print ("|");
				for(int c=0;c<SIZE;c++){
				p = new Pos(r,c);

				if (state.pos[BK].equals(p))    	System.out.print ("BK");
				else if (state.pos[WR].equals(p))	System.out.print ("WR");
				else if (state.pos[WK].equals(p))	System.out.print ("WK");
				else if (bestMove != null && bestMove.to.equals(p))
					System.out.print("XX");
				else
					System.out.print ("  ");
				System.out.print ("|");
			}
			if (r == 0)
				if(state.turn == State.WHITE)
					System.out.print("White's turn");
				else
					System.out.print("Black's turn");
			if (r == 1)
				if(checkMate(state))
					System.out.print("Checkmate!");
				else if (staleMate(state))
					System.out.print("Stalemate!");
			System.out.println();
		}
		for(int i = 0;i<SIZE;i++) System.out.print(" --");
		System.out.println();

	} // method printBoard


} // class Board
