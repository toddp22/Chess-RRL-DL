package VI;

import VI.Pos;
import Foil.Fact;


public class State {
	public final static int WHITE = 0;
	public final static int BLACK = 1;
	public final static int NumPieces = 3;
	Pos[] pos = new Pos[NumPieces];
	int turn = WHITE;

	public State(Fact f, int turn){
		this.turn = turn;
		for(int i=0;i<NumPieces;i++)
			pos[i] = new Pos(Integer.parseInt(f.args[2*i].name),Integer.parseInt(f.args[2*i+1].name));
	}

	State(Pos[] pos,int turn){
		this.pos = pos;
		this.turn = turn;
	} // constructor State

	State() {

	} // constructor State

	State (State st) {
		for (int i=0;i<NumPieces;i++)
			pos[i] = new Pos(st.pos[i]);
		turn = st.turn;
	} // constructor State


	void changeTurn(){
		if (turn == 0) turn = 1;
		else turn = 0;
	} // method changeTurn

	public String toString(){
		String str ="";
		for(int i=0;i<NumPieces;i++)
			str += pos[i].toString();
		str += " turn: ";
		str += turn==WHITE?"WHITE":"BLACK";
		return str;
	}

} // class State
