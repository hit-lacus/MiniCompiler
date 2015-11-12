package hit.mc.type;

import java.util.HashMap;

import hit.mc.Context;
/**
 * @author lacus
 */
public class Name extends Type {

	static Name current;
	/** 这是什么*/
	static HashMap<String,String> forwards = new HashMap<>();

	private String name;
	private String supername;
	private Context env;		

	protected Name(String n, String s, Context e) {
		super(NAME, 0);
		name = n;
		supername = s;
		env = e;
		current = this;
		System.out.println("   NEW CLASS: "+this+" (NAME: "+name+", SUPER: "+supername+", ENV: e"+env.getCounter()+")");
	}

	protected Name(String n, String lexeme) {
		super(NAME, 0);
		name = n;
		supername = null;
		env = null;
		// 向forwards 添加一条记录
		forwards.put(n, lexeme);
		System.out.println("   FORWARD REFERENCE: "+this+" (NAME: "+name+", SUPER: "+supername+", ENV: e"+env+")");
	}

	public static Name getCurrentClass(){
		return current;
	}

	public static void putCurrentClass(Name c){
		current = c;
		String n = c.getName();
		String s = c.getSuper();
		Context e = c.getEnv();
		System.out.println("   CURRENT CLASS: "+c+" (NAME: "+n+", SUPER: "+s+", ENV: e"+e.getCounter()+")");
	}

	public static boolean isForward(String n){
		return (forwards.remove(n) != null);
	}

	public static HashMap<String,String> ForwardHashtable(){
		return forwards;
	}

	public Context getEnv(){
		return env;
	}

	public String getName(){
		return name;
	}

	public String getSuper(){
		return supername;
	}

	public String toString(){
		return ""+tag+name;
	}

	protected String typeString(){
		return name;
	}
}