package Foil;
// class Predicate
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
//import java.util.List;



class UnboundArgumentException extends Exception{
	public static final long serialVersionUID = 0;
} // class UnboundArgumentException

public class Predicate implements Serializable, Pred, Constants{
	public static final String[] specialNames = {"Equal","Corner","Edge","Adjacent","Distance","DuplicateRookRow"};
	public static final long serialVersionUID = 0;

	public Term[] args;
	public String name;
	public boolean not = false;
	public int argCount = 0;
	public boolean[] boundByMe;  // indicates that this term was bound by this object
	                             // does not necessarily match all bound terms.
	public int depth = -1; // not defined at construction. (depends on clause it is placed in)
	public VarConstraint[] constraints = new VarConstraint[0];
	public boolean unique = true;   // additional constraint that all variables must be unique
	public boolean headRule = false; // true if predicate is the head of a rule.
	public boolean shortString = true; // true if we want shortened predicates printed.


	public boolean Special(){
		return false;
	} // method Special

	public Pred Clone() {
		return new Predicate(this);
	} // method clone

	private void init(String name,int arity){
		this.name = name;
		args = new Term[arity];
		boundByMe = new boolean[arity];
		constraints = new VarConstraint[arity/2];
		for(int i=0;i<arity;i++){
			boundByMe[i] = false;
			if (i%2 == 0)
				constraints[i/2] = new VarConstraint(new int[0]); //?? why new int[0] ??
		} // for

	} // method init

	public Predicate(String name,int arity){
		init(name,arity);
	} // constructor Predicate


	public Predicate(String name,String[] varNames){
		init(name,varNames.length);
		for(int i=0;i<varNames.length;i++){
			addTerm(new Variable(varNames[i]));
		} // for

	} // constructor Predicate

	public Predicate(Predicate p){
		init(p.name,p.args.length);

		for(int i=0;i<p.argCount;i++){
			args[i] = p.args[i];
		} // for
		argCount = p.argCount;
		not = p.not;

	} // constructor Predicate

	public Predicate(Fact f){
		init(f.name,f.args.length);

		for(int i=0;i<f.args.length;i++)
			args[i] = f.args[i];
		argCount = f.args.length;
		not = false;

	} // constructor Predicate

	public void printConstraints(){
		System.out.println("Constraints:");
		for (int i=0;i<constraints.length;i++){
			System.out.print(constraints[i] + " ");
		} // for each constraint

		System.out.println();
	}// method printConstraints

	public boolean determinant(ArrayList<String> varList){
		if (varList == null) return false;
		ArrayList<String> varNames = termNames();

//		System.out.println(varList);
//		System.out.println(varNames);

		Iterator<String> it = varNames.iterator();
		while(it.hasNext()){
			String name = it.next();
			if (varList.contains(name)) return true;
		} // while

		return false;
	} // method determinant

	/***************************************
		int degree:
			returns the number of terms (variables)
			that a variable of this predicate could depend on.

			Right now this is just 1-arity which is an upper bound on the
			actual degree of the predicate

			Probably should count the number of unique variables (not constants) -1.
	***************************************/
	public int degree(){
		int deg=0;
		ArrayList<String> varNames = varNames();

		deg = varNames.size();
		if (deg < argCount) return deg;
		else return deg-1;

	} // method degree

	/*************************************************
		int depth:
			returns the depth of this variable with respect to
			the current depth and the current variables

			returns curDepth if in current depth
			returns curDepth+1 if true
			returns -1 if depth is >= curDepth+2

		Note:
			This inforces determinism to some degree
			i.e. variables that are completely unbound to the
			the varNames from the rest of the clause have
			undefined (infinite) depth.
	*************************************************/

	public int depth(ArrayList<String> varNames, int curDepth){
		if (depth >= 0) return depth;

		// need at least one variable in varNames
		int numVars = varNames().size();

		int termsNotAccounted = argCount;
		for (int i=0;i<argCount;i++){
			if (args[i] instanceof Variable){
				if (varNames.contains(args[i].name)){
					termsNotAccounted--;
				} // if in the
			} // if this is a variable
			else
				termsNotAccounted--;
		} // for

		if (termsNotAccounted == 0 || (termsNotAccounted == 1 && argCount > 1 && numVars > 1)){
			depth = curDepth + 1;
		} // if
		else depth = -1;


		return depth;
	} // method depth

	public ArrayList<Pred> permute(ArrayList<Variable> vars){
		return new ArrayList<Pred>();
	} // permute


	// returns a List of all variable names
	// whether that variable is bound or not. (Check this.)
 	public ArrayList<String> varNames(){
		ArrayList<String> al = new ArrayList<String>(argCount);
		for(int i=0;i<argCount;i++){
			if (args[i] instanceof Variable)
			if (!al.contains(args[i].name)) al.add(args[i].name);
		} // for
		return al;
	} // method varnames

	// returns a List of all variable names
	// whether that variable is bound or not. (Check this.)
 	public ArrayList<String> termNames(){
		ArrayList<String> al = new ArrayList<String>(argCount);
		for(int i=0;i<argCount;i++){
			//if (args[i] instanceof Variable)
				if (!al.contains(args[i].name)) al.add(args[i].name);

		} // for
		return al;
	} // method varnames

	public void setVarList(Variable[] varList){
		if (varList == null) return;
		for(int i=0;i<argCount;i++){
			for(int j=0;j<varList.length;j++)
				if(args[i].name.equals(varList[j].name))
					args[i] = varList[j];
		} // for
	} /// setVarList


	public boolean not(){
		return not;
	} // method not

	public void negate(){
		not = !not;
	} // method negate

	public void addTerm(Term t){
		for(int i=0;i<args.length;i++){
			if(args[i] == null) {
				args[i] = t;
				argCount++;
				return;
			}// if
		} // for

	}// method addTerm

	public Term arg(int i){
		if (i >= args.length) return null;
		return args[i];
	} // method arg;

	/***************************************
		Method substitute:
			Substitute occurances of v with t
	***************************************/
/*
	public void substitute (Variable v, Term t){
		for (int i = 0;i< args.length;i++){
			args[i].substitute(v,t);
		} // for
	} // method Substitute
*/
	public Boolean evaluate (DBase D){
		return D.evaluate(this);
	} // method evaluate

	public String headToString(){
		String temp = "" + name;
		if (argCount != args.length) temp += ":" + args.length;
		temp += "(";
		if(not == true) temp = "~" + temp;

		temp += "*)";

		return temp;
	} // method headToString

	public String shortString(){
		//System.out.println("shortString");
		String temp = name;

		if (argCount != args.length) temp += ":" + args.length;
		temp += "(";
		if(not == true) temp = "~" + temp;

		for(int i=0;i<argCount;i++){
            // if args[i] and args[i+1] are nearly the same variable type
            // then just print the commonality.
            if (args[i] instanceof Variable && args[i+1] instanceof Variable){
                Variable v1 = (Variable)args[i], v2 = (Variable)args[i+1];
                if (v1.type.compatible(v1.type,COLOR) &&
                    v2.type.compatible(v2.type,PIECE))
                {
                    temp += v1.type.toStrWithout(LOC);
                    i++;
                    if (i < argCount -1) temp += ",";
                }
            }
            else
            	temp += args[i].toString() + ",";
		}// for

		// if(argCount > 0) temp += args[argCount-1].toString();
		temp += ")";

		return temp;
	}

	public String toString(){
		if (headRule) return headToString();
		if (shortString) return shortString();

		String temp = name;
		if (argCount != args.length) temp += ":" + args.length;
		temp += "(";
		if(not == true) temp = "~" + temp;

		for(int i=0;i<argCount-1;i++){
			temp += args[i].toString() + ",";
		}// for

		if(argCount > 0) temp += args[argCount-1].toString();
		temp += ")";

		return temp;
	} // method toString

	public boolean match(Fact f){
		if (!name.equals(f.name)) return false;
		if (argCount != f.args.length) return false;
		for(int i=0;i< argCount;i++){
			if (! args[i].match(f.args[i])) return false;
		}// for
		return true;
	} // method match

	public String name(){
		return name;
	} // method matchesName

	public int argCount(){
		return args.length;
	}// method argCount


	public boolean equals(Predicate p){
		if(!name.equals(p.name)) return false;
		if(!not == p.not) return false;
		if(argCount != p.argCount) return false;
		for(int i=0;i<argCount;i++)
			if(!args[i].equals(p.args[i])) return false;

		return true;
	} // method equals
/*
	public Predicate LGG (Predicate p){
		ArrayList<String> names = new ArrayList<String>();
		return LGG(p,names,new LookupTable());
	} // method LGG

	public Predicate LGG(Predicate p, ArrayList<String> names){
		return LGG(p,names,new LookupTable());
	} // method LGG

	public Predicate LGG(Predicate p, LookupTable LT){
		ArrayList<String> names = new ArrayList<String>();
		return LGG(p,names,new LookupTable());
	} // method LGG
*/
	// Don't know what to do about the boundbyme stuff.
/*	public Predicate LGG (Predicate p,ArrayList<String> nameList,LookupTable LT){
		if (p.name != name) return null;
		if (p.argCount != argCount) return null;
		if (not != p.not) return null;

		Predicate newP = new Predicate(name,argCount);
		newP.not = not;

		for (int i=0;i<argCount;i++){
			 if (LT.contains(args[i],p.args[i]))
					newP.addTerm(LT.get(args[i],p.args[i]));
			 else{
					Term result = args[i].LGG(p.args[i],nameList);
					if(result instanceof Variable){
						LT.insert(args[i],p.args[i],(Variable) result);
					} // Need to think about what happens if result is not a Variable
			 		newP.addTerm(result);
			 }
			 nameList.add(newP.args[i].name);

		} // for

		// System.out.println(LT);

		// assume that all bindings occur here.
		for (int i=0;i<argCount;i++){
			if (args[i].bound()) boundByMe[i] = true;
			else boundByMe[i] = false;
		} // for

		return newP;

	} // method LGG
*/

	public boolean containsVarFrom(Pred p){
		for(int i=0;i<args.length;i++)
			for(int j=0;j<p.argCount();j++)
				if(args[i].equals(p.arg(j))) return true;

		return false;
	} // method containsVariableFrom


	public Variable[] getVars(Variable []vs){
		boolean found;

		for(int i=0;i<argCount;i++){
			found = false;
			for (int vi = 0;vi < vs.length;vi++){
				if (vs[vi].equals(args[i])) {
					found = true;
					break;
				} // if
			} // for

			if (!found) {
				Variable [] temp = new Variable[vs.length+1];
				for (int t = 0;t<vs.length;t++) temp[t] = vs[t];
				temp[vs.length] = ((Variable)args[i]);
				vs = temp;
			} // if
		} // for

		// System.out.println("Done: predicate.getVars");

		return vs;
	} // method getVars

	public void bind (Fact f){
		if (!match(f)) return;
		for(int i=0;i<argCount;i++)
			if(!args[i].bound()) {
				args[i].bind(f.args[i]);
				boundByMe[i] = true;
			} // if
	} // method bind


	public void release(){
		for(int i=0;i<argCount;i++)	{
			if (boundByMe[i]){
				args[i].release();
				boundByMe[i] = false;
			}// if
		} // for

	} // method release


	// Still need to check LGG on variables bound to Constants
	// and Constants
	// and Clauses.


	// LGG(p1,p3) = LGG(Red({x,Joe},{y,Fred},z),Red(z,{w,Fred},{x,Joe})) = Red(x,{y,Fred},z)
	// Should this answer be: Red(x,{y,Fred},x) or Red({x,Joe},{w,Fred},{x,Joe}) ???
	//
	public static void main(String[] args){
		Predicate p1 = new Predicate ("Red",new String[] {"x","x","z"});
		//Predicate p2 = new Predicate ("Blue",new String[]{"x","y","z"});
		Predicate p3 = new Predicate ("Red",new String[] {"z","w","x"});

		p1.args[0].bind(new Constant("Joe"));
		p1.args[1].bind(new Constant("Joe"));
		p1.args[2].bind(new Constant("Fred"));
		p3.args[0].bind(new Constant("Larry"));
		p3.args[1].bind(new Constant("Larry"));
		p3.args[2].bind(new Constant("Moe"));


		System.out.println(p1);
		System.out.println(p3);

		//String[] names = {"x"};

//		Predicate resultP = p1.LGG(p3);
//		System.out.println("LGG(p1,p3) = LGG(" + p1 + "," + p3 + ") = " + resultP);
	} // method main

} // class Predicate


class SpecialPredicate extends Predicate{
	boolean includeNegative = true;
	public static final long serialVersionUID = 0;

	public SpecialPredicate(String name,int arity) {
		super(name,arity);
		init();
	}

	public SpecialPredicate(SpecialPredicate p){
		super(p);
		init();
	}

	public Pred Clone() {
		return new SpecialPredicate(this);
	} // method Clone




// method 

	private void init(){
		for (int i=0;i<args.length/2;i++){
			constraints[i] = new VarConstraint(new int[] {COLOR,PIECE});
		} // for
	} // method init


	/*****************************************************************************
	 * boolean Special()
	 * why does this alwayse return true?
	 *****************************************************************************/
	public boolean Special(){
		return true;
	}

	public ArrayList<Pred> permute(ArrayList<Variable> varList){
		if (args.length == 4) return permute4(varList);
		// printConstraints();
		if (args.length == 6) return permute6(varList);

		ArrayList<Pred> retList = new ArrayList<Pred>();
		for(int i=0;i<varList.size();i++)
			for(int j=i;j<varList.size();j++){
				Variable v1 = (Variable)varList.get(i);
				Variable v2 = (Variable)varList.get(j);
				if (unique && i == j) continue;
				if (constraints[0].compatible(v1,v2)){
					Pred cp = Clone();
					cp.addTerm(v1);
					cp.addTerm(v2);
					retList.add(cp);
					if(includeNegative){
						Pred cp2 = Clone();
						cp2.addTerm(v1);
						cp2.addTerm(v2);
						cp2.negate();
						retList.add(cp2);
					} // if include negative
				} // if constraints hold
			} // for j

		return retList;
	} // method permute
	
	public ArrayList<Pred> permute6(ArrayList<Variable> varList){
		ArrayList<Pred> retList = new ArrayList<Pred>();
		Variable v1 = (Variable)varList.get(0);
		Variable v2 = (Variable)varList.get(1);
		Variable v3 = (Variable)varList.get(2);
		Variable v4 = (Variable)varList.get(3);
		Variable v5 = (Variable)varList.get(4);
		Variable v6 = (Variable)varList.get(5);
		Pred cp = Clone();
		cp.addTerm(v1);
		cp.addTerm(v2);
		cp.addTerm(v3);
		cp.addTerm(v4);
		cp.addTerm(v5);
		cp.addTerm(v6);
		retList.add(cp);
		if (includeNegative) {
			Pred cp2 = Clone();
			cp2.addTerm(v1);
			cp2.addTerm(v2);
			cp2.addTerm(v3);
			cp2.addTerm(v4);
			cp2.addTerm(v5);
			cp2.addTerm(v6);
			cp2.negate();
			retList.add(cp2);
		}
		
		return retList;
	}

	public ArrayList<Pred> permute4(ArrayList<Variable> varList){
		// printConstraints();

		ArrayList<Pred> retList = new ArrayList<Pred>();
		for(int i=0;i<varList.size();i++)
			for(int j=i;j<varList.size();j++){
				if (unique && i == j) continue;

	//			checkPair((Variable)varList.get(i),(Variable)varList.get(j))

				Variable v1 = (Variable)varList.get(i);
				Variable v2 = (Variable)varList.get(j);
				if (!constraints[0].compatible(v1,v2)){
					continue;
				} // if
				for(int k=j;k<varList.size();k++){
					if (unique && k == j) continue;
					for(int l=k;l<varList.size();l++){
						if (unique && k == l) continue;
						Variable v3 = (Variable)varList.get(k);
						Variable v4 = (Variable)varList.get(l);

						if (constraints[1].compatible(v3,v4)){
							Pred cp = Clone();
							cp.addTerm(v1);
							cp.addTerm(v2);
							cp.addTerm(v3);
							cp.addTerm(v4);
							retList.add(cp);
							if (includeNegative) {
								Pred cp2 = Clone();
								cp2.addTerm(v1);
								cp2.addTerm(v2);
								cp2.addTerm(v3);
								cp2.addTerm(v4);
								cp2.negate();
								retList.add(cp2);
							}// if includeNegative
						}// if constraints hold


					} // for l
				} // for k


			} // for j

		return retList;
	} // method permute4


	public boolean SpecialArgs(){
		return false;
	} // method SpecialArgs

	public void checkArgs()throws UnboundArgumentException{
		for(int i=0;i<args.length;i++)
			if (!args[i].bound()) throw new UnboundArgumentException();

	} // method checkArgs


	public boolean match(Fact f){

		// System.out.println("Calling match in " + name);
		try {
			checkArgs();
			if(not)	{
//				System.out.print("Not: ");
//				System.out.println("current Fact: " + f);
				boolean retval = !SpecialArgs();
//				System.out.println("Return Value: " + retval);
				return retval;
			}
			else {
				//System.out.println("current Fact: " + f);
				return SpecialArgs();
			}
		} // if Equal

		catch (UnboundArgumentException e){
			System.out.println("Unbound Argument Exception for " + this);
			e.printStackTrace();
			System.exit(0);
			return false;
		} // catch

	} // method match


}// class SpecialPredicate


class EqualPredicate extends SpecialPredicate{
	public static final long serialVersionUID = 0;
	
	public EqualPredicate(){
		super("Equal",2);
		init();
		shortString = false;
	} // constructor EqualPredicate

	public EqualPredicate(EqualPredicate p){
		super(p);
		init();
		shortString = false;
	}

	private void init(){
		for (int i=0;i<args.length/2;i++){
			constraints[i] = new VarConstraint(new int[] {LOC});
			//constraints[i] = new VarConstraint(new int[]{});
		} // for
	} // method init


	public Pred Clone() {
		return new EqualPredicate(this);
	} // method Clone

	public boolean SpecialArgs(){
//		System.out.println("Checking EqualArgs for: " + this);
//		System.out.println("First Arg is: " + ((Variable)args[0]).binding);
//		System.out.println("Second Arg is: " + ((Variable)args[0]).binding);

	 	
	 	boolean retval = ((Variable)args[0]).binding.equals(((Variable)args[1]).binding);
//	 	System.out.println("Return Value is:" + retval);
	 	return retval;
	} // method EqualArgs

} // class EqualPredicate


class CornerPredicate extends SpecialPredicate{
	public static final long serialVersionUID = 0;
	
	public CornerPredicate(){
		super("Corner",2);
	} // constructor CornerPredicate

	public CornerPredicate(CornerPredicate p){
		super(p);
	}

	public Pred Clone() {
		return new CornerPredicate(this);
	} // method Clone


	public boolean SpecialArgs(){
		// System.out.println("Checking CornerArgs for: " + this);

		Constant zero = new Constant("0");
		Constant three = new Constant("3");

	 	if (((Variable)args[0]).binding.equals(zero) && ((Variable)args[1]).binding.equals(zero))
				return true;
	 	if (((Variable)args[0]).binding.equals(three) && ((Variable)args[1]).binding.equals(zero))
				return true;
	 	if (((Variable)args[0]).binding.equals(zero) && ((Variable)args[1]).binding.equals(three))
				return true;
	 	if (((Variable)args[0]).binding.equals(three) && ((Variable)args[1]).binding.equals(three))
				return true;

		return false;
	} // method CornerArgs

} // class CornerPredicate

class EdgePredicate extends SpecialPredicate{
	public static final long serialVersionUID = 0;
	
	public EdgePredicate(){
		super("Edge",2);
	} // constructor EdgePredicate

	public EdgePredicate(EdgePredicate p){
		super(p);
	}

	public Pred Clone() {
		return new EdgePredicate(this);
	} // method Clone

	public boolean SpecialArgs(){
		// System.out.println("Checking EdgeArgs for: " + this);
		Constant zero = new Constant("0");
		Constant three = new Constant("3");

	 	if (((Variable)args[0]).binding.equals(zero))
				return true;
	 	if (((Variable)args[0]).binding.equals(three))
				return true;
	 	if (((Variable)args[1]).binding.equals(zero))
				return true;
	 	if (((Variable)args[1]).binding.equals(three))
				return true;

		return false;
	} // method SpecialArgs

} // class EdgePredicate

class AdjacentPredicate extends SpecialPredicate{
	public static final long serialVersionUID = 0;
	
	public AdjacentPredicate(){
		super("Adjacent",4);
	} // constructor AdjacentPredicate

	public AdjacentPredicate(AdjacentPredicate p){
		super(p);
	}

	public Pred Clone() {
		return new AdjacentPredicate(this);
	} // method Clone


	public boolean SpecialArgs(){
		// System.out.println("Checking AdjacentArgs for: " + this);
		int [] Args = new int[4];

		try {
			// get Integer value for args
			for (int i=0;i<4;i++) {
				Args[i] = Integer.parseInt(((Variable)args[i]).binding.toString());
			} // for
		}
		catch (NumberFormatException e){
			System.out.println("Invalid Argument for AdjacentPredicate");
			System.exit(0);
			return false;
		}

		if (Args[0] == Args[2] && Args[1] == Args[3]) return false;
	 	return (Math.abs(Args[0] - Args[2]) <= 1) && (Math.abs(Args[1] - Args[3]) <= 1);
	} // method AdjacentArgs

} // class AdjacentPredicate

class RelativePredicate extends SpecialPredicate{
	
	public static final long serialVersionUID = 0;
	int xRel;
	int yRel;
	public RelativePredicate(int x,int y){
		super(("Relative"+x)+y,4);
		xRel = x;yRel = y;
	} // constructor RelativePredicate

	public RelativePredicate(RelativePredicate p){
		super(p);
		xRel = p.xRel;
		yRel = p.yRel;
	}

	public Pred Clone() {
		return new RelativePredicate(this);
	} // method Clone


	public boolean SpecialArgs(){
		//System.out.println("Checking RelativeArgs for: " + this + " :" + xRel +","+ yRel);
		int [] Args = new int[4];

		try {
			// get Integer value for args
			for (int i=0;i<4;i++) {
				Args[i] = Integer.parseInt(((Variable)args[i]).binding.toString());
			} // for
		}
		catch (NumberFormatException e){
			System.out.println("Invalid Argument for RelativePredicate");
			System.exit(0);
			return false;
		}

		boolean match =
			((Math.abs(Args[0] - Args[2]) == xRel) && (Math.abs(Args[1] - Args[3]) == yRel))
		||	((Math.abs(Args[0] - Args[2]) == yRel) && (Math.abs(Args[1] - Args[3]) == xRel));

		//System.out.println(Args [0]+ ", " + Args [1]+ ", " + Args [2]+ ", " + Args [3]);
		//System.out.println("Result is " + match);
		return match;
	} // method RelativeArgs

} // class RelativePredicate


class DiagonalPredicate extends SpecialPredicate{
	public static final long serialVersionUID = 0;
	
	public DiagonalPredicate(){
		super("Diagonal",4);
	} // constructor CornerPredicate

	public DiagonalPredicate(DiagonalPredicate p){
		super(p);
	}

	public Pred Clone() {
		return new DiagonalPredicate(this);
	} // method Clone

	public boolean SpecialArgs(){
		// System.out.println("Checking DiagonalArgs for: " + this);
		int [] Args = new int[4];

		try {
			// get Integer value for args
			for (int i=0;i<4;i++) {
				Args[i] = Integer.parseInt(((Variable)args[i]).binding.toString());
			} // for
		}
		catch (NumberFormatException e){
			System.out.println("Invalid Argument for DiagonalPredicate");
			System.exit(0);
			return false;
		}

		if (Args[0] == Args[2] && Args[1] == Args[3]) return false;
	 	return (Math.abs(Args[0] - Args[2]) == Math.abs(Args[1] - Args[3]));
	} // method DiagonalArgs

} // class DiagonalPredicate


class DistancePredicate extends SpecialPredicate{
	public static final long serialVersionUID = 0;
	
	public DistancePredicate(){
		super("Distance",5);
	} // constructor DistancePredicate

	public DistancePredicate(DistancePredicate p){
		super(p);
	}
/*
	private void init(){
		for (int i=0;i<args.length/2;i++){
			constraints[i] = new VarConstraint(new int[] {LOC});
		} // for
	} // method init
*/
	public Pred Clone() {
		return new DistancePredicate(this);
	} // method Clone

	public boolean SpecialArgs(){
		// System.out.println("Checking DistanceArgs for: " + this);
		int [] Args = new int[5];

		try {
			// get Integer value for args
			for (int i=0;i<5;i++) {
				Args[i] = Integer.parseInt(((Variable)args[i]).binding.toString());
			} // for
		}
		catch (NumberFormatException e){
			System.out.println("Invalid Argument for DistancePredicate");
			System.exit(0);
			return false;
		}

	 	return (Math.abs(Args[0] - Args[2]) == Args[4]) && (Math.abs(Args[1] - Args[3]) == Args[4]);
	} // method DistanceArgs

} // class DistancePredicate

class LTPredicate extends SpecialPredicate{
	public static final long serialVersionUID = 0;
	
	public LTPredicate(){
		super("LessThan",2);
		init();
	} // constructor LTPredicate

	public LTPredicate(LTPredicate p){
		super(p);
		init();
	}

	private void init(){
		for (int i=0;i<args.length/2;i++){
			constraints[i] = new VarConstraint(new int[] {LOC});
		} // for
	} // method init

	public Pred Clone() {
		return new LTPredicate(this);
	} // method Clone

	public boolean SpecialArgs(){
		// System.out.println("Checking LessArgs for: " + this);
		int arg0 = Integer.parseInt(((Variable)args[0]).binding.toString());
		int arg1 = Integer.parseInt(((Variable)args[1]).binding.toString());

	 	return arg0 < arg1;
	} // method LessArgs

} // class LTPredicate

class DupRookRowPredicate extends SpecialPredicate{
	public static final long serialVersionUID = 0;
	//private enum PieceType {WKR,WKC,BKR,BKC,WRR,WRC};
	
	public DupRookRowPredicate(){
		super("DuplicateRookRow",6);
	} // constructor DuplicateRookPredicate

	public DupRookRowPredicate(DupRookRowPredicate p){
		super(p);
	}

	public Pred Clone() {
		return new DupRookRowPredicate(this);
	} // method Clone

	public boolean SpecialArgs(){
		// System.out.println("Checking DupRookArgs for: " + this);
		int [] Args = new int[6];
		
		try {
			// get Integer value for args
			for (int i=0;i<6;i++) {
				Args[i] = Integer.parseInt(((Variable)args[i]).binding.toString());
			} // for
		}
		catch (NumberFormatException e){
			System.out.println("Invalid Argument for DupRookRowPredicate");
			System.exit(0);
			return false;
		}

		//Rows
		if (Args[2]==Args[4]) return false;
		if (Args[1]==Args[5]){                                        
			if(Args[2] < Args[4]){                                                                                                          
				if(Args[0] < Args[4]) return false;
			}
			if(Args[2] > Args[4])
				if (Args[0] > Args[4]) return false;
		}
		
		//Columns
		if (Args[3]==Args[5]) return false;
		if (Args[0]==Args[4]){                                        
			if(Args[3] < Args[5]){                                                                                                          
				if(Args[1] < Args[5]) return false;
			}
			if(Args[3] > Args[5])
				if (Args[1] > Args[5]) return false;
		}
	 	return (true);                                                                                                                
	 	                                                                                                        
	} // method DupRookRowPredicate

} // class DupRookRowPredicate

//in FoilImp
//add ps.Add(new DupRookRowPredicate());
