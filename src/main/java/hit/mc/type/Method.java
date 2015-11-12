package hit.mc.type;

/**
 * @author lacus
 */
public class Method extends Type {

	Type domain,range;	

	protected Method(Type d, Type r) {
		super(METHOD, 0);
		domain = d;
		range = r;
		System.out.println("   NEW METHOD: "+this+" (DOMAIN: "+domain+", RANGE: "+range+")");
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
		return "method: "+range.typeString()+"("+domain.typeString()+")";
	}
}
