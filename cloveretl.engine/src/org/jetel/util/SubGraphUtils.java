/*
 * jETeL/CloverETL - Java based ETL application framework.
 * Copyright (c) Javlin, a.s. (info@cloveretl.com)
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jetel.util;

/**
 * Utility class for sub-graph related code.
 * 
 * @author Kokon (info@cloveretl.com)
 *         (c) Javlin, a.s. (www.cloveretl.com)
 *
 * @created 12.4.2013
 */
public class SubGraphUtils {

	/** Type of SubGraphInput component. */
	public static final String SUB_GRAPH_INPUT_TYPE = "SUB_GRAPH_INPUT";
	/** Type of SubGraphOutput component. */
	public static final String SUB_GRAPH_OUTPUT_TYPE = "SUB_GRAPH_OUTPUT";

	/**
	 * @return true if and only if the given component type is SubGraphInput component.
	 */
	public static boolean isSubGraphInput(String componentType) {
		return SUB_GRAPH_INPUT_TYPE.equals(componentType);
	}

	/**
	 * @return true if and only if the given component type is SubGraphOutput component.
	 */
	public static boolean isSubGraphOutput(String componentType) {
		return SUB_GRAPH_OUTPUT_TYPE.equals(componentType);
	}

}