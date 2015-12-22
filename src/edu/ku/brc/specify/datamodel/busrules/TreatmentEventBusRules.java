/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.TreatmentEvent;

/**
 * @author timo
 *
 */
public class TreatmentEventBusRules extends AttachmentOwnerBaseBusRules {

    /**
     * Constructor.
     */
    public TreatmentEventBusRules()
    {
        super(TreatmentEvent.class);
    }

}
