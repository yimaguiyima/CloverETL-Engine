/* Generated By:JJTree: Do not edit this line. CLVFUppercaseNode.java */

package org.jetel.interpreter;


public class CLVFUppercaseNode extends SimpleNode {
	
	StringBuffer strBuf;
	
	public CLVFUppercaseNode(int id) {
		super(id);
		strBuf=new StringBuffer();
	}
	
	public CLVFUppercaseNode(FilterExpParser p, int id) {
	    super(p, id);
	    strBuf=new StringBuffer();
	  }
	
	/** Accept the visitor. **/
	public Object jjtAccept(FilterExpParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
