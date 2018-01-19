package Foil;
// class Term

// superclass of
// variable
// constant
// and

/***********************************
Changes:
Oct 29:
	Changed equals to be two methods
	one for Strings, one for other terms
Dec 11:
	Changed LGG to include List type namelist
***********************************/

import java.io.Serializable;
//import java.util.List;
import java.util.ArrayList;

/**
	abstract class Term

	This is the base class for all single variable constructs.
*/

abstract class Term implements Serializable{
	public String name;
	public static final long serialVersionUID = 0;

	/**
		Constructor
	*/
	public Term(String name){
		this.name = name;
	} // constructor Term

	/**
		Method: equals
			This is a strict form of equals
			Notably unbound variables of different names
			do not equal each other i.e. v != x
			Also unbound variables do not equal constants.
	*/



	/**
		equals:
		This returns true if the name of the term equals
		the passed in string
	*/

	public boolean equals(String s){
		return name.equals(s);
	} // method equals (String)

	// this doesn't work for bound variables
	/*
	public boolean equals(object t){
			return this.getClass() == t.getClass() &&	name.equals(t.name);
	} // method equals
	*/


	/**
	*/

	public String toString() {
		return name;
	} // method toString

	public int hashCode(){
		return name.hashCode();
	} // method hashCode


	/**
		Method match:
			This is a looser form of equality
			Constants match each other
			Unbound variables match constants

	*/

	public abstract boolean match(Constant c);
	public abstract boolean match(Variable v);
	public abstract boolean bound();
	public void bind(Constant c){
	} // method bind
	public void release(){
	} // method release

	/**
		Substutute occurances of v with t
		Not used.
	*/
	public abstract void substitute (Variable v,Term t);
	/**
		LGG
			Least General Generalization
	*/
	public abstract Term LGG(Term t);
	/**
		LGG:
			NameList: Used to define variables that are already in use.
			If a variable is returned, then it will be unique with
			respect to NameList
	*/
	public abstract Term LGG(Term t,String[] NameList);
	public abstract Term LGG(Term t,ArrayList<String> NameList);

} // class term

