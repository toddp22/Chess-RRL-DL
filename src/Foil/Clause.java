package Foil;
// class Clause

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


class Clause implements Serializable{
	public static final long serialVersionUID = 0;
	public ArrayList<Pred> predicates = new ArrayList<Pred>();
	public DBase facts = new DBase();

	public Clause(){
	} // constructor Clause

	public Clause(Pred p){
		predicates.add(p);
	} // constructor Clause

	public Clause(Pred[] L){
		for(int i=0;i<L.length;i++)
			predicates.add(L[i]);
	} // constructor Clause

	public void setMatches (ArrayList<Fact> flist){
		for(int i=0;i<flist.size();i++)	{
			Fact f = (Fact)flist.get(i);
			facts.insert(f);
		}// for

	} // method setMatches

	public void printMatches(){
		System.out.println(facts);
	}
	public int degree(){
		int maxDeg = 0;
		Iterator<Pred> it = predicates.iterator();

		while (it.hasNext()){
			Predicate p = (Predicate)it.next();
			int deg = p.degree();
			if (deg > maxDeg) maxDeg = deg;
		} // while

		return maxDeg;
	} // method degree

	/*************************************************
		int depth:
			returns the maximum depth of each of the predicates
			a predicate whose variables are all in the head have a depth of 0.
			variables which are not in the head that only depende on depth 0 variables have degree 1.
			etc.

		Algorithm:
			while (!done){
				search for
			}
	**************************************************/
	public int depth(ArrayList<String> varList) {
		int currentDepth=0;
		int numDone = 0;
		int lastNumDone = -1;

		// System.out.println("Clause: depth " + varList);

		// initialize depth
		for(int i=0;i<predicates.size();i++) ((Predicate)predicates.get(i)).depth = -1;
		// System.out.println("initialized");

		// set those who are one greater than current depth
		while(numDone < predicates.size() && lastNumDone < numDone){
			lastNumDone = numDone;
			for(int i=0;i<predicates.size();i++){ // for each predicate
				Predicate p = (Predicate)predicates.get(i);
				if (p.depth >=0) continue; // already done
				if (p.depth(varList,currentDepth) >= 0) {
					numDone++;
				} // if valid depth from predicate
			} // for
			// System.out.println("numDone = " + numDone);

			// System.out.println("predicates depth: ");

			for(int i=0;i<predicates.size();i++){ // for each predicate
				Predicate p = (Predicate)predicates.get(i);
			//	System.out.print(p.depth + " ");
				if (p.depth ==currentDepth+1) {
					List<String> varNames = p.varNames();
					Iterator<String> it = varNames.iterator();
					while(it.hasNext()){
						String name = it.next();
						if (!varList.contains(name))varList.add(name);
					}// while

				}// if we need to add variables
			} // for

			// System.out.println();

			currentDepth++;

		} // while not done

		if (numDone < predicates.size())
			return -1;
		return currentDepth;
	} // method depth


	private void restrictDepth(int maxD,ArrayList<String> varList){
		int currentDepth=0;
		int numDone = 0;
		int lastNumDone = -1;

		// System.out.println("Clause: restrictDepth " + varList);

		// initialize depth
		for(int i=0;i<predicates.size();i++) ((Predicate)predicates.get(i)).depth = -1;
		// System.out.println("initialized");

		// set those who are one greater than current depth
		while(numDone < predicates.size() && lastNumDone < numDone && currentDepth < maxD){
			lastNumDone = numDone;
			for(int i=0;i<predicates.size();i++){ // for each predicate
				Predicate p = (Predicate)predicates.get(i);
				if (p.depth >=0) continue; // already done
				if (p.depth(varList,currentDepth) >= 0) {
					numDone++;
				} // if valid depth from predicate
			} // for
			// System.out.println("numDone = " + numDone);

			// System.out.println("predicates depth: ");

			for(int i=0;i<predicates.size();i++){ // for each predicate
				Predicate p = (Predicate)predicates.get(i);
				// System.out.println("Predicate " + p + " has depth of " + p.depth + " ");
				if (p.depth ==currentDepth+1) {
					
					List<String> varNames = p.varNames();
					Iterator<String> it = varNames.iterator();
					while(it.hasNext()){
						String name = it.next();
						if (!varList.contains(name))varList.add(name);
					}// while

				}// if we need to add variables
			} // for

			// System.out.println();

			currentDepth++;

		} // while not done

		Iterator<Pred> it = predicates.iterator();
		while(it.hasNext()){
			Predicate p = (Predicate)it.next();
			if (p.depth == -1) it.remove();
		} // while

	} // method restrictDepth

	private void restrictDegree(int j){
		// System.out.println("j is: " + j);

		Iterator<Pred> it = predicates.iterator();
		while (it.hasNext()){
			Predicate p = (Predicate)it.next();
			int deg = p.degree();
			// System.out.println("degree of " + p + " is " + deg);
			if (deg > j) it.remove();
		}// while

	} // method restrictDegree


	// assumes valid depth for each predicate
	private void makeDeterminant(ArrayList<String> varList){
		Iterator<Pred> it = predicates.iterator();

//		System.out.println("makeDerminant");
//		System.out.println(varList);

		while(it.hasNext()){
			Predicate p = (Predicate)it.next();
			if(!p.determinant(varList))
				it.remove();
		} // method while
	} // method makeDetminant

	public void ijDeterminant(int i,int j,ArrayList<String> varList){
		// j is  length
		restrictDegree(j);
		restrictDepth(i,varList);
//		System.out.println("After restrictDepth");
//		System.out.println(varList);
		// System.out.println(this);
		makeDeterminant(varList);
	}// ijDeterminant


	public boolean hasPred (Pred p){
		if (predicates == null) return false;

		return predicates.contains(p);
	} // method hasPredicate

	public Variable[] getVars(Variable []vs){
		//int count = 0;

		for(int i=0;i< predicates.size();i++)
			vs = ((Pred)predicates.get(i)).getVars(vs);

		return vs;
	} // getVars

	public boolean containsVarFrom(Pred p){
		for(int pi=0;pi<predicates.size();pi++){
			Pred p1 = (Pred)predicates.get(pi);
			if(p1.containsVarFrom(p)) return true;
		}

		return false;
	} // method containsVariableFrom

	public void addPredicate(Pred p){
		if (p == null) return;

		if(predicates == null) predicates = new ArrayList<Pred>();
		predicates.add(p);
	} // method addPredicate

	public void addPred(Pred p){
		if (p == null) return;

		if(predicates == null) predicates = new ArrayList<Pred>();
		predicates.add(p);
	} // method addPred

	/*************************************************************************
		LGG:	Call LGG for each predicate

				female(mary),parent(ann,mary)
				female(eve),parent(tom,eve)
				-----------------------------
				female(X),parent(Y,X)

				where X = lgg(mary,eve) and Y = lgg(ann,tom)

	*************************************************************************/
/*	public Clause LGG (Clause c){
		LookupTable LT = new LookupTable();  // LGG dictionary (hashtable)
		ArrayList<String> nameList = new ArrayList<String>();     // Nariable Names

		return LGG(c,nameList,LT);
	} // method LGG


	public Clause LGG(Clause c,ArrayList<String> nameList, LookupTable LT){
		// LGG not defined for unequal length clauses.
		// I'm changing this. (This will automatically take care of it self
		// on the predicates side of things.
		//if (predicates.size() != c.predicates.size()) return null;


		// Also not defined for unequal length predicates
		Clause newC = new Clause();

		for (int i=0;i<predicates.size();i++) {
			for (int j=0;j<c.predicates.size();j++){
				Predicate p1 = (Predicate)predicates.get(i);
				Predicate p2 = (Predicate)c.predicates.get(j);
				newC.addPredicate(p1.LGG(p2,nameList,LT));
			} // for each predicate from c
		}// for each predicate

		return newC;
	} // method LGG
*/
	/**************************************
		Method substitute:
			Substitue occurances of v with t
	**************************************/
/*	public void substitute (Variable v, Term t){
		for (int i = 0;i< predicates.size();i++)
			((Predicate)predicates.get(i)).substitute(v,t);
	} // method Substitute
*/
	public String toString(){
		String str = "";
		// str += facts.
		if (predicates != null)
			str += predicates.toString();
		return str;
	} // method toString

	public void setVarList(Variable[] varList){
		if (predicates == null) return;
		for(int i=0;i<predicates.size();i++){
			Pred p = (Pred)predicates.get(i);
			p.setVarList(varList);
		}// for
	} // method setVarList


	public boolean specialMatches(Predicate head, Fact f){
		//try{
		//BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		for(int i=0;i<predicates.size();i++){
			Pred p = (Pred)predicates.get(i);
		/*	if((p.name().equals("InCheck"))&& !p.not()) {
				System.out.println("p = " + p);
				System.out.println("f = " + f);
				System.out.println("p.Special() = " + p.Special());
				System.out.println("p.match(f) = " + p.match(f));
				stdin.readLine();
			}*/
			if (p.Special())            
				if (!p.match(f)) return false;
		} // for
		//}catch(IOException e){}

		return true;
	} // method specialMatches


	public static void main(String[] args){
		Clause c = new Clause();
		Clause c1 = new Clause();

		Predicate p1 = new Predicate ("Red",new String[] {"x","y","z"});
		Predicate p2 = new Predicate ("Blue",new String[]{"x1","y1","j3"});
		Predicate p3 = new Predicate ("Red",new String[] {"z","w","x"});
		Predicate p4 = new Predicate ("Blue",new String[]{"w","x","z"});

		p1.args[0].bind(new Constant("Bill"));
		p1.args[1].bind(new Constant("Joe"));
		p1.args[2].bind(new Constant("Joe"));

		p2.args[0].bind(new Constant("Bill"));
		p2.args[1].bind(new Constant("Joe"));
		p2.args[2].bind(new Constant("Sally"));

		p3.args[0].bind(new Constant("Sally"));
		p3.args[1].bind(new Constant("Fred"));
		p3.args[2].bind(new Constant("Fred"));

		p4.args[0].bind(new Constant("Sally"));
		p4.args[1].bind(new Constant("Fred"));
		p4.args[2].bind(new Constant("Sally"));

		System.out.println("p1:" + p1);
		System.out.println("p2:" + p2);
		System.out.println("p3:" + p3);
		System.out.println("p4:" + p4);

//		Predicate resultP = p1.LGG(p3);
//		System.out.println("LGG(p1,p3) = LGG(" + p1 + "," + p3 + ") = " + resultP);

		c.addPredicate(p1);
		c.addPredicate(p2);

		c1.addPredicate(p3);
		c1.addPredicate(p4);

		System.out.println();
		System.out.println("c:" + c);
		System.out.println("c1:" + c1);

	//	Clause resultC = c.LGG(c1);
		System.out.println("c1:" + c1);
//		System.out.println("LGG(c,c1) = LGG(" + c + ",\n\t\t\t\t\t\t\t\t" + c1 + ") = \n\t\t\t\t\t\t\t\t" + resultC);
	} // method main

} // class Clause

