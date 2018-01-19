package VI;
import VI.Pos;

public class Pos {
	public int row=0;
	public int col=0;

	public Pos (int row,int col){
		this.row = row;
		this.col = col;
	} // constructor Pos

	public Pos (Pos p){
		this.row = p.row;
		this.col = p.col;
	} // constructor Pos

	public boolean equals(Pos p){
		return p.row == row && p.col == col;
	} // method equals

	public boolean equals(Object o){
		if(o instanceof Pos)
		return equals((Pos)o);
		else return false;
	} // method equals

	public Pos Clone(){
		return new Pos(row,col);
	}// method Clone

	public String toString(){
		return "(" + row + "," + col + ")";
	} // method toString
} // class Pos

