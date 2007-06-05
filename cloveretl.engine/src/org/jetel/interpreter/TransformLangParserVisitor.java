/* Generated By:JJTree: Do not edit this line. C:\projects\eclipse\cloveretl.engine\src\org\jetel\interpreter\TransformLangParserVisitor.java */

package org.jetel.interpreter;

import org.jetel.interpreter.ASTnode.*;

public interface TransformLangParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(CLVFStart node, Object data);
  public Object visit(CLVFStartExpression node, Object data);
  public Object visit(CLVFImportSource node, Object data);
  public Object visit(CLVFFunctionDeclaration node, Object data);
  public Object visit(CLVFVarDeclaration node, Object data);
  public Object visit(CLVFAssignment node, Object data);
  public Object visit(CLVFMapping node, Object data);
  public Object visit(CLVFOr node, Object data);
  public Object visit(CLVFAnd node, Object data);
  public Object visit(CLVFComparison node, Object data);
  public Object visit(CLVFAddNode node, Object data);
  public Object visit(CLVFSubNode node, Object data);
  public Object visit(CLVFMulNode node, Object data);
  public Object visit(CLVFDivNode node, Object data);
  public Object visit(CLVFModNode node, Object data);
  public Object visit(CLVFMinusNode node, Object data);
  public Object visit(CLVFPlusPlusNode node, Object data);
  public Object visit(CLVFMinusMinusNode node, Object data);
  public Object visit(CLVFNegation node, Object data);
  public Object visit(CLVFSizeNode node, Object data);
  public Object visit(CLVFLiteral node, Object data);
  public Object visit(CLVFInputFieldLiteral node, Object data);
  public Object visit(CLVFGlobalParameterLiteral node, Object data);
  public Object visit(CLVFVariableLiteral node, Object data);
  public Object visit(CLVFRegexLiteral node, Object data);
  public Object visit(CLVFBlock node, Object data);
  public Object visit(CLVFIfStatement node, Object data);
  public Object visit(CLVFSwitchStatement node, Object data);
  public Object visit(CLVFCaseExpression node, Object data);
  public Object visit(CLVFWhileStatement node, Object data);
  public Object visit(CLVFForStatement node, Object data);
  public Object visit(CLVFForeachStatement node, Object data);
  public Object visit(CLVFDoStatement node, Object data);
  public Object visit(CLVFBreakStatement node, Object data);
  public Object visit(CLVFContinueStatement node, Object data);
  public Object visit(CLVFReturnStatement node, Object data);
  public Object visit(CLVFFunctionCallStatement node, Object data);
  public Object visit(CLVFIsNullNode node, Object data);
  public Object visit(CLVFNVLNode node, Object data);
  public Object visit(CLVFIffNode node, Object data);
  public Object visit(CLVFTruncNode node, Object data);
  public Object visit(CLVFPrintStackNode node, Object data);
  public Object visit(CLVFBreakpointNode node, Object data);
  public Object visit(CLVFRaiseErrorNode node, Object data);
  public Object visit(CLVFDateAddNode node, Object data);
  public Object visit(CLVFDateDiffNode node, Object data);
  public Object visit(CLVFDate2StrNode node, Object data);
  public Object visit(CLVFStr2DateNode node, Object data);
  public Object visit(CLVFDate2NumNode node, Object data);
  public Object visit(CLVFStr2NumNode node, Object data);
  public Object visit(CLVFNum2StrNode node, Object data);
  public Object visit(CLVFPrintErrNode node, Object data);
  public Object visit(CLVFPrintLogNode node, Object data);
  public Object visit(CLVFSequenceNode node, Object data);
  public Object visit(CLVFLookupNode node, Object data);
}
