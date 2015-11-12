/* MiniJava 词法分析规约 */

import java_cup.runtime.*;

%%
//====================================================================================
//====================================================================================
/* 声明为公共类*/
%public
%class Scanner
/* 实现了sym接口*/
%implements sym

%unicode

%line
%column

/* CUP适配模式*/
%cup
%cupdebug

%{
  StringBuffer string = new StringBuffer();
  
  private Symbol symbol(int type) {
    return new JavaSymbol(type, yyline+1, yycolumn+1, yytext());
  }

  private Symbol symbol(int type, Object value) {
    return new JavaSymbol(type, yyline+1, yycolumn+1, yytext(), value);
  }

  public String current_lexeme(){
    int l = yyline+1;
    int c = yycolumn+1;
    return " (line: "+l+" , column: "+c+" , lexeme: '"+yytext()+"')";
  }

  /** 
   * assumes correct representation of a long value for 
   * specified radix in scanner buffer from <code>start</code> 
   * to <code>end</code> 
   */
  private long parseLong(int start, int end, int radix) {
    long result = 0;
    long digit;

    for (int i = start; i < end; i++) {
      digit  = Character.digit(yycharat(i),radix);
      result*= radix;
      result+= digit;
    }

    return result;
  }
%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]

/* 注释 */
Comment = {TraditionalComment} | {EndOfLineComment} | 
          {DocumentationComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/*" "*"+ [^/*] ~"*/"

/* 标识符 */
Identifier = [:jletter:][:jletterdigit:]*

/* 整型字面值   */
DecIntegerLiteral = 0 | [1-9][0-9]*

/* 浮点数字面值 */        
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? 

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

/* 字符串和字符字面值 */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]


/* 定义状态字符串读取状态，字符读取状态*/
%state STRING, CHARLITERAL




%%
//====================================================================================
//====================================================================================
/* 分三种状态进行匹配：默认开始状态、字符串读取状态，字符读取状态 */

/* 默认开始状态匹配的token*/
<YYINITIAL> {

  /* MiniJava关键词 */
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

  /* 布尔字面值 */
  "true"                         { return symbol(BOOLEAN_LITERAL, new Boolean(true)); }
  "false"                        { return symbol(BOOLEAN_LITERAL, new Boolean(false)); }
  
  /* 空字面值 */
  "null"                         { return symbol(NULL_LITERAL); }
  
  
  /* 分隔符 */
  "("                            { return symbol(LPAREN); }
  ")"                            { return symbol(RPAREN); }
  "{"                            { return symbol(LBRACE); }
  "}"                            { return symbol(RBRACE); }
  "["                            { return symbol(LBRACK); }
  "]"                            { return symbol(RBRACK); }
  ";"                            { return symbol(SEMICOLON); }
  ","                            { return symbol(COMMA); }
  "."                            { return symbol(DOT); }
  
  /* 运算符 */
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
  "&"                            { return symbol(AT); }
  "||"                           { return symbol(OROR); } 
  "+"                            { return symbol(PLUS); }
  "-"                            { return symbol(MINUS); }
  "*"                            { return symbol(MULT); }
  "/"                            { return symbol(DIV); }
  "%"                            { return symbol(MOD); }
  
  /* 进入字符串读取状态 */
  \"                             { yybegin(STRING); string.setLength(0); }

  /* 进入字符读取状态   */
  \'                             { yybegin(CHARLITERAL); }

  /* 数字字面值   */

  {DecIntegerLiteral}            { return symbol(INTEGER_LITERAL, new Integer(yytext())); }

 
  {FloatLiteral}                 { return symbol(FLOATING_POINT_LITERAL, new Float(yytext().substring(0,yylength()))); }
  
  /* 注释 */
  {Comment}                      { /* ignore */ }

  /* 空白 */
  {WhiteSpace}                   { /* ignore */ }

  /* 标识符 */ 
  {Identifier}                   { return symbol(IDENTIFIER, yytext()); }  
}


/* 字符串读取状态*/
<STRING> {
  //读取到第二个双引号，则进入默认开始状态
  \"                             { yybegin(YYINITIAL); return symbol(STRING_LITERAL, string.toString()); }
  
  {StringCharacter}+             { string.append( yytext() ); }
  
  /* 转义字符 */
  "\\b"                          { string.append( '\b' ); }
  "\\t"                          { string.append( '\t' ); }
  "\\n"                          { string.append( '\n' ); }
  "\\f"                          { string.append( '\f' ); }
  "\\r"                          { string.append( '\r' ); }
  "\\\""                         { string.append( '\"' ); }
  "\\'"                          { string.append( '\'' ); }
  "\\\\"                         { string.append( '\\' ); }

  
  /* 错误情况，字符串内不应该出现以下字符，出现则报错 */
  \\.                            { throw new RuntimeException("Illegal escape sequence \""+yytext()+"\""); }
  {LineTerminator}               { throw new RuntimeException("Unterminated string at end of line"); }
}


/* 字符读取状态*/
<CHARLITERAL> {
  //读取到第二个单引号，则进入默认开始状态
  {SingleCharacter}\'            { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character(yytext().charAt(0))); }
  
  /* 转义字符 */
  "\\b"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\b'));}
  "\\t"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\t'));}
  "\\n"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\n'));}
  "\\f"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\f'));}
  "\\r"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\r'));}
  "\\\""\'                       { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\"'));}
  "\\'"\'                        { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\''));}
  "\\\\"\'                       { yybegin(YYINITIAL); return symbol(CHARACTER_LITERAL, new Character('\\')); }

  
  /* 错误情况，字符不应该出现以下字符，出现则报错 */
  {LineTerminator}               { throw new RuntimeException("Unterminated character literal at end of line"); }
}

/* 读取到其他字符则同样报错 */

.|\n                             { return symbol(ILLEGAL_CHARACTER, yytext());}
<<EOF>>                          { return symbol(EOF); }