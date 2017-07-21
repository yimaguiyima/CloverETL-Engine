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
package org.jetel.component.tree.writer.model.runtime;

import org.jetel.component.tree.writer.TreeFormatter;
import org.jetel.data.DataRecord;
import org.jetel.exception.JetelException;

/**
 * class representing xml namespace declaration
 * 
 * @author lkrejci (info@cloveretl.com) (c) Javlin, a.s. (www.cloveretl.com)
 * 
 * @created 20 Dec 2010
 */
public class WritableNamespace extends BaseWritable {

	private final char[] prefix;
	private final char[] namespaceURI;

	public WritableNamespace(String prefix, String namespaceURI) {
		this.prefix = prefix == null ? null : prefix.toCharArray();
		this.namespaceURI = namespaceURI.toCharArray();
	}

	@Override
	public void write(TreeFormatter formatter, DataRecord[] availableData) throws JetelException {
		formatter.getNamespaceWriter().writeNamespace(prefix, namespaceURI);
	}

	@Override
	public boolean isEmpty(TreeFormatter formatter, DataRecord[] availableData) {
		return false;
	}
}
