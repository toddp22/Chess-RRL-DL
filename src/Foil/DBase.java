package Foil;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/*******************************************************
	class: PredBase

	This is a database of facts of a particular Predicate
********************************************************/
class PredBase implements Serializable{
	public static final long serialVersionUID = 0;
	String name = null;     // name of the predicate
	int arity = 0;          // number of parameters
	ArrayList<Fact> facts = null;      // the facts in the database.
	boolean useIndex=false; // Use the index or not.
	Hashtable<Constant,ArrayList<Fact>>[] index = null;		// The index for each of the parameters

	boolean fullPrint = true;  // print the full table or not in: toString()
	boolean indexPrint = false; // print the index or not in: toString()

	public PredBase(String name,int arity){
		this.name = name;
		this.arity = arity;

		facts = new ArrayList<Fact>();
	} // constructor PredBase

	public void createIndex(){
		useIndex = true;
		index = new Hashtable[arity];
		for (int i=0;i<arity;i++) index[i] = new Hashtable<Constant,ArrayList<Fact>>();

		// Insert values into the index

		// updateIndex for each fact in the database.
		Iterator<Fact> it = facts.iterator();
		while(it.hasNext()){
			updateIndex((Fact)it.next());
		} // while

	}// public void createIndex

/*	public int numPositiveFacts(){
		int num = 0;
		for(int j=0;j<facts.size();j++){
			Fact f = (Fact)facts.get(j);
			if (f.value == Boolean.TRUE)
				num++;
		}
		
		return num;
	}// public int numPositiveFacts
*/	
	
	/*****************************************************
		method: updateIndex:

		Right now I'm just using the reference f
		I'm not sure how to properly link this to the
		entry in the facts List. (I'm not sure I need to.)
	*****************************************************/

	private void updateIndex(Fact f){
		if (!useIndex) return;

		for (int i=0;i<arity;i++){
			Constant c = f.args[i];
			if (!index[i].containsKey(c))
				index[i].put(c,new ArrayList<Fact>());

			ArrayList<Fact> argIndex = (ArrayList<Fact>)index[i].get(c);
			argIndex.add(f);
		} // update index for each parameter
	} // method updateIndex

	public void insert(Fact f){
		if (f.name != name) return;
		if (f.args.length != arity) return;

		if(!facts.contains(f))facts.add(f);
		if(useIndex) updateIndex(f);
	} // method insert

	public void remove(Fact f){
		if (f.name != name) return;
		if (f.args.length != arity) return;

		if(facts.contains(f))facts.remove(f);

		if (useIndex){

			for (int i=0;i<arity;i++){
				Constant c = f.args[i];
				if (!index[i].containsKey(c))
					return;

				ArrayList<Fact> argIndex = index[i].get(c);
				argIndex.remove(f);
			} // for each parameter

		} // if useIndex

	} // method

	public boolean contains(Fact f){
		if (f.name != name) return false;
		if (f.args.length != arity) return false;

		return facts.contains(f);
	} // method contains

	public ArrayList<Fact> getMatches(Pred p){
		if (useIndex) return getMatchesWithIndex(p);

		ArrayList<Fact> matches = new ArrayList<Fact>();
		Iterator<Fact> it = facts.iterator();
		while(it.hasNext()){
			Fact f = it.next();
			if (p.match(f)) // it matches the predicate
				matches.add(f);
		}

		return matches;

	}// getMatches


	public ArrayList<Fact> getMatchesWithIndex(Pred p){
			int bestIndex = p.argCount();
			Constant key=null;

			// System.out.println("Getting Matches with Index");

			// Find first term that is a bound variable.
			// Will use the best term later.

			for (int i=0;i<p.argCount();i++){
				if (p.arg(i) instanceof Variable){
						Variable v = (Variable)p.arg(i);
						if(v.bound()) {
							bestIndex = i;
							key = v.binding;
							break;
						} // if
				} // if
				else if (p.arg(i) instanceof Constant){
					bestIndex = i;
					key = (Constant)p.arg(i);
					break;
				} //else
			}// for

			ArrayList<Fact> matches = new ArrayList<Fact>();


			if (bestIndex >= p.argCount()) { // we have all unbound variables
			                     // return the entire predBase.

				Iterator<Fact> it = facts.iterator();
				while(it.hasNext())
					matches.add(it.next());
			}
			else { // we have at least one unbound variable.
				   // Use the index appropriate.

				Hashtable<Constant,ArrayList<Fact>> Index = index[bestIndex];
				// Set keys = Index.keySet();
				List<Fact> keyFacts = Index.get(key);
				Iterator<Fact> it = keyFacts.iterator();
				while(it.hasNext()){
					Fact f = it.next();
					if (p.match(f)) // it matches the predicate
						matches.add(f);
				}
				// System.out.println();
			}


			return matches;
	} // getMatchesWithIndex


	public String toString(){
		String retStr = "";
		retStr += "PredBase: " + name + ": (" + arity + ")\n";
		int size = facts.size();
		retStr += "Contains: " + size + (size>1 ?" entries.":" entry");

		if(fullPrint){
			retStr += "\n";
			Iterator<Fact> it = facts.iterator();
			while(it.hasNext()){
				Fact f = it.next();
				retStr += f.toString() + "\n";
			} //while
		} // if fullPrint

		if (indexPrint && useIndex){
			retStr += "\n";
			retStr += "index:\n";

			for(int i=0;i<arity;i++){
				retStr += "arg " + i + "\n";
				Collection vals = index[i].values();
				Iterator it = vals.iterator();
				while(it.hasNext()){
					List l = (List)it.next();
					retStr += l.toString() + "\n";
				} //while
			} // for each argument

		} // if indexPrint

		return retStr;
	} // method toString



	public void dump(PrintWriter out){
		if(out == null) return;

		Iterator<Fact> it = facts.iterator();
		while (it.hasNext()) {
				out.println((Fact)it.next());
		} // while more facts
	} // method dump


} // class PredBase


/******************************************
	class: DBase

	This is a rewrite of the prior database.

	This is a database of Predicates
******************************************/

public class DBase implements Serializable{

	public static final long serialVersionUID = 0;
	Hashtable pMap = new Hashtable();
	boolean useIndex = true;

	public DBase(){
	} // constructor DBase

	public DBase (String filename,String predicateName,int val,int t){
		String delims = "(){}, ";
		//int count = 0;
		try{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line;
			StringTokenizer st;
			int[] params;
			String name;
			line = in.readLine();
			st = new StringTokenizer(line,delims);
			double value=0;
			while(line != null){
				//System.out.println("Adding fact for: " + line);
				st = new StringTokenizer(line,delims);
				params = new int[st.countTokens() -3];
				name = st.nextToken();
				name = predicateName;
				int turn = Integer.parseInt(st.nextToken()); // skip first param for now.
				if (turn != t) {
					line = in.readLine();
					continue; // only black's turn for now.
				}
				int i=0;
				while(st.hasMoreTokens()){
					if (i<params.length)
						params[i++] = Integer.parseInt(st.nextToken());
					else
						value = Double.parseDouble(st.nextToken());
				} // while
				//System.out.println("Value = " + value + ", val = " + val);
				if (value == val){
					insert(new Fact(name,params,true));
				}
				else{
					insert(new Fact(name,params,false));
				}
				line = in.readLine();
			} // while

		} // try
		catch (FileNotFoundException e){
			System.out.println("File Not Found Exception in reading file");
		}
		catch (IOException e){
			System.out.println("IO Exception in reading file");
		}

	} // constructor Database

	public void remove(Fact f){
		PredBase pb = (PredBase)pMap.get(f.name);
		if (pb == null) return;
		pb.remove(f);

	} // method remove


	/*********************************************************
		method LGG:
			return the cross Product LGG of entire database.
	*********************************************************/

	public Clause LGG(){
		return LGG(new LookupTable(),new ArrayList<String>());
	} // method LGG

	public Clause LGG (LookupTable LT,ArrayList<String> nameList){
		Clause c = new Clause();


		Collection preds = pMap.values();
		Iterator it = preds.iterator();
		while(it.hasNext()){
			PredBase pb = (PredBase)it.next();
			List<Fact> facts = pb.facts;
			Iterator<Fact> factIt = facts.iterator();
			Iterator<Fact> factIt2 = facts.iterator();
			while(factIt.hasNext()){
				Fact f = (Fact)factIt.next();
				while(factIt2.hasNext()){
					Fact f2 = (Fact)factIt.next();
					Predicate p = f.LGG(f2,LT,nameList);
					if (p!= null) c.addPredicate(p);
				} // while more facts

			} // while more facts

		}// while more predicates


		//System.out.println(LT);
		return c;
	} // method LGG


	public Boolean evaluate(Predicate p){
		if(p == null) return new Boolean(false);

		ArrayList<Fact> factList = getMatches(p);
		Iterator<Fact> factIt = factList.iterator();
		while(factIt.hasNext()){
			Fact f = (Fact)factIt.next();
			//	System.out.println("Matches Fact: " + f);
				return f.evaluate(p);
			} // while

		// use the assume false if not in database.
		if(p.not)	return new Boolean(true);
		else return new Boolean(false);
	} // method evaluate



	/**
		evaluate: Function f
		This needs to be completed.

		Not supported right now.
	*/
	public Constant evaluate(Function f){

		return null;
	} // method evaluate


	public void insert(Fact f){
		// determine which predBase the fact should go in.
		// We will just map based on name
		PredBase pb = (PredBase)pMap.get(f.name);
		// insert if necessary.
		if (pb == null){
			pb = new PredBase(f.name,f.args.length);
			if (useIndex) pb.createIndex();
			pMap.put(f.name,pb);
		} // if no predbase

		pb.insert(f);

	} // method insert


	/**
		genNegativeDatabase

		Generate Negative Database

	*/

	/*********************************************
				HERE!!
	*********************************************/

	private boolean findMatchingFacts(List<Pred> Ps,int pi, DBase N){
		// ps = precicates
		// pi is predicate index
		// N is negative database.

		if(pi >= Ps.size()) return true;    // why true?
		Pred p = Ps.get(pi);


		// This approach doesn't work with negative predicates,
		// because a negative predicate will never match the facts in a positive only database.
		// Only two solutions,
		//    1. look for bindings in a negative database,
		//    2. generate all possible negative bindings in order to find all possible matches for a clause.



		// I think a better way to do this is to bind all of the positive clauses, then
		// count the number of negative matches that are possible, only need to
		// determine if the number is > 0

		// The Special predicates are handled else where
		if (p.Special()) return findMatchingFacts(Ps,pi+1,N);
		List<Fact> FactList;


		if (p.not() == false) // positive predicate
			FactList = getMatches(p);
		else
			FactList = N.getMatches(p);

		// check to see if this predicate has a matching fact.
		// if so bind to these facts (one at a time)
		// and continue to find other matching facts for other predicates.
		Iterator<Fact> it = FactList.iterator();
		while(it.hasNext()){
			Fact f = (Fact)it.next();
			p.bind(f);
			if(findMatchingFacts(Ps,pi+1,N)) return true;
			p.release();
		} // for each matching fact


		// I'm not sure that this works correctly for
		// the ~Equals clause.
		// This should be done prior in the binding for the head
		// part.

		// Not using the negative database -- we would just continue here.

		return false; // no match
	}// findMatchingFact


	public boolean Matches(Clause c, DBase N){
		boolean match;

		if (c == null) return true;

		match = findMatchingFacts(c.predicates,0, N);

		for(int i=0;i<c.predicates.size();i++){
			((Pred)c.predicates.get(i)).release();
		}// for

		return match;
	} // method Matches

	public List<Fact> getFacts(){
		List<Fact> allFacts = new ArrayList<Fact>();

		Collection preds = pMap.values();
		Iterator it = preds.iterator();
		while(it.hasNext()){
			PredBase pb = (PredBase)it.next();
			List<Fact> facts = pb.facts;
			allFacts.addAll(facts);

		}// while more predicates

		return allFacts;

	} // method getFacts


	public DBase genNegativeDatabase(){
		DBase N = new DBase();
		//Fact f = null;

		Collection preds = pMap.values();
		Iterator it = preds.iterator();
		while(it.hasNext()){
			PredBase pb = (PredBase)it.next();
			List<Fact> facts = pb.facts;
			Iterator<Fact> factIt = facts.iterator();
			while(factIt.hasNext()){
				Fact f = (Fact)factIt.next();
				if (f.value.equals(new Boolean (false))){
					factIt.remove();
					N.insert(f);
				}
			} // while more facts

		}// while more predicates

		return N;
	} // method genNegativeDatabase()


	/**
		getMatches:
		Find all facts matching predicate p

		Matches both positive and negative instances of p
	*/

	public ArrayList<Fact> getMatches(Pred p){

		ArrayList<Fact> matches = new ArrayList<Fact>();
		// Fact[] matches = new Fact[countMatches(p)];
		if(p == null) return matches;
		//int cur = 0;

		PredBase pb = (PredBase)pMap.get(p.name());
		if (pb == null) return matches;


		matches = pb.getMatches(p);
		// get all my matches.


		return matches;
	} // method getMatches

	public int countMatches(Predicate p){
		return getMatches(p).size();
	} // method countMatches



	public boolean contains(Fact f){
		if (f == null || f.name==null)return false;

		PredBase pb = (PredBase)pMap.get(f.name);
		if (pb == null) return false;
		else return pb.contains(f);
	} // method contains

	public String toString(){
		String retStr = "";
		Collection tables = pMap.values();
		Iterator it = tables.iterator();

		while(it.hasNext()){
			PredBase pb = (PredBase)it.next();
			retStr += pb.toString()+ "\n";
		} // while more PredBases

		return retStr;

	} // method toString

	public void dump(String fname){
	try{
		PrintWriter out = new PrintWriter(new FileWriter(fname));

		Collection preds = pMap.values();
		Iterator it = preds.iterator();
		while(it.hasNext()){
			((PredBase)it.next()).dump(out);
		}// while more predicates

		out.close();
	}// try
	catch (Exception e){}
	} // method dump



	public static void main(String[] args){

	DBase J = new DBase("ValueF.db","CheckMate",10,FoilImp.BLACK);
	//DBase N = J.genNegativeDatabase();
	System.out.println(J);
		// System.out.println(J);



	/*
		Fact [] fact = new Fact[29];
		fact[0] = new Fact("Female","Sharon",true);
		fact[1] = new Fact("Male","Bob",true);
		fact[2] = new Fact("Female","Louise",true);
		fact[3] = new Fact("Female","Nora",true);
		fact[4] = new Fact("Male","Victor",true);
		fact[5] = new Fact("Male","Tom",true);
		String[] temp3 = {"Bob","Sharon"};
		fact[6] = new Fact("Father",temp3,true);
		String[] temp = {"Louise","Sharon"};
		fact[7] = new Fact("Mother",temp,true);
		String[] temp1 = {"Nora","Bob"};
		fact[8] = new Fact("Mother",temp1,true);
		String[] temp2 = {"Victor","Bob"};
		fact[9] = new Fact("Father",temp2,true);
		String[] temp6 = {"Bob","Tom"};
		fact[10] = new Fact("Father",temp6,true);
		String[] temp4 = {"Sharon","Nora"};
		fact[11] = new Fact("GrandDaughter",temp4,true);
		String[] temp5 = {"Louise","Tom"};
		fact[12] = new Fact("Mother",temp5,true);
		String[] temp7 = {"Sharon","Victor"};
		fact[13] = new Fact("GrandDaughter",temp7,true);
		String[] temp8 = {"Greg","Louise"};
		fact[14] = new Fact("Father",temp8,true);
		String[] temp9 = {"Mary","Louise"};
		fact[15] = new Fact("Mother",temp9,true);
		fact[16] = new Fact("Female","Mary",true);
		fact[17] = new Fact("Male","Greg",true);
		String[] temp10 = {"Sharon","Mary"};
		fact[18] = new Fact("GrandDaughter",temp10,true);
		String[] temp11 = {"Sharon","Greg"};
		fact[19] = new Fact("GrandDaughter",temp11,true);
		String[] temp12 = {"Greg","Bill"};
		fact[20] = new Fact("Father",temp12,true);
		String[] temp13 = {"Mary","Bill"};
		fact[21] = new Fact("Mother",temp13,true);
		String[] temp14 = {"Bill","Julie"};
		fact[22] = new Fact("Father",temp14,true);
		fact[23] = new Fact("Female","Julie",true);
		fact[24] = new Fact("Male","Bill",true);
		fact[25] = new Fact("Male","Joe",true);
		String[] temp15 = {"Bob","Joe"};
		fact[26] = new Fact("Father",temp15,true);
		String[] temp16 = {"Julie","Mary"};
		fact[27] = new Fact("GrandDaughter",temp16,true);
		String[] temp17 = {"Julie","Greg"};
		fact[28] = new Fact("GrandDaughter",temp17,true);

		DBase D = new DBase();
		for (int i=0;i<fact.length;i++)
			D.insert(fact[i]);
		*/
		// System.out.println(D);

		//Predicate p = new Predicate();
		/*
		Predicate p1 = new Predicate ("CheckMate",new String[] {"x","y","z","w","v","u"});
		p1.args[0].bind(new Constant("3"));
		p1.args[1].bind(new Constant("3"));
		p1.args[2].bind(new Constant("3"));
		p1.args[5].bind(new Constant("3"));

		// D.dump("GrandDaughter.base");
		System.out.println("Getting matches for : " + p1);
		System.out.println(J.getMatches(p1));
		*/

	} // method main

} // class DBase
