package Foil;
// class Rule

import java.io.IOException;
import java.io.PrintWriter;

/*
	Ways to speed things up.
	1. If we know that  Checkmate(x,y,z,w,v,u) -: Equal:(x,v)
	   matches certain facts, we shouldn't have to get these facts over and over again.
		 Any fact not matching the body should be eliminated.

			Question:  Does this always work -- is there a situation where a fact which doesn't
			match the first part of the body could be used subsequently in a new term to be investigated
			in the body.

			At the very least, the number of positive bindings can be searched as a subset of the
			prior rule under consideration.


			Get Positive Matches:
			Algorithm:
			  1. Get matches for the Head
				2. create a binding for each Fact that matches the head
				3. For each unbound variable in the body find a fact that matches a predicate in the body.

			This could be speed up considerably by simply searching the positive database.
			We need to get possible bindings from the negative database.
			However if all of the predicates are equal or not equal this should be able to be
			sped up considerably by simply creating subsets.

			We shouldn't be checking the negative database at all with a ~Equal predicate.



	2. We should be able to know what the bindings are also.
*/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



class Rule implements Serializable, Pred{
	public static final long serialVersionUID = 0;	
	public static final double NOT_DEFINED = -20002;
	public Clause[] body = new Clause[1];
	public Predicate head;
	public double entropy = NOT_DEFINED;
	public int ac = 0;   // active clause;
	public boolean predRule = false;
	public boolean brief = true;



/***************************************
	Pred Section
***************************************/
	public boolean Special(){
		//System.out.println("Im special");
		return true;
	}// method Special

	public boolean match(Fact f){
		boolean retval = false;
		//System.out.println(this);
		//System.out.println("Matching Fact: " + f);
		if (f.args.length != head.argCount)
				retval= false;

		else {
			for(int i=0;i<head.argCount;i++){
				head.args[i].bind(f.args[i]);
			}// for

			for (int c=0;c<body.length;c++){
				// System.out.println("Checking Clause #" + (c+1));
				if (body[c] != null && body[c].specialMatches(head,f)){
					retval = true;
					break;
				}//  if
			}
		}

		if(!head.not)
			return retval;
		else
			return !retval;



	} // method match
	public String name(){
		return head.name();
	} // method name

	public Term arg(int i){
		return head.arg(i);
	}// method arg

	public int argCount(){
		return head.argCount();
	} // method argCount

	public void release(){
		head.release();
	} // method release

	// This doesn't return a clone
	// rather this just creates a predicate of the head
	// with name and arity
	public Pred Clone(){
		Rule r = new Rule(this);
		r.predRule=predRule;
		return r;
	} // method Clone

	//  This doesn't do anything cuz we shouldn't need
	//  to add terms in this case.
	public void addTerm(Term t){
		return;

	}// public method addTerm

	public boolean containsVarFrom(Pred p){
		return head.containsVarFrom(p);

	}// public method containsVarFrom

	public boolean not(){
		return head.not();
	} // method not

	public void bind(Fact f){
		head.bind(f);
	} // method bind


	public Variable[] getVars(Variable[] vs){
		return head.getVars(vs);
	}// method getVars

	public void negate(){
		head.negate();
	} // method negate

	public ArrayList<Pred> permute(ArrayList<Variable> varList){
		ArrayList<Pred> retList = new ArrayList<Pred>();
		Pred r = Clone();
		retList.add(r);
		r = Clone();
		r.negate();
		retList.add(r);
		return retList;

	} // method permute
/***************************************
	End of Pred Section
***************************************/

	public void setMatches(ArrayList<Fact> flist){
		if (flist == null) return;
		if (body[ac] == null) return;
		body[ac].setMatches(flist);
	};

	public void printMatches(int cl){
		System.out.println("clause " + cl);
		if(body[cl] != null) body[cl].printMatches();
	};

	public void printMatches(){
		if(body[ac] != null) body[ac].printMatches();
	};

	public void newActiveClause(){
		Clause[] temp = new Clause[body.length +1];
		for (int i=0;i<body.length;i++){
			temp[i] = body[i];
			temp[i].facts = body[i].facts;
		}
		body = temp;
		ac++;
	} // method newActiveClause


	// Returns the number of facts in P that matches the rule

	// We also need to test those predicates in the body that are the
	// equals predicate.
	public ArrayList<Fact> getMatches(DBase D){
		//System.out.println(this);
		ArrayList<Fact> facts = D.getMatches(head);

		// System.out.println("The positive matches are: " + pfacts);
		//int count = 0;
		if (body[ac] == null) return facts;

		Iterator<Fact> factIt = facts.iterator();

		while(factIt.hasNext()){
		// for(int f=0;f<pfacts.length;f++){
			Fact f = (Fact)factIt.next();
			head.bind(f);
			// special case for equals.
			
			if(!body[ac].specialMatches(head,f)){
				//facts[f] = null;
				factIt.remove();
				head.release();
				continue;
			}
			head.release();
		} // for
		//System.out.println("Matching Facts: " + pfacts);
		//System.out.println("pfacts: " + pfacts);

		return facts;
	} // method getPositiveMatches

	public void calcEntropy(DBase P, DBase N, PrintWriter out) throws IOException{
		int p = 0;
		int n = 0;
		// get positive matches
		ArrayList<Fact> pfacts = getMatches(P);
		p = pfacts.size();
		System.out.println(this);
		out.println("#Positive Matches = " + p);
		out.println(pfacts);
		if(p <= 0){
			entropy = -1;
			return;
		} // if

		// get negative matches
		ArrayList<Fact> nfacts = getMatches(N);	// if we don't use the negative database, then we need to enumerate
		//ArrayList<Fact>	nfacts = D.getNegativeMatches(P,N);																 	// all possible bindings
		out.println("Negative Facts" + nfacts);
		out.println("Number of Negative Facts: "+ nfacts.size());
		//head.release();
		if (body[ac] == null) n = nfacts.size();
		else{
			Iterator<Fact> factIt = nfacts.iterator();
			while(factIt.hasNext()){
		//	for(int f=0;f<nfacts.length;f++){
				Fact f = (Fact)factIt.next();
				head.bind(f);

				//System.out.println("head:" + head);
				//System.out.println("body:" + body[ac]);
				// special case for Special.
				if(!body[ac].specialMatches(head,f)){
					head.release();
					continue;
				}
				else{
					n ++;
					// System.out.println(this);
				}
				head.release();
			} // for
		}//else
		// System.out.print("p = " + p + ", n = " + n + "   ");
		entropy = -1 * log2((double)p/(p+n));
		if(entropy == 0.0) entropy = 0.0;  // Silly thing to do to not have java print -0.0
		System.out.println("entropy = " + entropy);
	} // method calcEntropy

	public Rule(){
	} // constructor Rule

	public Rule(Predicate p){
		head = p;
	} // constructor Rule


	// count the number of unique variables in rule.
	public int countVars(){
		Variable[] vs = new Variable[0];
		vs = head.getVars(vs);

		if(body[ac] == null) return vs.length;
		vs = body[ac].getVars(vs);

		return vs.length;
	} // method countVars

	public Rule (Rule r){
		head = new Predicate(r.head);
		body = new Clause[r.ac+1];
		for(int c=0;c<r.body.length;c++){
			ac = c;
			if(r.body[c] == null) return;
			if(r.body[c].predicates == null) return;
			for(int i=0;i<r.body[c].predicates.size();i++){
				addPred((Pred)r.body[c].predicates.get(i));
			}
		body[c].facts = r.body[c].facts;

		}
	} // constructor Rule

	public boolean hasPred(Pred p){
		if (body[ac] == null) return false;

		return body[ac].hasPred(p);
	} // method hasPredicate

	public void setVarList(Variable[] varList){
		if(varList == null){
				System.out.println("bad varList");
				return;
		}
		if (head == null) return;
		head.setVarList(varList);
		if (body == null) return;
		for(int i=0;i<body.length;i++){
			if(body[i] != null)
				body[i].setVarList(varList);
		} // for
	} // method setVarList

	public void addPred(Pred p){
		if (head == null) {
			if (p instanceof Predicate){
				head = (Predicate)p;
			}
			return;
		}// if
		else{
			if(body[ac] == null) body[ac] = new Clause();
			body[ac].addPred(p);
		} // else
	} // method addPredicate

/*
	/*********************************
		LGG: Example
			 daughter(mary,ann) <- female(mary),parent(ann,mary)
		     daughter(eve,tom)  <- female(eve) ,parent(tom,eve)
		-------------------------------------------------------
			 daughter(X,Y) <- female(X),parent(Y,X)

		Algorithm:
			LGG on head, saving variable Names and LGG of terms
			LGG on body.
			Works only on active clause for each rule.

		To Do:
			LGG with multiple clauses.

	*********************************/
	
/*
	public Rule LGG (Rule r){
		return LGG(r,new LookupTable(),new ArrayList<String>());
	} // method LGG

	public Rule LGG(Rule r,LookupTable LT,ArrayList<String> nameList){
		Rule newR= new Rule();

		// LGG on head (predicate)
		if (head != null && r.head != null)
			newR.head = head.LGG(r.head,nameList,LT);

		// LGG on body
		if (body[ac] != null && r.body[ac] != null)
			newR.body[ac] = body[ac].LGG(r.body[ac],nameList,LT);

		return newR;
	} // method LGG
*/
	/************************************************************
		Method: RLGG
			Relative Least General Generalization
			Take two Predicates (Facts) Return Rule

		Algorithm:
			LGG of Heads
			Body = Cross Product of positive instances
			Remove those not ijDeterminant
			return new Rule.

	************************************************************/
/*
	public Rule RLGG(Rule r,DBase Pos){

		if (r.body[ac] != null || body[ac] != null) return null;
		LookupTable LT = new LookupTable();
		ArrayList<String> nameList = new ArrayList<String>();

		Rule newR = LGG(r,LT,nameList);
		//System.out.println(nameList);

		newR.body[ac] = Pos.LGG(LT,nameList);  // cross product

		newR.ijDeterminant(1,2);  // depth,degree


		return newR;
	} // method RLGG
*/
	public void ijDeterminant(int i,int j){
		if (head == null || body[ac] == null) return;
		ArrayList<String> varList = head.termNames();
		body[ac].ijDeterminant(i,j,varList);

	} // method ijDeterminant

	public static double log2(double n){
		return Math.log(n)/Math.log(2);
	} // method log2

	public int getT(Rule newR,DBase P,DBase N){
		// get covered facts from both "this" and newR.
		List<Fact> pfacts = getMatches(P);
		List<Fact> npfacts = newR.getMatches(P);

		int count = 0;

		// check to see which of them are the same.
		for(int i=0;i< pfacts.size();i++){
			for(int j=0;j< npfacts.size();j++){
				Fact f1 = pfacts.get(i);
				Fact f2 = npfacts.get(j);
				if(f1.equals(f2)) {
					count ++;
					break;
				} // if 
			}// for
		} // for

		return count;
	} // method getT

	public double Foil_Gain(Rule newR,DBase P, DBase N, PrintWriter out) throws IOException{
		// figure out what t is
		int t = getT(newR,P,N);
		if (entropy == NOT_DEFINED) calcEntropy(P,N,out);
		if (newR.entropy == NOT_DEFINED) newR.calcEntropy(P,N,out);
		if (newR.entropy == -1) t = 0;
		//System.out.println("Gain is: " + (t * (entropy - newR.entropy)));
		return t * (entropy - newR.entropy);

	} // method Foil_Gain


	public String toString(){
		String str = "";
		if(predRule) {
			head.headRule = true;
			return head.toString();
		} // if
		if(head != null) str += head.toString() + " -: ";

		int i;
		if (brief) i = ac;
		else i = 0;
		for(;i<=ac;i++)
			if(body[i] != null) {
				if(i > 0) {
					if(!brief) str += "\n";
					String headStr = head.toString();
					if (!brief) for (int sp = 0;sp < headStr.length()+4;sp++) str += " ";

					else str += "...";
				} // if we are the second clause
				str += body[i].toString();
			} // if
		return str;
	} // method toString

	/**********************************************
		int degree:
			returns the maximum degree of each of the predicates in the body of the active clause
			See predicate.java: degree for definition of degree.
	*********************************************/
	public int degree(){
		if (head == null) return 0;
		if (body[ac] == null) head.degree();
		return body[ac].degree();
	} // method degree

	/*************************************************
		int depth:
			returns the maximum depth of each of the predicates in the body
			a predicate whose variables are all in the head have a depth of 0.
			variables which are not in the head that only depend on
			depth 0 variables (variables in the head) have degree 1.
			etc.
	**************************************************/
	public int depth (){
		if (head == null || body == null) return 0;
		ArrayList<String> varList = head.varNames();
		return body[ac].depth(varList);
	} // method depth



	/**************************************************
			LGG: Example
			 daughter(mary,ann) <- female(mary),parent(ann,mary)
		     daughter(eve,tom)  <- female(eve) ,parent(tom,eve)
		-------------------------------------------------------
			 daughter(X,Y) <- female(X),parent(Y,X)
	**************************************************/

	
	public static void main(String[] args){
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

		DBase Family = new DBase();
		DBase GD = new DBase();

		for(int i=0;i<fact.length;i++){
			// System.out.println(fact[i]);
			if(fact[i].name.equals("GrandDaughter"))
				GD.insert(fact[i]);
			else
				Family.insert(fact[i]);
		}// for

		System.out.println(GD);
		System.out.println(Family);

		ArrayList<Rule> ruleList = new ArrayList<Rule>();



/*
	HERE!!!
	Not possible to do this with a DBase
		for (int i=0;i<GD.length;i++){
			for(int j=0;j<GD.length;j++){
				if (i == j) continue;
				Rule r1 = new Rule(new Predicate(GD.fact[i]));
				Rule r2 = new Rule(new Predicate(GD.fact[j]));

				System.out.println(r1);
				System.out.println(r2);

				Rule r3 = r1.RLGG(r2,Family);
				ruleList.add(r3);
				System.out.println(r3);
			} // for j
		} // for i
*/

		System.out.println(ruleList);

		Iterator<Rule> it = ruleList.iterator();
		while(it.hasNext()){
			//Rule r1 = (Rule)it.next();
			Iterator<Rule> it2 = ruleList.iterator();
			while(it2.hasNext()){
				//Rule r2 = (Rule)it2.next();
//				Rule r3 = r1.LGG(r2);

//				System.out.println(r3);
			} // it2
		} //it

		/*
		Predicate p1 = new Predicate("daughter",new String[] {"x","y"});
		Predicate p2 = new Predicate("female",new String[] {"z"});
		Predicate p3 = new Predicate("parent",new String[] {"y","z"});
		Predicate p4 = new Predicate("parent",new String[] {"z","w"});
		Rule r1 = new Rule();
		r1.head = p1;
		r1.addPredicate(p2);
		r1.addPredicate(p3);
		r1.addPredicate(p4);

/*
		p1.args[0].bind(new Constant("mary"));
		p1.args[1].bind(new Constant("ann"));
		p2.args[0].bind(new Constant("mary"));
		p3.args[0].bind(new Constant("ann"));
		p3.args[1].bind(new Constant("mary"));
*/
/*
		Predicate p1a = new Predicate("daughter",new String[] {"x1","y1"});
		Predicate p2a = new Predicate("female",new String[] {"z1"});
		Predicate p3a = new Predicate("parent",new String[] {"w1","v1"});
*/
/*
		p1a.args[0].bind(new Constant("eve"));
		p1a.args[1].bind(new Constant("tom"));
		p2a.args[0].bind(new Constant("eve"));
		p3a.args[0].bind(new Constant("tom"));
		p3a.args[1].bind(new Constant("eve"));

*/
/*
		Rule r2 = new Rule();
		r2.head =p1a;
		r2.addPredicate(p2a);
		r2.addPredicate(p3a);
*/
/*
		System.out.println("r1: " + r1);
		System.out.println("r2: " + r2);
*/
		//Rule r3 = r1.LGG(r2);

		//System.out.println("r3: " + r3);

		//System.out.println("Degree of r3: " + r3.degree());
		//System.out.println("Depth of r3: " + r3.depth());

	/*
		System.out.println("Depth of r2: " + r2.depth());
		System.out.println("r1: " + r1);
		System.out.println("Depth of r1: " + r1.depth());
*/
	} // method main


} // class Rule
