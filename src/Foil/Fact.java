// class Fact

package Foil;

import java.io.Serializable;
import java.util.ArrayList;
//import java.util.List;

import VI.Board;
import VI.Move;
import VI.State;



public class Fact implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -998148755361579258L;
	public final Constant [] args;	  // arguments of the fact for ... 
	public final String name;		  // name of the fact
	public final Object value;        // value of the Fact

	public Fact(String name, String arg, Object v){
		this.name = name;
		this.args = new Constant[1];
		args[0] = new Constant(arg);
		this.value = v;
	} // constructor Fact


	public Fact(String name, String arg, boolean v){
		this.name = name;
		this.args = new Constant[1];
		args[0] = new Constant(arg);
		this.value = new Boolean (v);
	} // constructor Fact

	public Fact (String name, Constant[] args, Object v){
		this.name = name;
		this.args = args;
		this.value = v;
	} // constructor Fact

	public Fact (String name, Constant[] args, boolean v){
		this.name = name;
		this.args = args;
		this.value = new Boolean(v);
	} // constructor Fact

	public Fact (String name, String[] args, boolean v){
		this.name = name;
		Constant [] temp = new Constant[args.length];
		for (int i=0;i<temp.length;i++) temp[i] = new Constant(args[i]);
		this.args = temp;
		this.value = new Boolean(v);
	} // constructor Fact

	public Fact (String name, int[] args, boolean v){
		this.name = name;
		Constant [] temp = new Constant[args.length];
		for(int i=0;i<temp.length;i++)
			temp[i] = new Constant(Integer.toString(args[i]));
		this.args = temp;
		this.value = new Boolean(v);
	} // constructor Fact


	public Move getMove(Rule r,Board b,int turn){

		State s = new State(this,turn);
 
		ArrayList<Move> moves = b.getValidMoves(s);
		for(int k=0;k<moves.size();k++){
			Move m = moves.get(k);
			System.out.println("Move: " + m);
			Fact f = move(m);
			if (r.match(f)) return m;
		} // for each move

		return null;
	} // getMove
	
	public Fact move(Move m) {
		return move(m,name);
	}


	public Fact move(Move m,String newName){
		//Fact f = new Fact(name,args,value);
		Constant [] Args = new Constant[args.length];
		for(int i=0;i<args.length;i++)
			Args[i] = args[i];
		switch(m.piece){
			case Board.BK: Args[0] = new Constant(Integer.toString(m.to.row));
						   Args[1] = new Constant(Integer.toString(m.to.col));  break;
			case Board.WK: Args[2] = new Constant(Integer.toString(m.to.row));
						   Args[3] = new Constant(Integer.toString(m.to.col));  break;
			case Board.WR: Args[4] = new Constant(Integer.toString(m.to.row));
						   Args[5] = new Constant(Integer.toString(m.to.col));  break;
		}
		return new Fact(newName,Args,value);
	} // method Move

	public void setArg(int a,Constant c){
		args[a] = c;
	} // method setArg

	public boolean match (Predicate p){
		if (!name.equals(p.name)) return false;
		if(p.args.length != args.length) return false;

		for (int i=0;i<args.length;i++)
			if (!p.args[i].match(args[i])) return false;

		return true;
	} // method match


	public boolean equals(Fact f){
	//System.out.print("Comparing: " + this);
	// System.out.println(" and " + f.name);
		if (!name.equals(f.name)) return false;
		if(f.args.length != args.length) return false;

		for (int i=0;i<args.length;i++){
			//if (args[i] instanceof Constant) System.out.println("Constant");
			if (!args[i].equals(f.args[i])) return false;
		}// for

		return true;
	} // method equals

	public boolean match (Function f){
		if (!name.equals(f.name)) return false;
		if(f.args.length != args.length) return false;

		for (int i=0;i<args.length;i++)
			if (args[i] != f.args[i]) return false;

		return true;
	} // method match

	public Fact negate(){
		Boolean v = (value == Boolean.FALSE)?Boolean.TRUE:Boolean.FALSE;
		return new Fact(name,args,v);
	} // method negate

	public Boolean evaluate(Predicate p){
		if (!match(p)) return null;

		if (p.not)
			return new Boolean(!((Boolean) value).booleanValue());
		else return (Boolean)value;
	} // method evaluate

	public Constant evaluate (Function f){
		if (!name.equals(f.name)) return null;
		if(f.args.length != args.length) return null;

		for (int i=0;i<args.length;i++)
			if (args[i] != f.args[i]) return null;

		return (Constant)value;
	} // method evaluate

/*	public Predicate LGG(Fact f){
		return LGG(f,new LookupTable(),new ArrayList<String>());
	} // method LGG

	public Predicate LGG(Fact f,LookupTable LT,ArrayList<String> nameList){

		if (name != f.name) return null;
		if (args.length !=  f.args.length ) return null;

		Predicate newP = new Predicate(name,args.length);

		for (int i=0;i<args.length;i++){
			 if (LT.contains(args[i],f.args[i]))
					newP.addTerm(LT.get(args[i],f.args[i]));
			 else{
					Term result = args[i].LGG(f.args[i],nameList);
					if(result instanceof Variable){
						LT.insert(args[i],f.args[i],result);
					} // Need to think about what happens if result is not a Variable
			 		newP.addTerm(result);
			 }
			 nameList.add(newP.args[i].name);

		} // for

		// System.out.println(LT);

		return newP;

	} // method LGG
*/
	public String toString(){
		String temp = "" + name + "(";
		for(int i=0;i<args.length-1;i++)
			temp += args[i].toString() + ", ";

		if(args.length > 0) temp += args[args.length-1].toString();
		temp += ")";   //+ " = " + value.toString();

		// negate string if necessary
		if(Boolean.class.isInstance(value)){
			if( ((Boolean)value).booleanValue() == false)
				temp = "~" + temp;
		}
		return temp;
	} // method toString


	public static void main(String[] args){

		Fact [] fact = new Fact[11];
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

	/*	Predicate p1 = fact[7].LGG(fact[8]);

		System.out.println("fact[7] = " + fact[7]);
		System.out.println("fact[8] = " + fact[8]);

		System.out.println("lgg(fact[7],fact[8]) = " + p1);
	*/

	} // method main
} // class Fact

