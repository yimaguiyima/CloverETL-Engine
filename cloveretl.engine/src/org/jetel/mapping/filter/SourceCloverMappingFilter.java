package org.jetel.mapping.filter;

import org.jetel.mapping.MappingSource;
import org.jetel.mapping.element.CloverMappingElement;
import org.jetel.mapping.filter.MappingFilterAdapter;


/**
 * Mapping filter. Only mapping assignment with template 'clover.field --> initiate.field' 
 * with the given port name remains.
 * 
 * @author Jan Ausperger (jan.ausperger@javlinconsulting.cz)
 *         (c) Javlin, a.s. (www.javlin.eu)
 *
 * @created 14.8.2007
 */
public class SourceCloverMappingFilter extends MappingFilterAdapter {

	/* (non-Javadoc)
	 * @see com.initiatesystems.etl.formatter.mapping.MappingFilterAdapter#checkSource(com.initiatesystems.etl.formatter.mapping.MappingSource)
	 */
	@Override
	public boolean checkSource(MappingSource source) {
		return source instanceof CloverMappingElement;
	}
}