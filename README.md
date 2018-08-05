# How to write your own compiler [如何编写属于自己的编译器(译文)]

![Politecnico di Torino](http://i.imgur.com/xnhTZWU.png)
![HIT](http://i.imgur.com/e7NSR80.jpg)

- - - - - - 
- - - - - -
>	原文:[How to write your own compiler](http://staff.polito.it/silvano.rivoira/HowToWriteYourOwnCompiler.htm)</br>
>	作者:[Silvano Rivoira 教授 控制和计算机工程学院 都灵理工大学 意大利](http://staff.polito.it/silvano.rivoira/)</br>
>	译者:**hit\_lacus@126.com** 13级本科生 软件学院 哈尔滨工业大学威海 </br>
>	翻译开始于:2015/11/5 23:56:41 
- - - -
- - - -

### 摘要
作为[Formal Languages and Compilers课程](http://staff.polito.it/silvano.rivoira/FormalLanguagesCompilers/materials.htm)和[Linguaggi e Traduttori课程](http://staff.polito.it/silvano.rivoira/LingTrad/materiale.htm)的任课老师，作者将使用JFlex，CUP，LLVM编写一个基于Java的，能够编译mjava语言（MiniJava，一种Java语言的子集）的编译器。

### 预期的读者
刚开始学习编译原理并希望锻炼自己实践能力的计算机相关专业本科三年级学生（BTW，译者就是这种情况 ^_^ ），和其他编译原理的入门者；本文不适合已有与编译相关的丰富经历的读者阅读。

### 前提条件
* 您必须掌握编译原理的基本知识，包括词法分析、语法分析、语义分析
* 您必须足够了解Java以便您能阅读、编写和调试Java代码
* 您需要简单了解一些Maven的相关知识
* 您需要熟悉以下开源项目
	1. [JFlex,a lexical analyzer generator for Java](http://jflex.de/)
	2. [CUP,an LALR parser generator for Java](http://www2.cs.tum.edu/projects/cup/)
	3. [LLVM](http://llvm.org/)

### 译者的测试环境
* Windows 7
* JDK 1.8.05 64bit
* Maven 3.3
* JFlex 1.6.1
* CUP 0.11b

### 开始前的准备
1. 准备Java(Java6及以上皆可)
2. 准备Maven(译者使用版本3.3.3)
3. 准备Eclipse for Java(建议使用4.0版本以上，译者使用Eclipse Luna，即4.4版本)
4. [获得Github代码](https://github.com/hit-lacus/MiniCompiler)


### 简单了解编译器	

### 简单了解JFLex


### 简单了解CUP


### 译者注
本文基于译者理解，对原文进行一定的增删，由于本人理解有限，译文并不一定符合原作者的表达。


![cup logo](http://www2.cs.tum.edu/projects/cup/cup_logo.gif)

- - - - -
- - - - -
**以下为译文主体**

- - - - -
- - - - -

# 目录
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


### 介绍

这篇文档介绍了一个构建完整的、能够编译一门真正的语言的编译器，这里演示了如何一步步地设计和实现编译过程的不同阶段。本编译器使用的工具都是开源和免费的：

1. 编译器前端是由JFlex，一种文法分析器的生成器，和CUP，一种LALR语法分析器的生成器组成的。
2. 后端是LLVM 【待译】。

### 自制编译器准备编译的语言（作者称之为mjava）

相比Java，**mjava**将具有以下特征：

* 在唯一的文件声明所有的类
* 没有接口，只允许单继承
* 没有抽象类
* 没有静态字段和静态方法
* 不允许重写方法
* 只实现了基本的流程操作
* I/O操作只能通过类似C风格的printf和scanf语句

### 文法分析
首先我们将设法获取文法元素（单词，token），通过文法分析器的生成器。JFlex将文法规约（文法规约定义了一些正则表达式，并可以在匹配正则表达式后执行一段Java代码）转化为一个实现了DFA的Java程序。

请参看mjava.flex，这篇文法规约使用sym.java中的整形常量来标识mjava中出现的终结符。

Flex可以处理mjava.flex来生成一个Scanner.java，一个词法分析器。Scanner通过java.io.Reader来读取一个源代码文本文件。

指令%debug使得JFlex生成一个main函数，用于读取文件路径作为参数，并且在控制台输出分词信息。对于每个单词，JFlex可以输出它的行数、列数、匹配的正则表达式，对应的action和sym.java内相应的整数。


### 语法分析

现在我们来对mjava代码进行语法结构分析。通过CUP，我们能见分析器规约转换成一个实现LALR语法分析器的Java程序。语法分析器规约放在mjava.cup文件，mjava.cup生成的语法分析器依赖于mjava.flex生成的词法分析器，用CUP处理mjava.cup将获得两个文件：sym.java和mjavac.java，前者定义了终结符，而后者是语法分析器。


### 符号表

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


### 类型

在类型匹配方面，这里使用Type.java对类型进行建模，首先我们将mjava中的类型分为以下两种（和Java几乎一致）：
1. 基本类型，如整型、浮点型、字符型、布尔型等
2. 引用类型，即基本类型以外的类型，可以使用Type构造函数来构造，构造函数为Type(String name,)

Type.java的成员变量包括：
1. private int tag;标识这个类型是基本类型还是引用类型
2. private int width;标识这个类型需要多大的内存空间
3. 若干个静态整型，标识类型的几类基本类型
	* public static final int CHARACTER = 1;
	* public static final int INTEGER = 2;
	* public static final int FLOATING = 3;
	* public static final int BOOL = 4;
	* public static final int VOIDTYPE = 5;
	* public static final int ERRORTYPE = 6;
 	* public static final int NAME = 7;
	* public static final int ARRAY = 8;
	* public static final int REFERENCE = 90;
	* public static final int PRODUCT = 91;
	* public static final int CONSTRUCTOR = 92;
	* public static final int METHOD = 93;
4. public static HashMap<String，Type> typeTable;

Type.java的成员变量包括：
1. public static void initTypes();向typeTable添加所有原始类型
2. 若干个返回类型为Type的静态函数



注意到Java里的类型
和Type.java在一个包里的的类有 Name.java, Array.java, Product.java, Method.java, Constructor.java, Reference. java。

类型表达式是一个字符串，并且具有以下特征：

1. 当类型是基本类型时，表达式是一个整型（int tag）；
2. 当类型时应用类型时，表达式是

当在源程序找到一个类型，会在Type的typeTable里找相应的类型是否存在，如果不存在就新增这样一条记录。

JavaSymbol.java的成员变量包括：

1. Type type
2. Name name
3. boolean isPub

为了，有必要推迟到所有的标识符都插入到符号表再进行类型检查，因此将整个语法分析分为两个阶段：
1. 第一阶段，完成符号表
2. 第二阶段，完成类型检查和中间代码的生成

语法分析器parser里的静态boolean变量就是为了标识当前阶段而设置的。




# 附录

## mjava语言结构分析

#### 概述
mjava类似于Java，但不支持包的概念，所有类必须在一个文件内声明。

#### 源代码实例

		public class String{
		}
		
		public class Int{
		
		    int n;
		
		    public Int(int i){
				n = i;
		    }
		
		    public int f() {
				return fact(n);
		    }
		
		    int fact(int n) {
				return n > 2 ? n * fact(n -1) : n;
		    }
		
		}
		
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
		}

#### 终结符
以Java语言作为类比，终结符应该包括一下几类：

1. 英语标点符号，如分号、圆括号、英语句号，英语逗号、@符号
2. 数学符号，如+、-、*、/、=
3. 逻辑运算符号，如&&，||，&、|
4. Java保留字（关键词），如if、else、class、extends、void、null、public
5. 字面值，如数字字面值、字符串字面值、字符字面值

		terminal BOOLEAN; // primitive_type
		terminal INT, CHAR, FLOAT; // numeric_type
		terminal LBRACK, RBRACK; // array_type
		terminal DOT; // qualified_name
		terminal SEMICOLON, MULT, COMMA, LBRACE, RBRACE, EQ, LPAREN, RPAREN, COLON;
		terminal PUBLIC; // modifier
		terminal CLASS; // class_declaration
		terminal EXTENDS; // super
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


#### 非终结符与产生式

开始之前，我们首先约定文法的开始符号为**goal**。

考虑一个Java文件，首先应该包含数个Java类的声明，所以**goal**可以推出 **class_declarations**。

		goal ::= class_declarations;
其中**class\_declarations**表示类声明系列。

- - - - - 
###### 主体
- - - - -

同时由于一个Java文件可以包含一到多个Java类的声明，所以**class\_declarations**可以推出 **class\_declaration**或者**class\_declarations** **class\_declaration**。

		class_declarations ::= class_declaration | class_declarations class_declaration;
其中**class\_declaration**表示单个类的声明。

对于单个类的声明，可以推出“头部加主体”的结构，进一步头部可以拆分为类修饰符选项、class关键词、类名、继承选项。所以进一步地，

		class_declaration ::= 
			modifiers_opt CLASS IDENTIFIER class_body
		    |	modifiers_opt CLASS IDENTIFIER EXTENDS IDENTIFIER class_body
		    ;
其中**modifiers\_opt**表示修饰符选项、**CLASS**表示class关键词、**IDENTIFIER**为标识符、**class\_body**为类主体、**EXTENDS**为extends关键词。

为了方便起见，修饰符选项**modifiers\_opt**只能以public或者不修饰。

		modifiers_opt::=
		    |   PUBLIC
		    ;

类主体**class\_body**可以表示为左花括号**LBRACE**、类成员声明系列选项 **class\_body\_declarations\_opt**、右花括号**RBRACE**。

		class_body ::=
		        LBRACE class_body_declarations_opt:d RBRACE
		    ;

类成员声明系列选项**class\_body\_declarations\_opt**可以推出空；或者推出类成员声明系列**class\_body\_declarations**

		class_body_declarations_opt ::=         
		    |   class_body_declarations
		    ;

对于类成员声明系列**class\_body\_declarations**，与**class\_declarations**的产生式类似，可以推出一个到多个类成员声明**class\_body\_declaration**

		class_body_declarations ::= 
		        class_body_declaration
		    |   class_body_declarations class_body_declaration
		    ;
我们知道，类成员声明包括成员变量、成员函数（包括构造函数）、代码块（常见的为静态代码块）；由于构造函数在语法上具有特殊性，比如它的方法名是固定的、可以通过super关键词和thsi关键词分别调用超类和本类的构造函数，现将除去构造函数成员函数和成员变量统称为**class\_member\_declaration**，构造函数称为**constructor\_declaration**，语句块称为**block**。

		class_body_declaration ::=
		        class_member_declaration
		    |   constructor_declaration
		    |   block
		    ;

		class_member_declaration ::=
		        field_declaration
		    |   method_declaration
		    |   SEMICOLON
		    ;
对于成员变量，可以推出修饰符选项**modifiers\_opt**、类型**type**、变量声明系列**variable\_declarators**、分号**SEMICOLON**。

		field_declaration ::= 
		        modifiers_opt type variable_declarators SEMICOLON
		    |   modifiers_opt type error SEMICOLON 
		    |   modifiers_opt type error method_declaration 
		    ;

注意到成员变量一行可以声明相同类型的多个变量，变量声明系列**variable\_declarators**可以推出单个变量声明**variable\_declarator\_id**；或者推出变量声明系列**variable\_declarators**、逗号**COMMA**、**M1**、变量声明**variable_declarator\_id**。

		variable_declarators ::=
		        variable_declarator_id
		    |   variable_declarators COMMA M1 variable_declarator_id
		    |   error COMMA variable_declarator_id
		    ;

对于单个变量声明可以分成一般变量和数组，分别推出标识符**IDENTIFIER**；或者推出变量声明**variable\_declarator\_id**、左方括号**LBRACK**、右方括号**RBRACK**。

		variable_declarator_id ::=
		        IDENTIFIER
		    |   variable_declarator_id LBRACK RBRACK
		    ;

方法声明可以产生方法头部 + 方法主体

		method_declaration ::=
		        method_header method_body
		    ;
    
方法头部可以产生修饰符选项 + 类型（返回值类型） + 标识符（方法名） + 左圆括号 + 形式参数列表选项 + 右括号

		method_header ::=
		        modifiers_opt type IDENTIFIER LPAREN formal_parameter_list_opt RPAREN
		    |   modifiers_opt VOID IDENTIFIER LPAREN formal_parameter_list_opt RPAREN
		    |   error LPAREN formal_parameter_list_opt RPAREN
		    ;
    
形式参数列表选项可以产生空、形式参数列表

		formal_parameter_list_opt ::=
		    |   formal_parameter_list:t
		    ;

形式参数列表可以产生形式参数 、形式参数列表 + 形式参数

		formal_parameter_list ::=
		        formal_parameter:t
		    |   formal_parameter_list:t1 COMMA formal_parameter:t2
		    |   error formal_parameter
		    ;

形式参数可以产生类型 + 变量声明单元

		formal_parameter ::=
		        type variable_declarator_id
    	;

方法主体可以产生左花括号 + 语句语句块选项 + 右花括号

		method_body ::=
		        LBRACE block_statements_opt RBRACE
		    |   SEMICOLON
		    ;

构造函数声明可以产生构造函数修饰符 + 构造函数主体

		constructor_declaration ::=
		        constructor_declarator constructor_body
		    ;
    
构造函数修饰符可以产生修饰符选项 + 标识符 + 左圆括号 + 形式参数列表 + 右圆括号

		constructor_declarator ::=
		        modifiers_opt IDENTIFIER
		        LPAREN formal_parameter_list_opt RPAREN
		    ;
    
构造函数主体可以产生 左花括号 + 隐式构造函数调用 + 语句块语句选项 + 右花括号

		constructor_body ::=
		        LBRACE explicit_constructor_invocation block_statements_opt RBRACE
		    |   LBRACE block_statements_opt RBRACE
		    ;

隐式构造函数调用可以产生 this关键词 + 左圆括号 + 形式参数列表 + 右圆括号 + 分号

		explicit_constructor_invocation ::=
		        THIS LPAREN argument_list_opt RPAREN SEMICOLON
		    |   SUPER LPAREN argument_list_opt RPAREN SEMICOLON
		    |   primary DOT THIS LPAREN argument_list_opt RPAREN SEMICOLON
		    |   primary DOT SUPER LPAREN argument_list_opt RPAREN SEMICOLON
		    ;

- - - - - 
######语句
- - - - -

语句块可以产生左花括号 + 语句块语句选项 + 右花括号

		block ::=
		        LBRACE block_statements_opt RBRACE
		    ;
    
语句块语句选项可以产生语句块语句系列；或者推出空

		block_statements_opt ::=
			    |   block_statements
			    ;
			    
语句块语句系列可以产生语句块语句；或者推出语句块语句系列、语句块语句

		block_statements ::=
		        block_statement
		    |   block_statements block_statement
		    |   error  block_statement 
		    ;
语句块语句可以产生局部变量声明语句、一般语句

		block_statement ::=
		        local_variable_declaration_statement
		    |   statement
		    ;
    
局部变量声明语句可以产生类型、变量声明系列、分号

		local_variable_declaration_statement ::=
		        type variable_declarators SEMICOLON
		    |   type error SEMICOLON
		    ;
一般语句可以产生无尾随子句的简单语句；或者推出if-then语句；或者推出if-then-else语句；或者推出while语句

		statement ::=
		        statement_without_trailing_substatement
		    |   if_then_statement
		    |   if_then_else_statement
		    |   while_statement
		    ;
可以产生无尾随子句的简单语句、

		statement_no_short_if ::=
		        statement_without_trailing_substatement
		    |   if_then_else_statement_no_short_if
		    |   while_statement_no_short_if
		    ;
无尾随子句的简单语句可以产生语句块；或者推出空语句；或者推出表达式语句；或者推出返回语句

		statement_without_trailing_substatement ::=
		        block
		    |   empty_statement
		    |   expression_statement
		    |   return_statement
		    ;
空语句

		empty_statement ::=
		        SEMICOLON
		    ;
表达式语句可以产生语句表达式 + 分号

		expression_statement ::=
		        statement_expression SEMICOLON
		    ;
语句表达式可以产生赋值；或者推出方法调用；或者推出实例化语句

		statement_expression ::=
		        assignment
		    |   method_invocation
		    |   class_instance_creation_expression
		    ;
if-then语句可以产生if + 左圆括号 + 表达式 + 右圆括号 + 语句

		if_then_statement ::=
		        IF LPAREN expression RPAREN statement
		    |   IF error 
		            {: parser.report_error("if_then_statement","WRONG"); :} 
		        RPAREN statement
		    ;

if-then-else语句可以产生if + 左圆括号 + 表达式 + 右圆括号 +    + else + 语句

		if_then_else_statement ::=
		        IF LPAREN expression RPAREN statement_no_short_if ELSE statement
		    |   IF LPAREN error RPAREN statement_no_short_if ELSE statement
		    ;

		if_then_else_statement_no_short_if ::=
		        IF LPAREN expression RPAREN statement_no_short_if ELSE statement_no_short_if
		    ;

while语句可以产生WHILE + 左圆括号 + 表达式 + 右圆括号 + 语句

		while_statement ::=
		        WHILE LPAREN expression RPAREN statement
		    |   WHILE error 
		            {: parser.report_error("expression","WRONG"); :} 
		        RPAREN statement
		    ;
    

		while_statement_no_short_if ::=
		        WHILE LPAREN expression RPAREN statement_no_short_if
		    ;

返回语句
		return_statement ::=
		        RETURN expression_opt SEMICOLON
		      ;

- - - - - 
######表达式
- - - - -

基本表达式可以推出非数组创建表达式；或者推出

		primary ::= 
		        primary_no_new_array
		    |   array_creation_expression
		    ;

非数组创建表达式可以产生字面值；或者推出THIS；或者推出左圆括号 + 表达式 + 右圆括号；或者推出

		primary_no_new_array ::=
		        literal
		    |   THIS
		    |   LPAREN expression RPAREN
		    |   class_instance_creation_expression
		    |   field_access
		    |   method_invocation
		    |   array_access
		    |   primitive_type DOT CLASS
		    |   VOID DOT CLASS
		    |   array_type DOT CLASS
		    |   name DOT CLASS
		    |   name DOT THIS
		    |   LPAREN error RPAREN
		    |   error DOT THIS 
		    ;

对象实例化语句可以产生NEW + name + 左圆括号 + 参数列表选项 + 右圆括号 + 类主体选项

		class_instance_creation_expression ::=
		        NEW name LPAREN argument_list_opt RPAREN class_body_opt
		    |   primary DOT NEW IDENTIFIER
		            LPAREN argument_list_opt RPAREN class_body_opt
		    ;
类主体选项可以产生空、类主体

		class_body_opt ::=
		    |   class_body
		;
参数列表选项

		argument_list_opt ::=
		    |   argument_list
		    ;
参数列表

		argument_list ::=
		        expression
		    |   argument_list COMMA expression
		    |   error expression
		    ;
数组创建表达式

		array_creation_expression ::=
		        NEW primitive_type dim_exprs dims_opt
		    |   NEW name dim_exprs dims_opt
		    ;
数组维表达式系列

		dim_exprs ::=
		        dim_expr
		    |   dim_exprs dim_expr
		    |   error dim_expr
		    ;
数组维表达式

		dim_expr ::=    LBRACK expression RBRACK
		    ;
数组维选项

		dims_opt ::=
		    |   dims
		    ;
数组维系列
		dims ::=
		        LBRACK RBRACK
		    |   dims LBRACK RBRACK
		    ;

成员变量获取

		field_access ::=
		        primary DOT IDENTIFIER
		    |   SUPER DOT IDENTIFIER
		    |   name DOT SUPER DOT IDENTIFIER
		    ;
    
方法调用

		method_invocation ::=
		        name LPAREN argument_list_opt RPAREN
		    |   primary DOT IDENTIFIER LPAREN argument_list_opt RPAREN
		    |   SUPER DOT IDENTIFIER LPAREN argument_list_opt RPAREN
		    |   name DOT SUPER DOT IDENTIFIER LPAREN argument_list_opt RPAREN
		    ;
数组获取

		array_access ::=
		        name LBRACK expression RBRACK
		    |   primary_no_new_array LBRACK expression RBRACK
		    ;
后缀表达式

		postfix_expression ::=
		        primary
		    |   name
		    |   AT name
		    ;
一元表达式

		unary_expression ::=
		        postfix_expression
		    |   NOT unary_expression
		    |   PLUS unary_expression
		    |   MINUS unary_expression
		    ;
二级运算表达式

		multiplicative_expression ::=
		        unary_expression
		    |   multiplicative_expression MULT unary_expression
		    |   multiplicative_expression DIV unary_expression
		    |   multiplicative_expression MOD unary_expression
		    |   error MULT unary_expression
		    |   error DIV unary_expression    
		    |   error MOD unary_expression
		    ;
一级运算表达式

		additive_expression ::=
		        multiplicative_expression
		    |   additive_expression PLUS multiplicative_expression
		    |   additive_expression MINUS multiplicative_expression
		    ;

关系表达式

		relational_expression ::=
		        additive_expression
		    |   relational_expression LT additive_expression
		    |   relational_expression GT additive_expression
		    |   relational_expression LTEQ additive_expression
		    |   relational_expression GTEQ additive_expression
		    |   error LT additive_expression
		    |   error GT additive_expression
		    |   error LTEQ additive_expression
		    |   error GTEQ additive_expression
		    ;
相等性表达式

		equality_expression ::=
		        relational_expression
		    |   equality_expression EQEQ relational_expression
		    |   equality_expression NOTEQ relational_expression
		    |   error EQEQ relational_expression
		    |   error NOTEQ relational_expression
		    ;
逻辑与表达式

		conditional_and_expression ::=
		        equality_expression
		    |   conditional_and_expression ANDAND equality_expression
		    |   error ANDAND equality_expression
		    ;
逻辑或表达式

		conditional_or_expression ::=
		        conditional_and_expression
		    |   conditional_or_expression OROR conditional_and_expression
		    |   error OROR conditional_and_expression
		    ;

条件表达式

		conditional_expression ::=
		        conditional_or_expression
		    |   conditional_or_expression QUESTION expression COLON conditional_expression
		    |   error QUESTION expression COLON conditional_or_expression
		    ;
赋值表达式

		assignment_expression ::=
		        conditional_expression
		    |   assignment
		    ;
赋值

		assignment ::=
		        left_hand_side EQ assignment_expression
		    |   error EQ assignment_expression
		    ;
赋值左端

		left_hand_side ::=
		        name
		    |   field_access
		    |   array_access
		    ;
表达式选项

		expression_opt ::=
		    |   expression
		    ;
    
表达式可以产生赋值语句

		expression ::=  assignment_expression
		    ;

- - - - - 
######类型
- - - - -

类型可以产生基本类型和引用类型

		type    ::=
		        primitive_type
		    |   reference_type
		    ;
    
基本类型可以产生数字类型和布尔类型

		primitive_type ::=
		        numeric_type
		    |   BOOLEAN
		    ;
    
数字类型可以产生整型、字符型、浮点型

		numeric_type ::= 
		        INT
		    |   CHAR
		    |   FLOAT
		    ;
    
引用类型可以产生name和数组类型

		reference_type ::=
		        name
		    |   array_type
		    ;
    
数组类型可以产生基本类型 + 维数；或者推出name + 维数

		array_type ::=
		        primitive_type dims
		    |   name dims
		    ;
name可以产生标识符；或者推出name + DOT + 标识符
		
		name    ::=
		        IDENTIFIER
		    |   name DOT IDENTIFIER
		    ;
		modifiers_opt::=
		    |   PUBLIC
		    ;


