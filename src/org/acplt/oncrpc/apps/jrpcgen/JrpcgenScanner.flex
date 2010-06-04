/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenScanner.flex,v 1.1.1.1 2003/08/13 12:03:47 haraldalbrecht Exp $
 *
 * Copyright (c) 1999, 2000
 * Lehrstuhl fuer Prozessleittechnik (PLT), RWTH Aachen
 * D-52064 Aachen, Germany.
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * To compile into java code use:
 *   java -jar <whereever/>JFlex.jar JrpcgenScanner.flex
 */

package org.acplt.oncrpc.apps.jrpcgen;
import org.acplt.oncrpc.apps.jrpcgen.cup_runtime.*;

%%

%class JrpcgenScanner
%unicode
// Do not use %cup directive here as this causes JFlex to create a parser
// class which tries to always implement java_cup.runtime.Scanner...
//%cup
%implements org.acplt.oncrpc.apps.jrpcgen.cup_runtime.Scanner
%function next_token
%type org.acplt.oncrpc.apps.jrpcgen.cup_runtime.Symbol
%eofval{
    return new Symbol(JrpcgenSymbols.EOF);
%eofval}
%eofclose
%line
%column

%{
  StringBuffer string = new StringBuffer();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline+1, yycolumn+1);
  }

  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline+1, yycolumn+1, value);
  }

  /* assumes correct representation of a long value for
     specified radix in String s */
  private long parseLong(String s, int radix) {
    int  max = s.length();
    long result = 0;
    long digit;

    for (int i = 0; i < max; i++) {
      digit  = Character.digit(yy_buffer[i],radix);
      result*= radix;
      result+= digit;
    }

    return result;
  }
%}

//
// Macros
//
LINE_TERMINATOR=\r|\n|\r\n
INPUT_CHARACTER=[^\r\n]

WHITE_SPACE={LINE_TERMINATOR}|[ \t\f]

JAVA_COMMENT={MULTILINE_COMMENT}|{EOL_COMMENT}
MULTILINE_COMMENT = "/*"{COMMENT_CONTENT}\*+"/"
EOL_COMMENT="//".*{LINE_TERMINATOR}
COMMENT_CONTENT=([^*]|\*+[^*/])*
C_COMPILER_DIRECTIVE="%".*{LINE_TERMINATOR}


IDENTIFIER=[a-zA-Z_][a-zA-Z0-9_]*
INTEGER_LITERAL = [1-9][0-9]*|"0x"[0-9A-Fa-f]+|0[0-7]+|0|-[1-9][0-9]*


%%


<YYINITIAL> {

// keywords
    "program"           { return symbol(JrpcgenSymbols.PROGRAM); }
    "version"           { return symbol(JrpcgenSymbols.VERSION); }
    "PROGRAM"           { return symbol(JrpcgenSymbols.PROGRAM); }
    "VERSION"           { return symbol(JrpcgenSymbols.VERSION); }

    "const"             { return symbol(JrpcgenSymbols.CONST); }
    "typedef"           { return symbol(JrpcgenSymbols.TYPEDEF); }

    "switch"            { return symbol(JrpcgenSymbols.SWITCH); }
    "case"              { return symbol(JrpcgenSymbols.CASE); }
    "default"           { return symbol(JrpcgenSymbols.DEFAULT); }

// data types
    "void"              { return symbol(JrpcgenSymbols.VOID); }
    "char"              { return symbol(JrpcgenSymbols.CHAR); }
    "short"             { return symbol(JrpcgenSymbols.SHORT); }
    "u_short"           { return symbol(JrpcgenSymbols.SHORT); }
    "int"               { return symbol(JrpcgenSymbols.INT); }
    "u_int"             { return symbol(JrpcgenSymbols.INT); }
    "long"              { return symbol(JrpcgenSymbols.LONG); }
    "u_long"            { return symbol(JrpcgenSymbols.LONG); }
    "hyper"             { return symbol(JrpcgenSymbols.HYPER); }
    "float"             { return symbol(JrpcgenSymbols.FLOAT); }
    "double"            { return symbol(JrpcgenSymbols.DOUBLE); }
    "quadruple"         { return symbol(JrpcgenSymbols.QUADRUPLE); }
    "bool"              { return symbol(JrpcgenSymbols.BOOL); }
    "bool_t"            { return symbol(JrpcgenSymbols.BOOL); }
    "enum"              { return symbol(JrpcgenSymbols.ENUM); }
    "opaque"            { return symbol(JrpcgenSymbols.OPAQUE); }
    "string"            { return symbol(JrpcgenSymbols.STRING); }
    "struct"            { return symbol(JrpcgenSymbols.STRUCT); }
    "union"             { return symbol(JrpcgenSymbols.UNION); }

// modifiers
    "unsigned"          { return symbol(JrpcgenSymbols.UNSIGNED); }

// separators
    ";"                 { return symbol(JrpcgenSymbols.SEMICOLON); }
    ","                 { return symbol(JrpcgenSymbols.COMMA); }
    ":"                 { return symbol(JrpcgenSymbols.COLON); }
    "="                 { return symbol(JrpcgenSymbols.EQUAL); }
    "*"                 { return symbol(JrpcgenSymbols.STAR); }
    "("                 { return symbol(JrpcgenSymbols.LPAREN); }
    ")"                 { return symbol(JrpcgenSymbols.RPAREN); }
    "{"                 { return symbol(JrpcgenSymbols.LBRACE); }
    "}"                 { return symbol(JrpcgenSymbols.RBRACE); }
    "["                 { return symbol(JrpcgenSymbols.LBRACKET); }
    "]"                 { return symbol(JrpcgenSymbols.RBRACKET); }
    "<"                 { return symbol(JrpcgenSymbols.LANGLE); }
    ">"                 { return symbol(JrpcgenSymbols.RANGLE); }

// integer literals
    {INTEGER_LITERAL} {
        return symbol(JrpcgenSymbols.INTEGER_LITERAL, yytext());
    }

// identifiers: simple return the identifier as the value of the symbol.
    {IDENTIFIER} {
        return symbol(JrpcgenSymbols.IDENTIFIER, yytext());
    }

// white space & comment handling
    {WHITE_SPACE}       { /* ignore */ }
    {JAVA_COMMENT}      { /* ignore */ }
    {C_COMPILER_DIRECTIVE}  { /* ignore */ }

}

. | \n                  { throw new Error("Illegal character \"" + yytext() + "\""); }

/* End of file JrpcgenScanner.flex */

