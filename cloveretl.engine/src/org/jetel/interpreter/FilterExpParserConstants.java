/* Generated By:JJTree&JavaCC: Do not edit this line. FilterExpParserConstants.java */
package org.jetel.interpreter;

public interface FilterExpParserConstants {

  int EOF = 0;
  int OR = 6;
  int AND = 7;
  int NOT = 8;
  int INTEGER_LITERAL = 9;
  int DIGIT = 10;
  int DECIMAL_LITERAL = 11;
  int HEX_LITERAL = 12;
  int OCTAL_LITERAL = 13;
  int FLOATING_POINT_LITERAL = 14;
  int EXPONENT = 15;
  int STRING_LITERAL = 16;
  int QUOTED_STRING = 17;
  int DQUOTED_STRING = 18;
  int DATE_LITERAL = 19;
  int DATETIME_LITERAL = 20;
  int EQUAL = 21;
  int NON_EQUAL = 22;
  int LESS_THAN = 23;
  int LESS_THAN_EQUAL = 24;
  int GREATER_THAN = 25;
  int GREATER_THAN_EQUAL = 26;
  int REGEX_EQUAL = 27;
  int CMPOPERATOR = 28;
  int MINUS = 29;
  int PLUS = 30;
  int MULTIPLY = 31;
  int DIVIDE = 32;
  int MODULO = 33;
  int FIELD_ID = 34;
  int OPEN_PAR = 35;
  int CLOSE_PAR = 36;
  int DATE_FIELD_LITERAL = 48;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\n\\r\"",
    "<OR>",
    "<AND>",
    "<NOT>",
    "<INTEGER_LITERAL>",
    "<DIGIT>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<OCTAL_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<STRING_LITERAL>",
    "<QUOTED_STRING>",
    "<DQUOTED_STRING>",
    "<DATE_LITERAL>",
    "<DATETIME_LITERAL>",
    "<EQUAL>",
    "<NON_EQUAL>",
    "<LESS_THAN>",
    "<LESS_THAN_EQUAL>",
    "<GREATER_THAN>",
    "<GREATER_THAN_EQUAL>",
    "<REGEX_EQUAL>",
    "<CMPOPERATOR>",
    "\"-\"",
    "\"+\"",
    "\"*\"",
    "\"/\"",
    "\"%\"",
    "<FIELD_ID>",
    "\"(\"",
    "\")\"",
    "\";\"",
    "\"substring\"",
    "\",\"",
    "\"uppercase\"",
    "\"lowercase\"",
    "\"trim\"",
    "\"length\"",
    "\"today\"",
    "\"isnull\"",
    "\"concat\"",
    "\"dateadd\"",
    "<DATE_FIELD_LITERAL>",
  };

}
