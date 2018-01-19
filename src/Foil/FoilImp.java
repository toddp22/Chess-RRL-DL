package Foil;
/*****
	Foil.java
	by Todd S. Peterson
	Adapted from: "Machine Learning", Tom Mitchel chapter 10
			          Quinlan, J.R. (1990) Learning logical definitions from relations.
								                     Machine Learning 5, 239-266

	To Do:
	0. Further testing. Try to determine sibling.
	1. In order to speed things up a bit, create Database subsets that
	   correspond to rule predicates.
	1. Do a beam search
	2. Clean up code.
			Perhaps use the set data structure.
				Need a way to make two types of predicate set, one with only the names and arity,
				and one with all of the arguments.
			Eliminate Magic Numbers.
	3. Get to work without negative database.
	   // We can calculate entropy by generating all possible bindings for a rule, and just determine
		 // if the rule matches or not.
  	 // Not convinced that this will work. (perhaps in limited situations)


	4. Read Database from file.
	5. Generate Exceptions for invalid situations
	   like trying to evaluate an "Equal" predicate which has one or more unbound variables.


*****/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import VI.Board;
import VI.Move;
import VI.State;
import VI.ValueIterator;

public class FoilImp implements Constants{
	public final static int WHITE = 0;
	public final static int BLACK = 1;

	public DBase D;
	Rule[] rules;
	Rule bestRule;
	ArrayList<Rule> OldRules = new ArrayList<Rule>();
	Predicate target;
	PredicateSet ps; 	// predicate pool
	ArrayList<Constant> cs;  		// constant pool
	DBase N;			// all negative facts
	DBase C;      		// all covered facts
	Variable[] vars; 	// variable pool
	public PredicateSet cp; 	// candidate predicates (with variables)
	String[] varString = {"x","y","z","w","v","u","t","s","r"};
	String fname = "rule1";
	PrintWriter out;
	BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
	boolean Debug;


	DBase CurD;
	DBase CurN;

	public void makeVarList(){
		vars = new Variable[varString.length];
		vars[0] = new Variable(varString[0],new VariableType(BLACK,KING,ROW));
		vars[1] = new Variable(varString[1],new VariableType(BLACK,KING,COL));
		vars[2] = new Variable(varString[2],new VariableType(WHITE,KING,ROW));
		vars[3] = new Variable(varString[3],new VariableType(WHITE,KING,COL));
		vars[4] = new Variable(varString[4],new VariableType(WHITE,ROOK,ROW));
		vars[5] = new Variable(varString[5],new VariableType(WHITE,ROOK,COL));
		for(int i=6;i<vars.length;i++)
			vars[i] = new Variable(varString[i]);
		//System.out.print("Varlist: ");
		//for(int i=0;i<vars.length;i++)
		//	System.out.print(vars[i] + " ");
		//System.out.println();

	} // method makeVarList

//	public void getPredicateSet(DBase D,PredicateSet ps,int x,int y){
	public void getPredicateSet(DBase D,PredicateSet ps){
		List<Fact> facts = D.getFacts();
		
		for(int i=0;i<facts.size();i++){
			Fact f = (Fact)facts.get(i);
			if(!ps.Contains(f.name) && !target.name.equals(f.name))
				ps.Add(new Predicate(f.name,f.args.length));
		} //for

		// Always have the special Predicates
//		if (x != -1)
		ps.Add(new EqualPredicate());
		// ps.Add(new LTPredicate());
		ps.Add(new CornerPredicate());
		ps.Add(new EdgePredicate());
		ps.Add(new AdjacentPredicate());
		ps.Add(new DiagonalPredicate());
		ps.Add(new RelativePredicate(2,1));
		ps.Add(new RelativePredicate(2,0));
	
		// Add the Old Rule
		// if(Debug)	{
			//System.out.println("Adding Old Rules");
			for (int i=0;i<OldRules.size();i++){
				ps.Add((Pred)OldRules.get(i));
			}
			//System.out.println(OldRules);
		//}// if
	} // method getPredicateSet

	public void getConstantSet(DBase D,ArrayList<Constant> cs){
		List<Fact> facts = D.getFacts();

		for(int i=0;i<facts.size();i++){
			Fact f = (Fact)facts.get(i);
				for (int j=0;j<f.args.length;j++){
					Constant c = new Constant(f.args[j].name);
					if (!cs.contains(c)) cs.add(c);
				} // for
		} //for

		//System.out.println("Constant Set" + cs);
	} // method getConstantSet

	// returns true if all integers in count are ascending
	public boolean onlyAscending(int[] count){
		for(int i=0;i<count.length-1;i++)
			if(count[i] > count[i+1]) return false;
		return true;
	} // method onlyAscending

	public boolean incCount(int[] count, int max, boolean onlyDoAscending){

		for(int i=0;i<count.length;i++){
			count[i]++;

			if(count[i] <= max) {
				if(onlyDoAscending){
					if (onlyAscending(count)) return false; // not finished
					else return (incCount(count,max,onlyDoAscending));
				}
				else {
					return false; // not finished
				}
			}
			else count[i] = 0;
		} // for

		return true; // finished
	}// method incCount

	public void getNegativeDatabase2(){
		N = D.genNegativeDatabase();
	} // method getNegativeDatabase2

	public void getNegativeDatabase(){
		// generate all facts

		Constant[] c;
		int[] count;
		Boolean False = new Boolean(false);

		// for each Predicate
		for(int p=0;p<ps.size();p++){
			// for each argument in predicate p
			Predicate pr = (Predicate) ps.get(p);
			c = new Constant[pr.args.length];
			count = new int[pr.args.length];

			for(int i=0;i<c.length;i++)
					// c[i] = cs.clist[count[i]];
					c[i] = (Constant) cs.get(count[i]);
			Fact F1 = new Fact(pr.name,c,False);
			// System.out.println(F1);
			if(!D.contains(F1)) N.insert(F1);

			while (!incCount(count,cs.size()-1,false)){
				c = new Constant[pr.args.length];
				for(int i=0;i<c.length;i++)
					c[i] = (Constant) cs.get(count[i]);	// cs.clist[count[i]];
				Fact F = 	new Fact(pr.name,c,False);
				if(!D.contains(F)) N.insert(F);
			} // while
		}

		// do for target predicate also
			// for each argument in predicate p
			Predicate pr = target;
			c = new Constant[pr.args.length];
			count = new int[pr.args.length];

			for(int i=0;i<c.length;i++)
					c[i] = (Constant) cs.get(count[i]); // cs.clist[count[i]];
			Fact F1 = new Fact(pr.name,c,False);
			if(!D.contains(F1)) N.insert(F1);


			while (!incCount(count,cs.size()-1,false)){
				c = new Constant[pr.args.length];

				for(int i=0;i<c.length;i++)
					c[i] = (Constant) cs.get(count[i]); // cs.clist[count[i]];
				Fact F = 	new Fact(pr.name,c,False);
				if(!D.contains(F)) N.insert(F);
			} // while



	} // method getNegativeDatabase

	// assign variables to targetPredicate
	void assignToTarget(){
		for(int i=0;i<target.args.length;i++){
			target.addTerm(vars[i]);
		}// for

	}// method assignToTarget

	/**

	*/

	void genCandidatePreds() throws IOException{
		cp = new PredicateSet(false);

		for(int i=0;i<ps.size();i++){
			Pred curp = (Pred)ps.get(i);
			ArrayList<Variable> varList = new ArrayList<Variable>();

			int count = bestRule.countVars();
			if (!curp.Special()) count++;

			for(int v=0;v<count;v++) varList.add(vars[v]);
			ArrayList<Pred> permuteList = curp.permute(varList);

			for(int j=0;j<permuteList.size();j++){
				cp.Add((Pred)permuteList.get(j));
			}// for each predicate in the permutation
		} // for each predicate

		//System.out.println("Preds:");
		//System.out.println(cp);
		//stdin.readLine();

	} // getCandidatePreds

	void generateRules(){
		rules = new Rule[cp.size()];
		for(int i=0;i<rules.length;i++){
			rules[i] = new Rule(bestRule);
		} // for

		// add predicates from set.
		for(int i=0;i<rules.length;i++){
			if(!rules[i].hasPred((Pred)cp.get(i)))
				rules[i].addPred((Pred)cp.get(i));

			// System.out.println(rules[i]);
		}
	} // method generateRules()

	public Rule testRules() throws IOException{


		/* Algroithm
			For each Fact in the database that matches the target predicate
				Bind the Target Predicate to the fact.
				Count the number of items in the database that match all of the predicates in the body.
				Unbound variables may match any constant.
		 Foil_Gain(t,p1,n1,p0,n0)
		*/

		bestRule.calcEntropy(CurD,CurN);

		Rule newBestRule = bestRule;
		Rule r;
		double bestgain = 0;
		double gain;
		for (int i = 0;i< rules.length;i++){
			r = rules[i];
  		//if(Debug) System.out.println("Rule: # " + i + ": " + r);

			r.calcEntropy(CurD,CurN);
			gain = bestRule.Foil_Gain(r,CurD,CurN);

			//if (Debug) System.out.println("Gain is: " + gain);
			if (gain > bestgain){
				bestgain = gain;
				newBestRule = rules[i];
			} // if
		}// for



		//out.close();
		return newBestRule;
	} // method testRules()

	public void moveCovered() throws IOException{
		//BufferedReader stdin = new BufferedReader(new InputStreamReader (System.in));
		ArrayList<Fact> flist= bestRule.getPositiveMatches(D,N);
		//System.out.println(flist.size());
	  	//System.out.println("Covered Facts: ");
		if (flist == null) return;    //!!! CHECK THIS !!!!// 
	  	bestRule.setMatches(flist);

		//stdin.readLine();
	  	//out.println("Covered Facts: ");
		int j = 1;
		for(int i=0;i<flist.size();i++,j++)	{
			Fact f = (Fact)flist.get(i);
			//System.out.print(f + " ");
			out.print(f + " ");
			if (j == 4) { j = 0; out.println(); }
			C.insert(f);
			D.remove(f);
		} // for
		//System.out.println();
		//System.out.println();
		// System.out.println("Covered Facts: " + C);
		// System.out.println("Regular Facts: " + D);

	} // method moveCovered

	public void learn()throws IOException {
    		//getNegativeDBase();
		out = new PrintWriter(new BufferedWriter (new FileWriter(fname + ".out")));
 		bestRule = new Rule(target);

	    assignToTarget();
		System.out.println("Target Predicate: " + target);
		Rule newBestRule = null;

		while (true){
			CurD = D;
			CurN = N;
			while(true){
//				System.out.println("Generating Candidate Predicates");
				genCandidatePreds();
//				System.out.println("Generating Rules");
				generateRules();
//				System.out.println("Testing Rules");
				newBestRule = testRules();
				//System.out.println("NewBestRule is: ");
				//System.out.println(newBestRule);
				//out.println("NewBestRule is: ");
				//out.println(newBestRule);
				// choose best rule
				if(newBestRule == bestRule || newBestRule.entropy == 0.0) break;
				bestRule = newBestRule;
			} // while
			bestRule = newBestRule;
			// bestRule.brief = false;
			//out.println("BestRule is: ");
			//out.println(bestRule);
			//System.out.println("BestRule is: ");
			//System.out.println(bestRule);
			moveCovered();
			bestRule.newActiveClause();

			List<Fact> flist = bestRule.getPositiveMatches(D,N);
			if (flist.size() == 0) break;

		} // while no more facts.


		//System.out.println("All done!");
		bestRule.brief = false;
		System.out.println("BestRule is: ");
		System.out.println(bestRule);
		out.println("BestRule is: ");
		out.println(bestRule);
		out.flush();
		out.close();

		//System.out.println("Covered Facts: ");

		//for(int i=0;i<bestRule.body.length-1;i++)
		//	bestRule.printMatches(i);

} // method learn

	public FoilImp(Predicate target,DBase D,boolean getOldRules)throws IOException{
		Debug = getOldRules;
		this.target = target;
		fname = target.name;
		this.D = D;
		CurD = D;
		C = new DBase(); // These will store the Covered Instances.
		makeVarList();
		//if(getOldRules) getOldRules();
		ps = new PredicateSet();
		cs = new ArrayList<Constant>();
		cp = new PredicateSet(false);
//		getPredicateSet(D,ps,-1,0);
		getPredicateSet(D,ps);
		getConstantSet(D,cs);
		//System.out.println("Creating Negative Database"); // really really need negative as failure here.
		getNegativeDatabase2();
		CurN = N;
//		stdin.readLine();


} // constructor Foil

public void dumpRules() throws IOException{
		System.out.println("Writing Best Rule to disk");
		FileOutputStream file = new FileOutputStream("rules.dat");
		ObjectOutputStream outStream = new ObjectOutputStream(file);
		outStream.writeObject(bestRule);
		outStream.close();
		file.close();

} // method dumpRules

public void recreateRuleBase()throws IOException{
	System.out.println("Trying to open Check.db");
	DBase J = new DBase("Check.db","InCheck",10,BLACK);
	Predicate target = new Predicate("InCheck",6);
	System.out.println("Calculating FOL");
	FoilImp F = new FoilImp(target,J,false);
    F.learn();
	F.dumpRules();

} // method recreateRuleBase

public void getOldRules() {
	try{
		boolean successful=false;

		while(!successful){
			System.out.println("Opening Rule File");
			FileInputStream file = null;
			ObjectInputStream inStream = null;

			try{
				file = new FileInputStream("rules.dat");
                System.out.println(file);
				inStream = new ObjectInputStream(file);
                Object o = inStream.readObject();
                //System.out.println(o);
				OldRules.add((Rule)o);
				System.out.println("Added Old Rules");
				successful = true;
				inStream.close();
				file.close();
                //System.out.println(OldRules);
				Rule r = (Rule)OldRules.get(0);
                if (r == null){
                    inStream.close();
                    file.close();
                    recreateRuleBase();
                }
				r.predRule = true;
				r.setVarList(vars);
			}
			catch(InvalidClassException e){
				inStream.close();
				file.close();
				recreateRuleBase();
			}
			catch(IOException e){
				recreateRuleBase();
			}
            catch(Exception e){
                System.out.println(e);
                recreateRuleBase();
            }
		}

		for(int i=0;i<OldRules.size();i++)
			System.out.println(OldRules.get(i));

	}
	catch(IOException e){
		System.out.println(e);
		System.out.println("Problem getting Old Rules");
	}
/*	catch(ClassNotFoundException e){
		System.out.println(e);
		System.exit(3);
	}*/

}// method getOldRules

public static void transform (ValueIterator vi, Rule r1,double val,int turn,String tar) throws Exception{
/* Algorithm:
	transform looks at each clause in a rule and generates a high level move
	which describes the correct move for each of the facts that the clause covers.
	Each of these facts represents a board state. These board states are similar to
	the other board states in the clause.
	
	The high level move is generated by creating a new DBase (d2) of facts.
	The positive facts in the DBase are are the mini-max moves of each of the facts in
	the clause. The negative facts in d2 are the other moves of each of the facts in the
	clause. 
	
	Transform takes clauses in a rule one at a time.  The input rule r1 represents 
	all of the states that have the same mini-max value for the whole value-space.  
	The transform algorithm	creates a rule that represents the difference between
	the   
	
   Detail:
	vi is the value iterator.
	r1 is the input rule.
	val is a value between 0 and 1 inclusive which the other moves must be 
	 	less than to generate a negative fact.
	turn is 0 for white and 1 for black. (Verified)
	target is a string that names the target rule.
	
	d2 is the data base which holds facts represented by f1 and f2
	f is the current fact covered by the input rule r1
	f1 is the mini-max move from the position described by f
	f2 in turn is each of the moves from position described by f
	and they will be negated and inserted into D2.
*/
	//System.out.println("\n\n\n\n");
	//System.out.println("r1 = " + r1);
	//System.out.println("\n\n\n\n");
	//System.out.println("val = "+ val);
	//System.out.println("turn = " + turn);
	//System.out.println("target = " + tar);
	
	Board B = new Board();

	
	// length-1 is correct.
	for(int i=0;i<r1.body.length-1;i++){  // For each clause in the body of r1 
		DBase d1 = r1.body[i].facts;      // Get the database of facts covered by the clause
		List<Fact> facts = d1.getFacts();		  // Get the facts in the database
		DBase d2 = new DBase();			  // d2 is new state database   		
											   
		System.out.println("Clause: " +	r1.body[i]);
		for(int j=0;j<facts.size();j++){	// for each fact in facts
			Fact f = (Fact)facts.get(j);	// f is the fact
			//System.out.println("--- Fact " + f);
			//Fact notF = f.negate();
			//System.out.println(notF);
			State s = new State(f,turn);	// s is the state relative to this fact
			//System.out.println(s);
			Move mm = vi.miniMaxMove(s);	// get the mini-max move for s
			//System.out.println("MiniMaxMove " + mm );
//			Move bm = vi.bestMove(s);
//			System.out.println("BestMove " + bm);
			Fact f1 = f.move(mm,tar);		// get a new fact f1 for this move
//			System.out.println("New Fact " + f1);
			//d2.insert(notF);
			d2.insert(f1);				    // put fact from new state into d2

			
			List<Move> moves = B.getValidMoves(s);			// get valid moves for this state
			for(int k=0;k<moves.size();k++){			// For each move
				Move m = moves.get(k);			
				//System.out.println("Move: " + m);
				Fact f2 = f.move(m,tar);
				State s2 = new State(f2,1-turn);		// Make a new state for other player's turn
				double v = vi.value(s2);				// get the value for new turn.
				//System.out.println(s2 + ": " + v);
				Fact notF2 = f2.negate();				// negate all the facts that are not the next mini-max move
				if (v < val)		// what should be done if v == val?? They should not be inserted.
				{
					d2.insert(notF2);					// insert all facts that have value less than mini-max move
					//System.out.println("Inserting");
				}
				//else System.out.println("Not inserting");
			}// for each move

		} // for each fact
		// System.out.println(d2);
		Predicate target = new Predicate(tar,6);
		FoilImp f = new FoilImp(target,d2,false);
		f.learn();
		
		System.out.println();
		System.out.println();
		System.out.println();

	}// for each clause

} // method transform


public static void doLearn(int x,int y) throws Exception
{
	String pname = "CheckmateJ";
	DBase J = new DBase("ValueJ.db",pname,10,BLACK);
	Predicate target = new Predicate(pname,6);
	System.out.println("Calculating FOIL");
	FoilImp F = new FoilImp(target,J,true);
	
	System.out.println("******");
	System.out.println("*"+x+","+y+"**");
	System.out.println("******");

//	F.getPredicateSet(F.D,F.ps,x,y);
	F.getPredicateSet(F.D,F.ps);
	F.learn();
}

/*
/// This main is for feature creation
public static void main(String[] args) throws Exception{
	doLearn(-1,0);
	
//	doLearn(0,0);
//	doLearn(1,0);
  	doLearn(2,0); // Better
	doLearn(3,0); // Better
//	doLearn(1,1);
	doLearn(2,1); // Better
//	doLearn(3,1);
	doLearn(2,2); // Better
	doLearn(3,2); // Better
//	doLearn(3,3);
} // void main


 */


/// This main is for mate in 1 transform
/*
public static void main(String[] args) throws Exception{

	String pname = "mate1WJ";
	DBase J = new DBase("ValueJ.db",pname,9,WHITE);
	Predicate target = new Predicate (pname,6);
	System.out.println("Calculating FOIL");
	FoilImp F1 = new FoilImp(target,J,true);
    F1.learn();
    System.out.println("\n\n\n");
	Rule r1 = F1.bestRule;

	ValueIterator VI = new ValueIterator();

	for(int v=0;v<10;v++){
		// System.out.println("\nRound: " + (v+1));
		VI.Iterate();
	}


	transform(VI,r1,1.0,State.WHITE,"mate1T");

}
*/

// This is main for mate in 2 transform
public static void main(String[] args) throws Exception{

	String pname = "mate2WJ";
	DBase J = new DBase("ValueJ.db",pname,8,WHITE);
	Predicate target = new Predicate (pname,6);
	System.out.println("Calculating FOIL");
	FoilImp F1 = new FoilImp(target,J,true);
    F1.learn();
    System.out.println("\n\n\n");
	Rule r1 = F1.bestRule;

	ValueIterator VI = new ValueIterator();

	for(int v=0;v<10;v++){
		// System.out.println("\nRound: " + (v+1));
		VI.Iterate();
	}


	transform(VI,r1,.9,State.WHITE,"mate2T");

}	

/**/

	public static void junkMain()
	{
	
	
//		String pname = "mate2BJ";
//		DBase J = new DBase("ValueJ.db",pname,8,BLACK);

//		String pname = "CheckmateJ";
//		DBase J = new DBase("ValueJ.db",pname,10,BLACK);
/**/
/*		System.out.println(J);
		Predicate target = new Predicate (pname,6);
		System.out.println("Calculating FOIL");
		FoilImp F = new FoilImp(target,J,true);
        F.learn();
/**/
/*        Rule r2 = F.bestRule;
*/
/* 
		String pname = "mate1WJ";
		DBase J = new DBase("ValueJ.db",pname,9,WHITE);
		Predicate target = new Predicate (pname,6);
		System.out.println("Calculating FOIL");
		FoilImp F1 = new FoilImp(target,J,true);
        F1.learn();
        System.out.println("\n\n\n");
        
		Rule r1 = F1.bestRule;

/*         
    	String pname = "mate2WJ";
		DBase J = new DBase("ValueJ.db",pname,8,WHITE);
		Predicate target = new Predicate (pname,6);
		System.out.println("Calculating FOIL");
		FoilImp F1 = new FoilImp(target,J,true);
        F1.learn();
        
		Rule r3 = F1.bestRule;
*/				
  /*
		String pname = "mate1BJ";
		DBase J = new DBase ("ValueJ.db",pname,9,BLACK);
		Predicate target = new Predicate(pname,6);
		System.out.println("Calculating FOIL");
		FoilImp F1 = new FoilImp(target,J,true);
		F1.learn();
		
		Rule r4 = F1.bestRule;
	*/	
	
/*	
		ValueIterator VI = new ValueIterator();

		for(int v=0;v<10;v++){
			// System.out.println("\nRound: " + (v+1));
			VI.Iterate();
		}
		
		transform(VI,r1,1.0,State.WHITE,"mate1T");
	//	transform(VI,r3,.9,State.WHITE,"mate2T");
		// this is the right one.
*/
	} 

} // class Foil





