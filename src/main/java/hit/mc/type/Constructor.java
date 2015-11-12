package hit.mc.type;

/**
 * @author lacus
 */
public class Constructor extends Type {

	Type domain,range;	

	protected Constructor(Type d, Type r) {
		super(CONSTRUCTOR, 0);
		domain = d;
		range = r;
		System.out.println("   NEW CONSTRUCTOR: "+this+" (DOMAIN: "+domain+", RANGE: "+range+")");
	}

	public Type getDomain(){
		return  domain;
	}

	public Type getRange(){
		return  range;
	}

	public String toString(){
		return  ""+tag+domain+range;
	}

	protected String typeString(){
		return "constructor: "+range.typeString()+"("+domain.typeString()+")";
	}
}
