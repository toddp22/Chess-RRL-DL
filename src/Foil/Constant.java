package Foil;
//import Term;
//import Variable;
import java.io.Serializable;
//import java.util.List;
import java.util.ArrayList;

public class Constant extends Term implements Serializable{
	public static final long serialVersionUID = 0;
	
	public Constant(String name){
		super(name);
	} // constructor val


	/*************************
		Method substitute:
			bind if possible
			Not possible for constants to bind
	*************************/
	public void substitute(Variable v,Term t){
		// do nothing    WHY?
	}// method substitute

	public boolean match(Constant c){
		return equals(c);
	}// method match

	public boolean match(Variable v){
		return v.match(this);
	}// method match


	public boolean bound(){
		return true;    // bound to itself
	} // method bound

	public boolean equals (String s){
		return name.equals(s);
	} // method equals

	public boolean equals (Object o){
		return(o instanceof Constant && ((Constant) o).name.equals(name));  // What is this doing?
	} // method equals

	public Term LGG(Term t,String[] nameList){
		if (equals(t)) return this;
		else return new Variable(nameList);  // returns a new variable with a default name
	} // method LGG

	public Term LGG(Term t,ArrayList<String> nameList){
		if (equals(t)) return this;
		else return new Variable(nameList);  // returns a new variable with a default name
	} // method LGG

	// Need to be careful about naming variables right.
	public Term LGG(Term t){
		String[] nameList = {name,t.name};
		return LGG(t,nameList);
	} // method LGG

	
	/*********************************/
	/* Test Code                     */
	/*********************************/
	
	public static void main(String[] args){
		Constant c1 = new Constant("C");
		Constant c2 = new Constant("C");

		if (c1.match(c2))
			System.out.println("They match.");
		else
			System.out.println("They dont.");
	} // method main

} // class Constant

