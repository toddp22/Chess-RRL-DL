package Foil;
//import Term;
//import java.util.List;
import java.util.ArrayList;

class Function extends Term {
	public static final long serialVersionUID = 0;	
	public final Term[] args;
	private DBase D = null;

	public Function(String name,int arity){
		super(name);
		args = new Variable[arity];
	} // constructor Function

	public boolean bound(){
		for(int i=0;i<args.length;i++)
			if(!args[i].bound()) return true;
		return false;
	} // method bound

	/***********************************
		setDatabase: D

		Used to assign this function to a database
		for the purpose of evaluating the function.
	***********************************/

	public void setDatabase(DBase D){
		this.D = D;
	} // method setDatabase

	/**********************************
		Substitute instances of v
		for t
	**********************************/
	public void substitute (Variable v, Term t){
		for (int i = 0;i< args.length;i++){
			args[i].substitute(v,t);
		} // for
	} // method Substitute


	/****************************************
		public boolean match(Constant c):
			Returns whether or not this function can be matched to

		Need a way of matching functions to functions
		  All we have is constants and variables.
	****************************************/

	public boolean match(Constant c){
		// Double check this method.
		// This function (if fully bound could equal the constant c as defined in database D);

//		if (D != null)
//		   return c.equals(D.evaluate(this));

		return false;
	} // method Match
	
	public boolean match (Variable v){
		if(v.bound()) return match(v.binding);
		else return true;
	} // method match

	/*
	public Constant evaluate (DBase D){
		return D.evaluate(this);
	} // method evaluate
	*/

	public Term LGG(Term t,String[] namelist){
		if (equals(t)) return this; // this may not be correct
		else return new Variable(namelist);
	} // method LGG

	public Term LGG(Term t,ArrayList<String> namelist){
		if (equals(t)) return this; // this may not be correct
		else return new Variable(namelist);
	} // method LGG

	public Term LGG(Term t){
		String[] names = {name,t.name};
		return LGG(t,names);
	} // LGG
} // class Function

