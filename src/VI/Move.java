package VI;


public class Move {
	public int piece;
	public Pos to;
	public double value = 0.0;

	public Move(int piece,Pos to){
		this.piece = piece;
		this.to = to;
	} // constructor Move

	public Move(int piece,Pos to, double value){
		this.piece = piece;
		this.to = to;
		this.value = value;
	} // constructor Move

	public String toString(){
		return "" + Board.pieceStr[piece] + " to: " + to + " ,value = " + value;
	} // method toString
} // class Move
