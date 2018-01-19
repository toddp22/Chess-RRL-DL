package Foil;
//import Term;
//import Constant;
import java.io.Serializable;
//import java.util.List;
import java.util.ArrayList;

// class Variable
class Variable extends Term implements Serializable{
	public static final long serialVersionUID = 0;
	public static final String[] VarNames = {"x","y","z","w","v","t","r","s"};
	public Constant binding = null;
	public VariableType type = null;
	public boolean useTypeName = false;

	public Variable (String name, VariableType vt){
		super(name);
		type = vt;
		useTypeName= true;
	} // constructor variable

	public Variable (String name){
		super(name);
	} // constructor Variable

	public VariableType type(){
		return type;
	} // method type;

	public Variable(){
		super(VarNames[0]);   // should this always be zero or should it be the current variable for uniqueness?
	} // constructor Variable

	// create a variable with a name not in nameList
	public Variable (String[] nameList){
		super(uniqueName(nameList));
	} // constructor Variable

	public Variable (ArrayList<String> nameList){
		super(uniqueName(nameList));
	} // constructor Variable

	public int hashCode(){
		if (bound()) return binding.hashCode();
		else return name.hashCode();
	} // method hashCode

	public void bind (Constant C){
		binding = C;
	} // method bind

	public static String uniqueName(ArrayList<String> nameList){
		String name;

		if (nameList == null) return VarNames[0];

		for(int c=0;c<1000;c++){
			for(int i=0;i<VarNames.length;i++){
				if (c > 0) name = VarNames[i] + c;
				else name = VarNames[i];
				if (!nameList.contains(name)) return name;
			} // for
		}// for count

		return "NoMoreNames";

	} // method uniqueName


	// returns a unique name from VarNames not in nameList
	public static String uniqueName(String[] nameList){
		boolean match = false;
		String name;

		if (nameList == null) return VarNames[0];

		for(int c=0;c<1000;c++){
			for(int i=0;i<VarNames.length;i++){
				match = false;
				if (c > 0) name = VarNames[i] + c;
				else name = VarNames[i];
				for (int j=0;j<nameList.length;j++){
					if (name.equals(nameList[j])) {
						match = true;
						break;
					} // if match
				} // for
				if  (!match) return name;
			} // for
		}// for count

		return "NoMoreNames";
	} // method uniqueName

	public boolean bound(){
		return !(binding  == null);
	} // method bound

	public String toString(){
		if (useTypeName && type != null) return type.toString();
		if (binding == null)
			return name;
		else return "{" + name + "," + binding.toString() + "}";
	} // method toString


	/****************************
		Method rename:
			Simply change the variable name
	****************************/
	public void rename(String s){
		name = s;
	} // method rename


	/***************************************
		Method substitute:
			Substitute this variable if it
			equals v with term t

			Note:
				This will not work properly if v or this is
				bound.
	***************************************/
	public void substitute(Variable v,Term t){

		if (this.equals(v)){
			if (t instanceof Variable)
				rename(t.name);
			else if (t instanceof Constant)
				bind((Constant)t);
		} // if
	} // method substitute

	public boolean equals(Object o){
		//System.out.println("Variable.Equals(object)");
		if (o instanceof Variable)
			return equals((Variable) o);
		if (o instanceof Constant)
			return equals((Constant) o);
		if (o instanceof String)
			return super.equals((String) o);


		return false;
	} // method equals

	public boolean equals(Constant c){
		// System.out.println("Variable.equals(Constant)");
		if (binding == null) return false;
		else return binding.equals(c);
	} // method equals

	/**********************************
		Method match returns true if the two
		objects are unifiable.
	**********************************/

	public boolean match(Constant c){
		if (binding == null) return true;
		return binding.match(c);
	} // method match

	public boolean match(Variable v){
		if (!bound() || !v.bound())
			return true;
		else
			return binding.match(v.binding);
	} // method match


	// HERE!! Need to check this!!
	public boolean equals(Term v){
		// System.out.println("Var.equals(Term)");
		//System.out.println(v);
		if (v instanceof Variable)
			return equals((Variable)v);
		else if  (v instanceof Constant)
			return bound() && binding.equals((Constant)v);

			// return super.equals(v);
			return false;
	} // method equals

	// need to determine exactly what this means
	// are bound variables equal to unbound variables??
  // I would say no.
	public boolean equals(Variable v){
		// System.out.println("Var.equals(var)");
		if (bound() && v.bound())
			return binding.equals(v.binding);
		if (!bound() && !v.bound())
			return super.equals(v.name);
		else
			return false;
	} // method equals

	public void release (){
		binding = null;
	} // method release


	// again need to be careful about naming of variables
	// HERE I think this is wrong.
	public Term LGG (Term v, String[] nameList){
	// not sure if this definition is correct
	// do we need to return a new variable with this same binding?
		// System.out.println("LGG:" + this + ", " + v);
		if(v instanceof Variable) {
			Variable v1 = (Variable) v;          /// sloppy sloppy sloppy. Need to figure out a way not to need introspection
			if(equals(v1)) {
				// System.out.println("Equal");
				substitute(this,new Variable(uniqueName(nameList)));
				return this;
			}

		}else if (equals(v)) {
			// System.out.println("Equal");
			return this;
		}

		return (new Variable(nameList));
	} // method LGG


	public Term LGG (Term v, ArrayList<String> nameList){
	// not sure if this definition is correct
	// do we need to return a new variable with this same binding?
		// System.out.println("LGG:" + this + ", " + v);
		if(v instanceof Variable) {
			Variable v1 = (Variable) v;          /// sloppy sloppy sloppy. Need to figure out a way not to need introspection
			if(equals(v1)) {
				// System.out.println("Equal");
				substitute(this,new Variable(uniqueName(nameList)));
				return this;
			}

		}else if (equals(v)) {
			// System.out.println("Equal");
			return this;
		}

		return (new Variable(nameList));
	} // method LGG


	public Term LGG (Term v){
		String[] names = {name,v.name};
		return LGG(v,names);
	} // method LGG


	public static void main(String[] args){
		Variable v = new Variable("v");
		Variable x = new Variable("x");
		Term t = v.LGG(x);
		System.out.println(t);

		Term t1 = x.LGG(new Constant("Bob"));
		System.out.println(t1);

		x.bind (new Constant("Bob"));
		
		t = v.LGG(x);
		System.out.println(t);
		v.bind (new Constant("Bob"));
		Term t2 = v.LGG(x);

		if(v.equals(x)) System.out.println("They're equal");

		//String[] names = {"x","y","v"};
		//String[] names2 = null;
		//String[] names3 = {"x","y","z","w","v","t","r","s","x1","y1","z1","w1","v1","t1","r1","s1"};

		/*
		System.out.println("Unique Name from " + "{\"x\",\"y\",\"v\"}" + "= " + v.uniqueName(names));
		System.out.println("Unique Name from " + "{}" + "= " + v.uniqueName(names2));
		System.out.println("Unique Name from " + "Full Array" + "= " + v.uniqueName(names3));
		*/

		System.out.println(v);
		System.out.println(x);
		System.out.println(t);
		System.out.println(t1);
		System.out.println(t2);
	} // method main

} // class Variable


