package Foil;

import java.util.ArrayList;

interface Pred {
	public boolean match(Fact f);
	public boolean Special();
	public String name();
	public int argCount();
	public void release();
	public Pred Clone();
	public void addTerm(Term t);
	public Term arg(int i);
	public boolean containsVarFrom(Pred p);
	public void negate();
	public boolean not();
	public void bind(Fact f);
	public void setVarList(Variable[] varList);
	public Variable[] getVars(Variable[] varList);
	public ArrayList<Pred> permute(ArrayList<Variable> varList);


}