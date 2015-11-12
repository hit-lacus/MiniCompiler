package hit.mc.type;

/**
 * @author lacus
 */
public class Reference extends Type {

	Type referred;	

	protected Reference(Type r) {
		super(REFERENCE, 4);
		referred = r;
		System.out.println("新的引用：" + this + "(引用了: " + referred + ", 占用空间" + width + ")");
	}

	public Type getReferred(){
		return referred;
	}

	public String toString(){
		return ""+tag+referred;
	}

	protected String typeString(){
		return "*"+referred.typeString();
	}
}
