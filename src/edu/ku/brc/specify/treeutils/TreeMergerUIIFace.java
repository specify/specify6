/**
 * 
 */
package edu.ku.brc.specify.treeutils;


import java.util.List;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * @author tnoble
 * 
 * First shot at providing ui for tree merger process
 * 
 * Using ids instead of Hibernate objects for now. Implementors will
 * have to look up objects. 
 *
 */
public interface TreeMergerUIIFace<N extends Treeable<N,D,I>,
	D extends TreeDefIface<N,D,I>,
	I extends TreeDefItemIface<N,D,I>>
{
	/**
	 * @param toMergeId the node being merged
	 * @param mergeIntoId the node merged into
	 * 
	 * called when the merge begins
	 */
	public void merging(Integer toMergeId, Integer mergeIntoId);
	
	
	/**
	 * @param movedId the node moved
	 * @param oldParentId the former parent
	 * @param newParentId the new parent
	 */
	public void moved(Integer movedId, Integer oldParentId, Integer newParentId);
	
	
	/**
	 * @param toMergeId the node merged
	 * @param mergeIntoId the node merged into
	 * 
	 * called after merge is complete
	 */
	public void merged(Integer toMergeId, Integer mergeIntoId);
	
	
	/**
	 * @param choices list of nodes to choose from
	 * @param mustChoose indicates whether a choice must made
	 * @return item in choices. null indicates no choice.
	 */
	public Integer choose(List<Integer> choices, boolean mustChoose);
}
