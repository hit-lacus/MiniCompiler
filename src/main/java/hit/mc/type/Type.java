package hit.mc.type;

import hit.mc.Context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * 描述并记录源程序中出现的类型
 * @author lacus
 */
public class Type {
	/** 字符型 */
	public static final int CHARACTER = 1;
	/** 整型 */
	public static final int INTEGER = 2;
	/** 浮点型 */
	public static final int FLOATING = 3;
	/** 浮点型 */
	public static final int BOOL = 4;
	/** 无类型 */
	public static final int VOIDTYPE = 5;
	/** 错误类型 */
	public static final int ERRORTYPE = 6;
	/** Name类型 */
	public static final int NAME = 7;
	/** 数组类型 */
	public static final int ARRAY = 8;
	/** 引用类型*/
	public static final int REFERENCE = 90;
	public static final int PRODUCT = 91;
	public static final int CONSTRUCTOR = 92;
	public static final int METHOD = 93;

	/** 静态变量，记录源程序中出现的类型*/
	static HashMap<String,Type> types = new HashMap<>();

	/** 标识*/
	int tag;
	/** 占用空间*/
	int width = 0;

	protected Type(int t, int w) {
		tag = t;
		width = w;
	}

	public int getWidth() {
		return width;
	}

	public void putWidth(int w) {
		width = w;
		System.out.println("   UPDATED WIDTH: " + this + " (WIDTH: " + width
				+ ")");
	}

	/**
	 * 初始化，将Java一些原始类型放到types里
	 */
	public static void initTypes() {
		types.put(INTEGER + "", new Type(INTEGER, 4));
		types.put(FLOATING + "", new Type(FLOATING, 8));
		types.put(CHARACTER + "", new Type(CHARACTER, 2));
		types.put(BOOL + "", new Type(BOOL, 1));
		types.put(VOIDTYPE + "", new Type(VOIDTYPE, 0));
		types.put(ERRORTYPE + "", new Type(ERRORTYPE, 0));
	}
	
	

	public static Type integer() {
		return (Type) types.get("" + INTEGER);
	}

	public boolean isInteger() {
		if (tag == INTEGER)
			return true;
		else
			return false;
	}

	public static Type floating() {
		return (Type) types.get("" + FLOATING);
	}

	public boolean isFloating() {
		if (tag == FLOATING)
			return true;
		else
			return false;
	}

	public static Type character() {
		return (Type) types.get("" + CHARACTER);
	}

	public boolean isCharacter() {
		if (tag == CHARACTER)
			return true;
		else
			return false;
	}

	public static Type bool() {
		return (Type) types.get("" + BOOL);
	}

	public boolean isBool() {
		if (tag == BOOL)
			return true;
		else
			return false;
	}

	public static Type voidtype() {
		return (Type) types.get("" + VOIDTYPE);
	}

	public boolean isVoidtype() {
		if (tag == VOIDTYPE)
			return true;
		else
			return false;
	}

	public static Type errortype() {
		return (Type) types.get("" + ERRORTYPE);
	}

	public boolean isErrortype() {
		if (tag == ERRORTYPE)
			return true;
		else
			return false;
	}

	public static Array array(int size, Type base) {
		Array a = (Array) types.get("" + ARRAY + size + base);
		if (a != null)
			return a;
		a = new Array(size, base);
		types.put(a.toString(), a);
		return a;
	}

	public boolean isArray() {
		if (tag == ARRAY)
			return true;
		else
			return false;
	}

	public static Name putName(String name, String supername, Context e) {
		Name n = (Name) types.get("" + NAME + name);
		n = new Name(name, supername, e);
		types.put(n.toString(), n);
		return n;
	}

	/**
	 * @param name
	 * @param lexeme
	 * @return
	 */
	public static Name forwardName(String name, String lexeme) {
		Name n = (Name) types.get("" + NAME + name);
		if (n != null)
			return n;
		n = new Name(name, lexeme);
		types.put(n.toString(), n);
		return n;
	}

	public boolean isName() {
		if (tag == NAME)
			return true;
		else
			return false;
	}

	public static Name getName(String name) {
		return (Name) types.get("" + NAME + name);
	}

	public static Product product(Type left, Type right) {
		Product p = (Product) types.get("" + PRODUCT + left + right);
		if (p != null)
			return p;
		p = new Product(left, right);
		types.put(p.toString(), p);
		return p;
	}

	public static Constructor constructor(Type domain, Type range) {
		Constructor c = (Constructor) types.get("" + CONSTRUCTOR + domain
				+ range);
		if (c != null)
			return c;
		c = new Constructor(domain, range);
		types.put(c.toString(), c);
		return c;
	}

	public boolean isConstructor() {
		if (tag == CONSTRUCTOR)
			return true;
		else
			return false;
	}

	public static Method method(Type domain, Type range) {
		Method m = (Method) types.get("" + METHOD + domain + range);
		if (m != null)
			return m;
		m = new Method(domain, range);
		types.put(m.toString(), m);
		return m;
	}

	public boolean isMethod() {
		if (tag == METHOD)
			return true;
		else
			return false;
	}

	/**
	 * @param referred 尝试构造一个引用类型的变量
	 * @return 构造出来的引用类型变量
	 */
	public static Reference reference(Type referred) {
		Reference r = (Reference) types.get("" + REFERENCE + referred);
		if (r != null)
			return r;
		r = new Reference(referred);
		types.put(r.toString(), r);
		return r;
	}

	public boolean isReference() {
		if (tag == REFERENCE)
			return true;
		else
			return false;
	}

	public String toString() {
		return tag + "";
	}

	protected String typeString() {
		String t = "";
		switch (tag) {
		case CHARACTER:
			t = "sbyte";
			break;
		case INTEGER:
			t = "int";
			break;
		case FLOATING:
			t = "double";
			break;
		case BOOL:
			t = "bool";
			break;
		case VOIDTYPE:
			t = "void";
			break;
		case ERRORTYPE:
			t = "error";
		}
		return t;
	}
	
	/** 简单地打印已获得的类型*/
	public static void printTypes() {
		TreeMap<String,Type> tm = new TreeMap<>(types);
		Set<Entry<String, Type>> tMap = tm.entrySet();
		Iterator<Entry<String, Type>> it = tMap.iterator();
		System.out.println("\nTypes: ");
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			Type value = (Type) entry.getValue();
			System.out.println("      " + key + " = " + value.typeString());
		}
	}
}