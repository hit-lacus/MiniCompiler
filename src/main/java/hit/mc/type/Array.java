package hit.mc.type;

/**
 * 数组类型
 * @author lacus
 */
public class Array extends Type {

	/**数组大小*/
	int size;
	/**数组元素类型*/
	Type base;	

	/**
	 * 
	 * @param s 数组大小
	 * @param b 数组元素
	 */
	protected Array(int s, Type b) {
		super(ARRAY, s * b.getWidth());
		size = s;
		base = b;
		System.out.println("构建新数组：" + this + " (大小: " + size + ", 元素: " + base + ", 占据内存空间为"+width+")");
	}

	public int getSize(){
		return size;
	}

	public Type getBase(){
		return base;
	}

	public String toString(){
		return ""+tag+size+base;
	}

	protected String typeString(){
		return base.typeString()+"[]";
	}
}
