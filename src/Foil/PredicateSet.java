package Foil;

class PredicateSet{
	private Pred[] plist;
	private int count;
	boolean checkNameOnly = true;

	public PredicateSet(){
		count = 0;
	} // constructor PredicateSet

	public int size(){
		return count;
	} // method size

	public Object get(int i){
		return plist[i];
	} // method get


	public PredicateSet(boolean CNO){
		count = 0;
		checkNameOnly = CNO;
	} // constructor PredicateSet

	public boolean Contains(Pred p){

		if (plist == null) return false;

		for(int i=0;i<count;i++){
			if(plist[i].equals(p)) return true;
		} // for

		return false;
	} // Contains

	public boolean Contains(String name){
		if (plist == null) return false;

		for(int i=0;i<count;i++){
			if(plist[i].name().equals(name)) {
				return true;
			} // if
		} // for

		return false;
	} // method Contains

	public void Add(Pred p){
		if(checkNameOnly){
			if (Contains(p.name())) return;
		}
		else
			if (Contains(p)) return;

		// grow array
		if (plist == null) plist = new Pred[5];
		else if (count == plist.length){
			Pred[] temp = new Pred[plist.length+5];
			for(int i=0;i<plist.length;i++) temp[i] = plist[i];
			plist = temp;
		}//
		plist[count] = p;
		count++;

	} // method Add

	public String toString(){
		String pString = "Predicate Set: [";
		for (int i=0;i<count-1;i++){
			if(i%5 == 4) pString += "\n                ";
			pString += plist[i].toString() + ", ";
		}// for

		if(count > 0)
			pString += plist[count-1].toString();
		pString += "]";
		return pString;

	} // method toString

} // class PredicateSet

