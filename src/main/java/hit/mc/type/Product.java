package hit.mc.type;

/**
 * @author lacus
 */
public class Product extends Type {

	Type left,right;	

	protected Product(Type l, Type r) {
		super(PRODUCT, l.getWidth() + r.getWidth());
		left = l;
		right = r;
		System.out.println("   NEW PRODUCT: "+this+" (LEFT: "+left+", RIGHT: "+right+", WIDTH: "+width+")");
	}

	public String toString(){
		return  ""+tag+left+right;
	}

	protected String typeString(){
		return left.typeString()+", "+right.typeString();
	}
}
