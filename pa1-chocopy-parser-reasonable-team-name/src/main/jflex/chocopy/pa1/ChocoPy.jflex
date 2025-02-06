package chocopy.pa1;
import java_cup.runtime.*;
import java.util.Stack;

%%

/*** Do not change the flags below unless you know what you are doing. ***/

%unicode
%line
%column

%class ChocoPyLexer
%public

%cupsym ChocoPyTokens
%cup
%cupdebug

%eofclose false

/*** Do not change the flags above unless you know what you are doing. ***/

/* The following code section is copied verbatim to the
 * generated lexer class. */
%{
    /* The code below includes some convenience methods to create tokens
     * of a given type and optionally a value that the CUP parser can
     * understand. Specifically, a lot of the logic below deals with
     * embedded information about where in the source code a given token
     * was recognized, so that the parser can report errors accurately.
     * (It need not be modified for this project.) */

    /** Producer of token-related values for the parser. */
    final ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();

    /** Return a terminal symbol of syntactic category TYPE and no
     *  semantic value at the current source location. */
    private Symbol symbol(int type) {
        return symbol(type, yytext());
    }

    /** Return a terminal symbol of syntactic category TYPE and semantic
     *  value VALUE at the current source location. */
    private Symbol symbol(int type, Object value) {
        return symbolFactory.newSymbol(ChocoPyTokens.terminalNames[type], type,
            new ComplexSymbolFactory.Location(yyline + 1, yycolumn + 1),
            new ComplexSymbolFactory.Location(yyline + 1,yycolumn + yylength()),
            value);
    }

    /* Some utils added to handle with indent and dedent */
    private Stack<Integer> indentStack = new Stack<>();
    private int indentLevel = 0;
    public int numPendingDedents = 0;

    private int getIndentLevel() {
        int spaces = 0;
        for (char c : yytext().toCharArray()) {
            if (c == ' ') {
                spaces++;
            } else if (c == '\t') {
                spaces += 8 - (spaces % 8);  
            }
        }
        return spaces;
    }
    private Symbol handleIndentation() {
        if (numPendingDedents > 0) {
            numPendingDedents--;
            return symbol(ChocoPyTokens.DEDENT);
        }

        int newIndentLevel = getIndentLevel();

        if (newIndentLevel > indentStack.peek()) {
            indentStack.push(newIndentLevel);
            return symbol(ChocoPyTokens.INDENT);
        }

        while (newIndentLevel < indentStack.peek()) {
            indentStack.pop();
            numPendingDedents++;
        }

        if (newIndentLevel != indentStack.peek()) {
            return symbol(ChocoPyTokens.UNRECOGNIZED);
        }

        if (numPendingDedents > 0) {
            numPendingDedents--;
            return symbol(ChocoPyTokens.DEDENT);
        }

        return null; 
}

%}

%init{
      indentStack.push(0);
%init}

/* Macros (regexes used in rules below) */

WhiteSpace = [ \t]
LineBreak  = \r|\n|\r\n

IntegerLiteral = 0 | [1-9][0-9]*
StringLiteral = \"([^\"\\]|\\.)*\"

Identifier = [a-zA-Z_][a-zA-Z_0-9]*

%state STMT
%state MULTIDEDENT

%%


<YYINITIAL> {

  /* Handle indentation when there are non-whitespace characters. */
  ^{WhiteSpace}*{LineBreak}    { /* Blank lines are ignored. */ }

  /* Handle indentation when there are non-whitespace characters. */
  ^{WhiteSpace}+               { 
                                  Symbol indent = handleIndentation();
                                //   System.out.println(indentLevel);
                                  if (numPendingDedents != 0){
                                    yypushback(1);
                                    yybegin(MULTIDEDENT);
                                    return indent;
                                  }
                                  if (indent != null) {
                                      yybegin(STMT); 
                                      return indent; 
                                  }
                                  yybegin(STMT);
                               }

  ^[^\s]                       { 
                                
                                  Symbol indent = handleIndentation();
                                //   System.out.println(numPendingDedents);
                                  if (numPendingDedents != 0){
                                    yypushback(1);
                                    yybegin(YYINITIAL);
                                    return indent;
                                  }
                                  if (indent != null) {
                                      yybegin(STMT); 
                                      yypushback(1); 

                                      return indent;
                                  }
                                  yybegin(STMT);  
                                  yypushback(1);  
                               }


}

<MULTIDEDENT>{
    {WhiteSpace}+               {
                                    if (numPendingDedents != 0){
                                        numPendingDedents--;
                                        yypushback(1);
                                        yybegin(MULTIDEDENT);
                                        return symbol(ChocoPyTokens.DEDENT);
                                    }
                                    else {
                                        yybegin(STMT);
                                    }
    }
}

<STMT>{

  /* Handle end of logical line with tokens, generating NEWLINE */

  /* Literals. */
  {IntegerLiteral}            { return symbol(ChocoPyTokens.NUMBER,
                                                 Integer.parseInt(yytext())); }



  "True"                      { return symbol(ChocoPyTokens.TRUE); }
  "False"                     { return symbol(ChocoPyTokens.FALSE); }
  "None"                      { return symbol(ChocoPyTokens.NONE); }
  "and"                       { return symbol(ChocoPyTokens.AND); }
  "or"                        { return symbol(ChocoPyTokens.OR); }
  "not"                       { return symbol(ChocoPyTokens.NOT); }
  "if"                        { return symbol(ChocoPyTokens.IF); }
  "else"                      { return symbol(ChocoPyTokens.ELSE); }
  "pass"                      { return symbol(ChocoPyTokens.PASS); }
  "class"                     { return symbol(ChocoPyTokens.CLASS); }
  "global"                    { return symbol(ChocoPyTokens.GLOBAL); }
  "nonlocal"                  { return symbol(ChocoPyTokens.NONLOCAL); }
  "return"                    { return symbol(ChocoPyTokens.RETURN); }
  "def"                       { return symbol(ChocoPyTokens.DEF); }
  "while"                     { return symbol(ChocoPyTokens.WHILE); }
  "for"                       { return symbol(ChocoPyTokens.FOR); }
  "in"                        { return symbol(ChocoPyTokens.IN); }
  "elif"                      { return symbol(ChocoPyTokens.ELIF); }
  "is"                        { return symbol(ChocoPyTokens.IS); }

  {Identifier}                { return symbol(ChocoPyTokens.IDENTIFIER, yytext()); }

  {StringLiteral}             {String Value = yytext().substring(1, yytext().length()-1);
                                return symbol(ChocoPyTokens.STRING_LITERAL, Value); }
    /* Operators. */
  ":"                         { return symbol(ChocoPyTokens.COLON, yytext()); }
  "+"                         { return symbol(ChocoPyTokens.PLUS, yytext()); }
  "-"                         { return symbol(ChocoPyTokens.MINUS, yytext()); }
  "="                         { return symbol(ChocoPyTokens.EQ, yytext()); }
  "=="                        { return symbol(ChocoPyTokens.EQEQ, yytext()); }
  "!="                        { return symbol(ChocoPyTokens.NOTEQ, yytext()); }
  "*"                         { return symbol(ChocoPyTokens.MULTIPLE, yytext()); }
  "//"                        { return symbol(ChocoPyTokens.DIVIDE, yytext()); }
  "%"                         { return symbol(ChocoPyTokens.MOD, yytext()); }
  "("                         { return symbol(ChocoPyTokens.LPAR, yytext()); }
  ")"                         { return symbol(ChocoPyTokens.RPAR, yytext()); }
  "["                         { return symbol(ChocoPyTokens.LBRA, yytext()); }
  "]"                         { return symbol(ChocoPyTokens.RBRA, yytext()); }
  ","                         { return symbol(ChocoPyTokens.COMMA, yytext()); }
  "."                         { return symbol(ChocoPyTokens.DOT, yytext()); }
  "->"                        { return symbol(ChocoPyTokens.ARROW, yytext()); }
  ">"                         { return symbol(ChocoPyTokens.GREATER, yytext()); }
  "<"                         { return symbol(ChocoPyTokens.LESS, yytext()); }
  ">="                        { return symbol(ChocoPyTokens.GREATEREQ, yytext()); }
  "<="                        { return symbol(ChocoPyTokens.LESSEQ, yytext()); }


  "#"[^\r\n]*                 { /* Ignore single-line comments */ }
  /* Whitespace. */
  {WhiteSpace}                { /* ignore */ }

  {LineBreak} {
        yybegin(YYINITIAL);
        return symbol(ChocoPyTokens.NEWLINE);
    }


}

//<<EOF>>                       { return symbol(ChocoPyTokens.EOF); }

<<EOF>> {
     while (indentStack.size() > 1) {
        indentStack.pop();
        return symbol(ChocoPyTokens.DEDENT);
    }
    return symbol(ChocoPyTokens.EOF);
}

/* Error fallback. */
[^]                           { return symbol(ChocoPyTokens.UNRECOGNIZED); }