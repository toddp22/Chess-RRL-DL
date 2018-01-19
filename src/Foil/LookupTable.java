package Foil;
import java.util.Hashtable;

public class LookupTable {
	public class Entry {
		private Term t1;
		private Term t2;
	
		public Entry(Term t1,Term t2){
			this.t1 = t1;this.t2 = t2;
		} // constructor Entry
	
		public boolean equals(Term t1,Term t2){
			return (this.t1.equals(t1) && this.t2.equals(t2));
		} // method equals
		
		public boolean equals(Object o){
			if (o instanceof Entry){
				Entry e = (Entry)o;
				return equals(e.t1,e.t2);
			} // if
			else return false;
		} // method equals
		
		public int hashCode(){
			return t1.hashCode() + t2.hashCode();
		} // method hashCode
		
		public String toString(){
			return "[" + t1.toString() + "," + t2.toString() + "]";
		} // method toString
	
	} // class entry
	
	private Hashtable<Entry,Term> table;
	
	public LookupTable(){
		table = new Hashtable<Entry,Term>();
	} // constructor LookupTable
	
	public boolean contains(Term t1, Term t2){
		return table.containsKey(new Entry(t1,t2));
	} // method contains
	
	public void insert(Term t1, Term t2, Term t3){
		if (!contains(t1,t2)) {
			table.put(new Entry(t1,t2),t3);
			// table.put(new Entry(t2,t1),t3);          /// This should be right, however right now it does more harm than good.
		}// if
	} // method insert
	
	public Term get(Term t1, Term t2){
		return (Term)table.get(new Entry(t1,t2));
		
	} // method get
	
	public String toString(){
		return "LookupTable: " + table.toString();	
	} // method toString
	
	
	public static void main(String[] args){
		LookupTable LT = new LookupTable();
		
		Term t1 = new Constant("t1");
		Term t2 = new Constant("t2");
		//Entry e1 = new Entry(t1,t2);
		Term t3 = new Constant("t1");
		Term t4 = new Constant("t2");

		Term v = new Variable();
		
		System.out.println("t1 = " + t1 + ", t2 = " + t2 + ", v = " + v);
		
		LT.insert(t1,t2,v);
		
		System.out.println(t1.equals(t3));
		
		Term T2 = LT.get(t3,t4);
		
		System.out.println("t3 = " + t1 + ", t4 = " + t2 + ", T2 = " + T2);
		System.out.println(LT);
	} // method main
	
} // class LookupTable
	

	

