/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import java.util.List;

import edu.ku.brc.specify.tasks.subpane.qb.ERTICaptionInfoQB;
import edu.ku.brc.specify.tasks.subpane.qb.HQLSpecs;

/**
 * @author timo
 *
 */
public class Specs {
	protected final HQLSpecs specs;
	protected final List<ERTICaptionInfoQB> cols;
	protected final String uniquenessHQL;
	protected final HQLSpecs uniquenessSpecs;

	public Specs(HQLSpecs specs, List<ERTICaptionInfoQB> cols,
			String uniquenessHQL, HQLSpecs uniquenessSpecs) {
		this.specs = specs;
		this.cols = cols;
		this.uniquenessHQL = uniquenessHQL;
		this.uniquenessSpecs = uniquenessSpecs;
	}

	public HQLSpecs getSpecs() {
		return specs;
	}

	public List<ERTICaptionInfoQB> getCols() {
		return cols;
	}

	public String getUniquenessHQL() {
		return uniquenessHQL;
	}

	public HQLSpecs getUniquenessSpecs() {
		return uniquenessSpecs == null ? specs : uniquenessSpecs;
	}
}
