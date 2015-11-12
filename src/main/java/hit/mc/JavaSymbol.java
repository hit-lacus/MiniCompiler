package hit.mc;

import hit.mc.type.*;

/**
 * Java的符号
 * @author lacus
 */
public class JavaSymbol {
	
	/** 符号的类型*/
	private Type type;
	/** */
	private Name ownerClass;
	/** 是否是公共*/
	private boolean pub = false;

	public JavaSymbol(Type t, Name c, boolean p){
		type = t;
		ownerClass = c;
		pub = p;
	}

	public Type getType(){
		return type;
	}

	public Name getOwnerClass(){
		return ownerClass;
	}


	public boolean isPublic(){
		return pub;
	}

	public String toString(){
		return "("+type+", "+ownerClass+", "+pub+")";
	}

}
