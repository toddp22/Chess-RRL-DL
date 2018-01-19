package Foil;
import java.io.Serializable;

class VarConstraint implements Constants, Serializable{
	public static final long serialVersionUID = 0;	
	private int [] constraint;    // example: [COLOR,PIECE]
	//private boolean shortName = true;

	public VarConstraint (int [] constraint){
		this.constraint = constraint;
	} // constructor VarConstraint

	public String toString(){
		if (constraint == null) return "none";
		String retStr = "";
		for (int i=0;i<constraint.length;i++){
			retStr += constString[i];
			if (i < constraint.length-1) retStr += " ";
		} // for

		return retStr;
	} // method toString

	public boolean equals (Object o){
		if (o instanceof VarConstraint)
			return equals((VarConstraint) o);

		return false;
	} // method equals: object

	public boolean equals(VarConstraint vc){
		if (constraint == null && vc.constraint == null) return true;
		else if (constraint == null || vc.constraint == null) return false;
		else if (constraint.length != vc.constraint.length) return false;

		for(int i=0;i<constraint.length;i++){
			if (constraint != vc.constraint) return false;
		} // for

		return true;
	} // method equals: VarConstraint

	public boolean compatible(Variable v1, Variable v2){
		VariableType vt1 = v1.type(), vt2 = v2.type();

		if (constraint == null) return true;

		for (int i=0;i<constraint.length;i++){
			if(!vt1.compatible(vt2,constraint[i])) return false;
		} // for

		return true;
	} // method compatible

} // class VarConstraint