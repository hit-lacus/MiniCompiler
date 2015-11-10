#How to write your own compiler(Translation in Chinese)
##如何编写属于自己的编译器（中文译文）
![Politecnico di Torino](http://i.imgur.com/xnhTZWU.png)
![HIT](http://i.imgur.com/e7NSR80.jpg)

- - - - - - 
- - - - - -
>	原文:[How to write your own compiler](http://staff.polito.it/silvano.rivoira/HowToWriteYourOwnCompiler.htm)</br>
>	作者:[Silvano Rivoira, Politecnico di Torino](http://staff.polito.it/silvano.rivoira/)</br>
>	译者: **hit_lacus@126.com** Dept Sofware HIT </br>
>	翻译开始于:2015/11/5 23:56:41 
- - - -
- - - -

###摘要
作为[Formal Languages and Compilers课程](http://staff.polito.it/silvano.rivoira/FormalLanguagesCompilers/materials.htm)和[Linguaggi e Traduttori课程](http://staff.polito.it/silvano.rivoira/LingTrad/materiale.htm)的任课老师，作者将使用JFlex，CUP，LLVM编写一个基于Java的，能够编译mjava语言（MiniJava，一种Java语言的子集）的编译器。

###前提条件
* 您必须掌握编译原理的基本知识，包括词法分析、语法分析、语义分析
* 您必须足够了解Java以便您能阅读、编写和调试Java代码
* 您需要简单了解一些Maven的相关知识
* 您需要熟悉以下开源项目
	1. [JFlex,a lexical analyzer generator for Java](http://jflex.de/)
	2. [CUP,an LALR parser generator for Java](http://www2.cs.tum.edu/projects/cup/)
	3. [LLVM](http://llvm.org/)


###译者的测试环境
* Windows 7
* JDK 1.8.05 64bit
* Maven 3.3
* JFlex 1.6.1
* CUP 0.11b

###开始前的准备
1. 准备Java(Java6及以上皆可)
2. 准备Maven(译者使用版本3.3.3)
3. 准备Eclipse for Java(建议使用4.0版本以上，译者使用Eclipse Luna，即4.4版本)
4. 在Eclipse内导入本工程
	
>	Import - Git - Clone


###译者注
本文基于译者理解，对原文进行一定的增删，由于本人理解有限，译文并不一定符合原作者的表达。


![cup logo](http://www2.cs.tum.edu/projects/cup/cup_logo.gif)

- - - - -
- - - - -
**以下为译文主体**

- - - - -
- - - - -

#目录
1. 介绍（Introduction）
2. 自制编译器准备编译的语言（Source lauguage）
3. 文法分析（Lexical analysis）
4. 语法解析（Parsing）
5. 错误处理（Error handling）
6. 符号表（Symbol table）
7. 类型（Type）
8. 类型检查（Type checking）
9. 代码生成
10. 面向对象的代码生成


###介绍

这篇文档介绍了一个构建完整的、能够编译一门真正的语言的编译器，这里演示了如何一步步地设计和实现编译过程的不同阶段。本编译器使用的工具都是开源和免费的：

1. 编译器前端是由JFlex，一种文法分析器的生成器，和CUP，一种LALR语法分析器的生成器组成的。
2. 后端是LLVM 【待译】。

### 自制编译器准备编译的语言（作者称之为mjava）
**Test.mjava**  为使用mjava写的源代码

	/********************************************************************
	                        Sample mjava source program
	*********************************************************************/
	 
	public class String{
	} //end of class String
	 
	public class Int{
	  int n;
	  public Int(int i){
	    n = i;
	  }
	  public int f(){
	    return fact(n);
	  }
	  int fact(int n){
	    return n > 2 ? n * fact(n -1) : n;
	  }
	} //end of class Int
	 
	public class Test{
	  public void main(){
	    int n, f;
	    Int t;
	    n = 0;
	    while(n < 1 || n > 16) {
	       printf ("Enter an integer greater than 0 and less than 17: ");
	       scanf ("%d", &n);
	    }
	    t = new Int(n);
	    f = t.f(); 
	    printf("factorial(%d)= %d\n", n, f);
	  }
	} //end of class Test

相比Java，**mjava**将具有以下特征：

* 在唯一的文件声明所有的类
* 没有接口，只允许单继承
* 没有抽象类
* 没有静态字段和静态方法
* 不允许重写方法
* 只实现了基本的流程操作
* I/O操作只能通过类似C风格的printf和scanf语句

###文法分析
首先我们将设法获取文法元素（单词，token），通过文法分析器的生成器。JFlex将文法规约（文法规约定义了一些正则表达式，并可以在匹配正则表达式后执行一段Java代码）转化为一个实现了DFA的Java程序。

请参看mjava.flex，这篇文法规约使用sym.java中的整形常量来标识mjava中出现的终结符。

Flex可以处理mjava.flex来生成一个Scanner.java，一个词法分析器。Scanner通过java.io.Reader来读取一个源代码文本文件。

指令%debug使得JFlex生成一个main函数，用于读取文件路径作为参数，并且在控制台输出分词信息。对于每个单词，JFlex可以输出它的行数、列数、匹配的正则表达式，对应的action和sym.java内相应的整数。



**mjava.flex**

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Copyright (C) 2006  Silvano Rivoira <silvano.rivoira@polito.it>                    *
	 * All rights reserved.                                                    *
	 *                                                                         *
	 * This program is free software; you can redistribute it and/or modify    *
	 * it under the terms of the GNU General Public License. See the file      *
	 * COPYRIGHT for more information.                                         *
	 *                                                                         *
	 * This program is distributed in the hope that it will be useful,         *
	 * but WITHOUT ANY WARRANTY;              *
	 *                                                                         *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/* mjava  language scanner specification */
	
	import java_cup.runtime.*;
	
	%%
	
	%public
	%class Scanner
	%implements sym
	%unicode
	%line
	%column
	%cup
	%debug
	
	%{
	  StringBuffer string = new StringBuffer();
	 
	  private Symbol symbol(int type) {
	    return new Symbol(type, yyline+1, yycolumn+1);
	  }
	
	  private Symbol symbol(int type, Object value) {
	    return new Symbol(type, yyline+1, yycolumn+1, value);
	  }
	%}
	
	/* main character classes */
	LineTerminator = \r|\n|\r\n
	InputCharacter = [^\r\n]
	
	WhiteSpace = {LineTerminator} | [ \t\f]
	
	/* comments */
	Comment = {TraditionalComment} | {EndOfLineComment} | 
	          {DocumentationComment}
	
	TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
	EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?
	DocumentationComment = "/*" "*"+ [^/*] ~"*/"
	
	/* identifiers */
	Identifier = [:jletter:][:jletterdigit:]*
	
	/* integer literals */
	DecIntegerLiteral = 0 | [1-9][0-9]*
	
	/* floating point literals */        
	FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? 
	
	FLit1    = [0-9]+ \. [0-9]* 
	FLit2    = \. [0-9]+ 
	FLit3    = [0-9]+ 
	Exponent = [eE] [+-]? [0-9]+
	
	/* string and character literals */
	StringCharacter = [^\r\n\"\\]
	SingleCharacter = [^\r\n\'\\]
	
	%state STRING, CHARLITERAL
	
	%%
	
	<YYINITIAL> {
	
	  /* keywords */
	  "boolean"                      { return symbol(BOOLEAN); }
	  "char"                         { return symbol(CHAR); }
	  "class"                        { return symbol(CLASS); }
	  "else"                         { return symbol(ELSE); }
	  "extends"                      { return symbol(EXTENDS); }
	  "float"                        { return symbol(FLOAT); }
	  "int"                          { return symbol(INT); }
	  "new"                          { return symbol(NEW); }
	  "if"                           { return symbol(IF); }
	  "public"                       { return symbol(PUBLIC); }
	  "super"                        { return symbol(SUPER); }
	  "return"                       { return symbol(RETURN); }
	  "void"                         { return symbol(VOID); }
	  "while"                        { return symbol(WHILE); }
	  "this"                         { return symbol(THIS); }
	
	  /* boolean literals */
	  "true"                         { return symbol(BOOLEAN_LITERAL, new Boolean(true)); }
	  "false"                        { return symbol(BOOLEAN_LITERAL, new Boolean(false)); }
	  
	  /* null literal */
	  "null"                         { return symbol(NULL_LITERAL); }
	  
	  
	  /* separators */
	  "("                            { return symbol(LPAREN); }
	  ")"                            { return symbol(RPAREN); }
	  "{"                            { return symbol(LBRACE); }
	  "}"                            { return symbol(RBRACE); }
	  "["                            { return symbol(LBRACK); }
	  "]"                            { return symbol(RBRACK); }
	  ";"                            { return symbol(SEMICOLON); }
	  ","                            { return symbol(COMMA); }
	  "."                            { return symbol(DOT); }
	  
	  /* operators */
	  "="                            { return symbol(EQ); }
	  ">"                            { return symbol(GT); }
	  "<"                            { return symbol(LT); }
	  "!"                            { return symbol(NOT); }
	  "?"                            { return symbol(QUESTION); }
	  ":"                            { return symbol(COLON); }
	  "=="                           { return symbol(EQEQ); }
	  "<="                           { return symbol(LTEQ); }
	  ">="                           { return symbol(GTEQ); }
	  "!="                           { return symbol(NOTEQ); }
	  "&&"                           { return symbol(ANDAND); }
	  "&"          			   { return symbol(AT); }
	  "||"                           { return symbol(OROR); } 
	  "+"                            { return symbol(PLUS); }
	  "-"                            { return symbol(MINUS); }
	  "*"                            { return symbol(MULT); }
	  "/"                            { return symbol(DIV); }
	  "%"                            { return symbol(MOD); }
	  
	  /* string literal */
	  \"                             { yybegin(STRING); string.setLength(0); }
	
	  /* character literal */
	  \'                             { yybegin(CHARLITERAL); }
	
	  /* numeric literals */
	
	  {DecIntegerLiteral}            { return symbol(INTEGER_LITERAL, new Integer(yytext())); }
	
	 
	  {FloatLiteral}                 { return symbol(FLOATING_POINT_LITERAL, new Float(yytext().substring(0,yylength()))); }
	  
	  /* comments */
	  {Comment}                      { /* ignore */ }
	
	  /* whitespace */
	  {WhiteSpace}                   { /* ignore */ }
	
	  /* identifiers */ 
	  {Identifier}                   { return symbol(IDENTIFIER, yytext()); }  
	}
	
	<STRING> {
	  \"                             { yybegin(YYINITIAL); return symbol(STRING_LITERAL, string.toString()); }
	  
	  {StringCharacter}+             { string.append( yytext() ); }
	  
	  /* escape sequences */
	  "\\b"                          { string.append( '\b' ); }
	  "\\t"                          { string.append( '\t' ); }
	  "\\n"                          { string.append( '\n' ); }
	  "\\f"                          { string.append( '\f' ); }
	  "\\r"                          { string.append( '\r' ); }
	  "\\\""                         { string.append( '\"' ); }
	  "\\'"                          { string.append( '\'' ); }
	  "\\\\"                         { string.append( '\\' ); }
	
	  
	  /* error cases */
	  \\.                            { throw new RuntimeException("Illegal escape sequence \""+yytext()+"\""); }
	  {LineTerminator}               { throw new RuntimeException("Unterminated string at end of line"); }
	}
	
	<CHARLITERAL> {
	  {SingleCharacter}\'            { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character(yytext().charAt(0))); }
	  
	  /* escape sequences */
	  "\\b"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\b'));}
	  "\\t"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\t'));}
	  "\\n"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\n'));}
	  "\\f"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\f'));}
	  "\\r"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\r'));}
	  "\\\""\'                       { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\"'));}
	  "\\'"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\''));}
	  "\\\\"\'                       { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\\')); }
	
	  
	  /* error cases */
	  {LineTerminator}               { throw new RuntimeException("Unterminated character literal at end of line"); }
	}
	
	/* error fallback */
	
	.|\n                             { return symbol(ILLEGAL_CHARACTER, yytext());}
	<<EOF>>                          { return symbol(EOF); }

**sym.java** 

	public interface sym {
	  /* terminals */
	  public static final int STRING_LITERAL = 49;
	  public static final int GTEQ = 38;
	  public static final int MULT = 10;
	  public static final int CHAR = 4;
	  public static final int LTEQ = 37;
	  public static final int LPAREN = 15;
	  public static final int INT = 3;
	  public static final int MINUS = 31;
	  public static final int RPAREN = 16;
	  public static final int BOOLEAN_LITERAL = 47;
	  public static final int OROR = 42;
	  public static final int CHARACTER_LITERAL = 48;
	  public static final int NOT = 32;
	  public static final int SEMICOLON = 9;
	  public static final int LT = 35;
	  public static final int ILLEGAL_CHARACTER = 44;
	  public static final int COMMA = 11;
	  public static final int CLASS = 19;
	  public static final int ANDAND = 41;
	  public static final int DIV = 33;
	  public static final int PLUS = 30;
	  public static final int NULL_LITERAL = 51;
	  public static final int IF = 25;
	  public static final int THIS = 22;
	  public static final int DOT = 8;
	  public static final int EOF = 0;
	  public static final int BOOLEAN = 2;
	  public static final int RETURN = 28;
	  public static final int SUPER = 23;
	  public static final int NEW = 29;
	  public static final int error = 1;
	  public static final int EQEQ = 39;
	  public static final int MOD = 34;
	  public static final int VOID = 21;
	  public static final int EQ = 14;
	  public static final int LBRACK = 6;
	  public static final int COLON = 17;
	  public static final int FLOATING_POINT_LITERAL = 46;
	  public static final int LBRACE = 12;
	  public static final int ELSE = 26;
	  public static final int RBRACK = 7;
	  public static final int WHILE = 27;
	  public static final int FLOAT = 5;
	  public static final int PUBLIC = 18;
	  public static final int RBRACE = 13;
	  public static final int EXTENDS = 20;
	  public static final int QUESTION = 43;
	  public static final int AT = 24;
	  public static final int GT = 36;
	  public static final int NOTEQ = 40;
	  public static final int IDENTIFIER = 50;
	  public static final int INTEGER_LITERAL = 45;
	}


### 语法分析

现在我们来对mjava代码进行语法结构分析。通过CUP，我们能见分析器规约转换成一个实现LALR语法分析器的Java程序。语法分析器规约放在mjava.cup文件，mjava.cup生成的语法分析器依赖于mjava.flex生成的词法分析器，用CUP处理mjava.cup将获得两个文件：sym.java和mjavac.java，前者定义了终结符，而后者是语法分析器。




**mjava.cup**

	import java_cup.runtime.*;
	import java.io.*;
	
	/* mjava  parser for CUP.  
	 * Copyright (C) 2006 Silvano Rivoira <silvano.rivoira@polito.it>
	 * This program is released under the terms of the GPL; see the file
	 * COPYING for more details.  There is NO WARRANTY on this code.
	 */
	
	parser code  {:
	
	   public static void main(String argv[]) {
	
	    for (int i = 0; i < argv.length; i++) {
	      try {
	        System.out.println("Parsing ["+argv[i]+"]");
	        Scanner s = new Scanner(new FileReader(argv[i]));
	        mjavac p = new mjavac(s);
	        p.parse();
	        
	        System.out.println("No errors.");
	      }
	      catch (Exception e) {
	        e.printStackTrace(System.out);
	        System.exit(1);
	      }
	    }
	  }
	
	  public void report_error(String message, Object info) {
	    StringBuffer m = new StringBuffer("Error ");
	
	    if (info instanceof java_cup.runtime.Symbol) 
	      m.append( "("+info.toString()+")" );
	     
	    m.append(" : "+message);
	   
	    System.err.println(m);
	  }
	   
	  public void report_fatal_error(String message, Object info) {
	    report_error(message, info);
	    throw new RuntimeException("Fatal Syntax Error");
	  }
	:};
	
	terminal BOOLEAN; // primitive_type
	terminal INT, CHAR, FLOAT; // numeric_type
	terminal LBRACK, RBRACK; // array_type
	terminal DOT; // qualified_name
	terminal SEMICOLON, MULT, COMMA, LBRACE, RBRACE, EQ, LPAREN, RPAREN, COLON;
	terminal CLASS; // class_declaration
	terminal EXTENDS; // super
	terminal PUBLIC; // public
	terminal VOID; // method_header
	terminal THIS, SUPER; // explicit_constructor_invocation
	terminal AT; // reference operator
	terminal IF, ELSE; // if_then_statement, if_then_else_statement
	terminal WHILE; // while_statement, do_statement
	terminal RETURN; // return_statement
	terminal NEW; // class_instance_creation_expression
	terminal PLUS, MINUS, NOT, DIV, MOD;
	terminal LT, GT, LTEQ, GTEQ; // relational_expression
	terminal EQEQ, NOTEQ; // equality_expression
	terminal ANDAND; // conditional_and_expression
	terminal OROR; // conditional_or_expression
	terminal QUESTION; // conditional_expression
	terminal ILLEGAL_CHARACTER; // illegal_character
	
	terminal java.lang.Number INTEGER_LITERAL;
	terminal java.lang.Number FLOATING_POINT_LITERAL;
	terminal java.lang.Boolean BOOLEAN_LITERAL;
	terminal java.lang.Character CHARACTER_LITERAL;
	terminal java.lang.String STRING_LITERAL;
	terminal java.lang.String IDENTIFIER; // name
	terminal NULL_LITERAL;
	
	// 1) The Syntactic Grammar
	non terminal goal;
	
	// 2) Lexical Structure
	non terminal literal;
	
	// 3) Types, Values, and Variables
	non terminal type, primitive_type, numeric_type;
	non terminal reference_type;
	non terminal array_type;
	
	// 4) Names
	non terminal name;
	
	// 5) Classes
	non terminal class_declarations;
	
	// 5.1) Class Declaration
	non terminal class_declaration, sup, super_opt;
	non terminal class_body, modifiers_opt;
	non terminal class_body_declarations, class_body_declarations_opt;
	non terminal class_body_declaration, class_member_declaration;
	
	// 5.2) Field Declarations
	non terminal field_declaration, variable_declarators;
	non terminal variable_declarator_id;
	
	// 5.3) Method Declarations
	non terminal method_declaration, method_header;
	non terminal formal_parameter_list_opt, formal_parameter_list;
	non terminal formal_parameter;
	non terminal method_body;
	
	// 5.4) Constructor Declarations
	non terminal constructor_declaration, constructor_declarator;
	non terminal constructor_body;
	non terminal explicit_constructor_invocation;
	
	// 6) Blocks and Statements
	non terminal block;
	non terminal block_statements_opt, block_statements, block_statement;
	non terminal local_variable_declaration_statement, local_variable_declaration;
	non terminal statement, statement_no_short_if;
	non terminal statement_without_trailing_substatement;
	non terminal empty_statement;
	non terminal expression_statement, statement_expression;
	non terminal if_then_statement;
	non terminal if_then_else_statement, if_then_else_statement_no_short_if;
	non terminal while_statement, while_statement_no_short_if;
	non terminal return_statement;
	
	// 7) Expressions
	non terminal primary, primary_no_new_array;
	non terminal class_instance_creation_expression;
	non terminal argument_list_opt, argument_list;
	non terminal array_creation_expression;
	non terminal dim_exprs, dim_expr, dims_opt, dims;
	non terminal field_access, method_invocation, array_access;
	non terminal postfix_expression;
	non terminal unary_expression;
	non terminal multiplicative_expression, additive_expression;
	non terminal relational_expression, equality_expression;
	non terminal conditional_and_expression, conditional_or_expression;
	non terminal conditional_expression, assignment_expression;
	non terminal assignment;
	non terminal left_hand_side;
	non terminal assignment_operator;
	non terminal expression_opt, expression;
	
	
	start with goal;
	
	// 1) The Syntactic Grammar
	goal ::=
			class_declarations
		;
	
	// 2) Lexical Structure.
	literal ::=
			INTEGER_LITERAL
		|	FLOATING_POINT_LITERAL
		|	BOOLEAN_LITERAL
		|	CHARACTER_LITERAL
		|	STRING_LITERAL
		|	NULL_LITERAL
		;
	
	// 3) Types, Values, and Variables
	type	::=
			primitive_type
		|	reference_type
		;
	primitive_type ::=
			numeric_type
		|	BOOLEAN
		;
	numeric_type ::= 
			INT 
		|	CHAR
		|	FLOAT 
		;
	reference_type ::=
			name
		|	array_type
		;
	array_type ::=
			primitive_type dims
		|	name dims
		;
	
	// 4) Names
	name	::=
			IDENTIFIER
		|	name DOT IDENTIFIER
		;
	
	modifiers_opt::=
		|	PUBLIC
		;
	
	// 5) Classes
	class_declarations ::= 
		|	class_declarations class_declaration
		;
	
	// 5.1) Class Declaration
	class_declaration ::= 
			modifiers_opt CLASS IDENTIFIER super_opt class_body
		;
	super_opt ::=	
		|	sup
		;
	sup ::=
			EXTENDS name
		;
	class_body ::=
			LBRACE class_body_declarations_opt RBRACE 
		;
	class_body_declarations_opt ::= 
		|	class_body_declarations ;
	class_body_declarations ::= 
			class_body_declaration
		|	class_body_declarations class_body_declaration
		;
	class_body_declaration ::=
			class_member_declaration
		|	constructor_declaration
		|	block
		;
	class_member_declara

###符号表

一旦我们完成了语法分析，我们就能开始语义分析阶段。

为了获得源代码的信息，首先我们将进行符号表的设计。在分析阶段收集的信息将会在代码生成阶段产生作用，符号表的每条记录将包含特定标识符的词义、类型、位置和其他所需要的信息。
因为mjava承认嵌套式的上下文（一段代码可以拥有多个上下文，同一个变量声明可以出现在不同的上下文），我们通过为每个上下文设置独立的符号表来支持相同标识符的多次声明。一个简易的实现方式是子符号表作为父符号表的元素出现。

让我们来对这样一个嵌套的上下文进行建模，首先类名命名为Context.java，这是一个final类。

**Context**将包含数个成员变量，包括:

1. public static Context root; 这是一个静态变量，意义为根上下文，语法分析一开始就建立的根节点。
2. public static Context current;这是一个静态变量，意义为当前上下文的指针，一开始复制为root，表示开始时的当前上下文为root。
3. private HashMap<String ,JavaSymbol> table;这是一个映射，意义为符号表，用来保存此上下文的符号(标识符名和符号)。
4. private Context parent;这是一个父上下文的指针。

**Context**将包含数个成员方法，包括:

1. put(String key,JavaSymbol symbol)
	向当前Context添加一条新的记录
2. get(String key)
	从当前Context获得一条记录
3. advance()
	在当前的Context创建一个子Context，并将当前Context置为子Context
4. back()
	离开当前Context，将当前Context置为父Context
5. createClass(String className,JavaSymbol symbol)
	创建一个新的Java类


###类型
The structure of a type in mjava is represented by a type expression defined as:
· a basic type ( integer, floating, character, boolean, void, error )
· the expression formed by applying a type constructor (name, array, product, method, constructor, reference ) to a type expression.
Types are implemented by the class hierarchy in package type : the superclass Type in Typejava, declares a field (tag) corresponding to either a basic type or a type constructor, and a field (width) containing the number of bytes required to store variables of that type.
Each type constructor is implemented by a corresponding subclass of Type defined, respectively, in Name.java, Array.java, Product.java, Method.java, Constructor.java, and Reference. java.
Type expressions are strings formed by:
· just a tag (for basic types)
· the tag of a type constructor followed by the type expression to which it applies (for constructed types).
In order to efficiently check type equivalence, we maintain a dictionary of the types declared in a source program by means of the HashMap table in class Type : entries in the dictionary are key-value pairs where the key is a type expression and the value is the corresponding Type object.
When a type is encountered in the source program, its type expression is searched for in the dictionary: if present, the corresponding Type object is retrieved and used, otherwise a new Type object is created and a new entry is added to the dictionary.
In this way two types are equivalent if and only if they are represented by the same Type object.
 
To complete the construction of symbol tables, we have to specify the information we want to collect about identifiers in the class  Symb.
In Symb.java we have declared the following fields:
· a reference (type) to a Type object
· a reference (ownerClass) to the Name object of the class where the identifier is declared
· a boolean value (pub), indicating whether the identifier is public or private.
 
In order to admit forward references, it is necessary to postpone type checking until all the identifiers have been inserted into the symbol table, and therefore to organize the front end in two passes: the first one to create the symbol table, the second one to perform type checking and intermediate code generation.
The front end for mjava specified in mjava.cup implements two passes by reading and processing the source file two times .
The  static variables parser.first and parser.second indicate the current pass.
In the second pass the parser moves through the chains of symbol tables, following the same path followed in the first pass.
This behavior is obtained by saving, in the first pass, the current (top) environment into the ArrayList newEnvs (in class Env ) anytime it is going to be changed by push and pop operations.
In the second pass the sequence of environments saved in newEnvs is restored by invoking  method next of class Env in Env.java,  in place of push and pop.

在类型匹配方面，这里使用Type.java对类型进行建模，首先我们将mjava中的类型分为以下两种（和Java几乎一致）：
1. 基本类型，如整型、浮点型、字符型、布尔型等
2. 引用类型，即基本类型以外的类型，可以使用Type构造函数来构造，构造函数为Type(String name,)

Type.java的成员变量包括：
1. private int tag;标识这个类型是基本类型还是引用类型
2. private int width;标识这个类型需要多大的内存空间

和Type.java在一个包里的的类有
