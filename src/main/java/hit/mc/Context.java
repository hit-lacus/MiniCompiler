package hit.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hit.mc.type.*;
import hit.mc.*;

/**
 * 
 * @author lacus
 */
public class Context {
	static Context top;
	static int num = 0;
	static Context root = new Context(null);
	static int envCounter;
	static ArrayList newEnvs = new ArrayList();

	HashMap table;
	int counter;
	Context prev;
	List vars;

	public Context(Context p) {
		counter = num;
		num++;
		table = new HashMap();
		prev = p;
		vars = new ArrayList();
	}

	public int getCounter() {
		return counter;
	}

	public static void initFirst() {
		envCounter = 0;
		newEnvs.add(envCounter, root);
		top = (Context) newEnvs.get(envCounter);
		System.out.println(" " + top);
	}

	public static void initSecond() {
		envCounter = 0;
		top = (Context) newEnvs.get(envCounter);
		System.out.println(" " + top);
	}

	public static void next() {
		envCounter++;
		top = (Context) newEnvs.get(envCounter);
		System.out.println(" " + top);
	}

	public static int putClass(String c, boolean p) {
		Name cName = Type.getName(c);
		if ((cName != null) && !Name.isForward(c)) {
			push();
			return 1;
		} else {
			Context current = top;
			push();
			cName = Type.putName(c, null, top);
			JavaSymbol s = new JavaSymbol(cName, cName, p);
			current.table.put(c, s);
			System.out.println("   PUT " + c + " IN " + current);
			return 0;
		}
	}
	/**
	 * 
	 * @param c 需要被放进去的类
	 * @param p 是否是公共类
	 * @param sc c 的超类，如果有的话
	 * @return
	 */
	public static int putClass(String c, boolean p, String sc) {
		// 尝试获取c的Name
		Name cName = Type.getName(c);
		
		if ((cName != null) && !Name.isForward(c)) {
			push();
			push();
			return 1;
		} else {
			Name scName = Type.getName(sc);
			if (scName == null) {
				push();
				push();
				return 2;
			} else {
				push(scName.getEnv());
				Context current = top;
				push();
				cName = Type.putName(c, sc, top);
				JavaSymbol s = new JavaSymbol(cName, cName, p);
				current.table.put(c, s);
				System.out.println("   PUT " + c + " IN " + current);
				root.table.put(c, s);
				System.out.println("   PUT " + c + " IN " + root);
				return 0;
			}
		}
	}

	public static boolean put(String name, JavaSymbol s) {
		if (!top.table.containsKey(name)) {
			top.table.put(name, s);
			System.out.println("   PUT " + name + " IN " + top);
			return true;
		}
		return false;
	}

	public static void putSymb(String name, JavaSymbol s) {
		Context e = top.prev;
		e.table.put(name, s);
		System.out.println("   CHANGED " + name + " IN " + e);
	}

	public static boolean putVar(String name, JavaSymbol s) {
		if (!top.table.containsKey(name)) {
			top.table.put(name, s);
			top.vars.add(name);
			System.out.println("   PUT VARIABLE " + name + " IN " + top);
			return true;
		}
		return false;
	}

	public static JavaSymbol get(String name) {
		return get(name, top);
	}

	public static JavaSymbol get(String name, Context env) {
		for (Context e = env; e != null; e = e.prev) {
			JavaSymbol found = (JavaSymbol) (e.table.get(name));
			if (found != null)
				return found;
		}
		return null;
	}

	static void push(Context e) {
		envCounter++;
		top = new Context(e);
		newEnvs.add(envCounter, top);
		System.out.println(" " + top);
	}

	public static void push() {
		push(top);
	}

	public static void pop() {
		top = top.prev;
		envCounter++;
		newEnvs.add(envCounter, top);
		System.out.println(" " + top);
	}

	public String toString() {
		if (prev != null)
			return " e" + counter + ": " + "(e" + prev.getCounter() + ") "
					+ table + " - VARIABLES: " + vars;
		else
			return " e" + counter + ": " + table + " - VARIABLES: " + vars;
	}

}
