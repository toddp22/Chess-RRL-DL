package Foil;
import java.io.Serializable;

interface Constants{
	final static int ROOK = 0;
	final static int KING = 1;

	final static int WHITE = 0;
	final static int BLACK = 1;

	final static int ROW = 0;
	final static int COL = 1;

	final static int COLOR = 0;
	final static int PIECE = 1;
	final static int LOC = 2;

	final static String[] constString= {"Color","Piece","Location"};


	final static String[][] facString= {{"White","Black"},
										{"Rook","King"},
							  			{"Row","Col"}};

	final static String[][] sfacString= {{"W","B"},
							  			{"R","K"},
							  			{"R","C"}};

} // Consts

class VariableType implements Constants, Serializable{
	public static final long serialVersionUID = 0;	
	private int []factor;   // This is a list of factors similar to: [BLACK,KING,ROW]
	private boolean shortName = true; // true if use shortend names

	// Check to see if we need to make a seperate copy of this.
	public VariableType(int [] factor){
		this.factor = factor;
	} // variableType

	// copy constructor.
	public VariableType(VariableType vt){
		factor = new int[vt.factor.length];
		for(int i=0;i<factor.length;i++)
			factor[i] = vt.factor[i];
			shortName = vt.shortName;
	} // constructor VariableType


	/**

		Checks to see if this and vt are compatible they both
		have factor f -- where f is a factor

	*/
	public boolean compatible (VariableType vt, int f){

		return vt.factor[f] == factor[f];

	} // compatible

	public VariableType(int c,int p,int l){
		factor= new int[3];
		factor[COLOR] = c;
		factor[PIECE] = p;
		factor[LOC] = l;
	} // constructor VariableType

	public void setShortName(boolean val){
		shortName = val;
	} // method setShortName

	/**
		relaxedVT: returns a new VariableType without
		the constraint f.
	*/

	public boolean equals(VariableType vt){
		for(int i=0;i<factor.length;i++)
			if (factor[i] != vt.factor[i]) return false;
		// return piece == vt.piece && color == vt.color && loc == vt.loc;
		return true;
	} // boolean equals:VariableType

	public boolean equals(Object o){
		if (o instanceof VariableType)
			return equals((VariableType)o);
		else
			return false;
	} // boolean equals: Object

	public String toStrWithout(int fac){
		String retStr = "";
		for(int i=0;i<factor.length;i++){
			if (shortName){
                if(i!= fac) retStr += sfacString[i][factor[i]];
            }else{
                if (i != fac) retStr += facString[i][factor[i]];
				if (i < factor.length-1) retStr += " ";
			} // else
		} // for each factor
		return retStr;
	} // method commonFactor

	public String toString(){
		String retStr = "";
		for (int i=0;i<factor.length;i++){
			if (shortName) retStr += sfacString[i][factor[i]];
			else{
				retStr += facString[i][factor[i]];
				if (i < factor.length-1) retStr += " ";
			} // else
		} // for

		return retStr;
	} // method toString
} // class VariableType