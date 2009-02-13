package edu.ku.brc.specify.datamodel.busrules;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.IllustrativeBarCodeUI;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DNASequence;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.DocumentAdaptor;

public class DNASequenceBusRules extends AttachmentOwnerBaseBusRules implements CommandListener
{
    protected IllustrativeBarCodeUI  barCodeUI = null;
    
    /**
     * 
     */
    public DNASequenceBusRules()
    {
        super(DNASequence.class);
        
        CommandDispatcher.register("Data_Entry", this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            barCodeUI = (IllustrativeBarCodeUI)formViewObj.getControlById("8");
            if (barCodeUI == null)
            {
                barCodeUI = new IllustrativeBarCodeUI();
            }
            
            if (barCodeUI != null)
            {
                final JTextArea ta = (JTextArea)formViewObj.getCompById("4");
                ta.getDocument().addDocumentListener(new DocumentAdaptor() {
                    @Override
                    protected void changed(DocumentEvent e)
                    {
                        if (isEditMode())
                        {
                            barCodeUI.setSequence(ta.getText());
                            adjustTotals();
                        }
                    }
                });
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeMerge(java.lang.Object,
     *      edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    /*@Override
    public void beforeMerge(final Object dataObj, 
                            final DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        DNASequence dnaSeq = (DNASequence)dataObj;
        if (dnaSeq != null && AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
        {
            //CollectingEvent ce = colObj.getCollectingEvent();
            //if (ce != null)
            {
                try
                {
                    session.saveOrUpdate(dnaSeq);
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectionObjectBusRules.class, ex);
                    ex.printStackTrace();
                }
            }
        }
    }*/
    
    /**
     * @param id
     * @param value
     */
    private void setValue(final String id, final int value)
    {
        JTextField  tf = (JTextField)formViewObj.getControlById(id);
        if (tf != null)
        {
            tf.setText(Integer.toString(value));    
        } else
        {
            log.debug("Couldn't find id["+id+"] " + value);
        }
        
    }
    
    /**
     * 
     */
    private void adjustTotals()
    {
        if (formViewObj != null&& barCodeUI != null)
        {
            setValue("residues", barCodeUI.getTotal('A')+barCodeUI.getTotal('G')+barCodeUI.getTotal('C')+barCodeUI.getTotal('T')+barCodeUI.getTotal('X'));
            setValue("compA", barCodeUI.getTotal('A'));
            setValue("compG", barCodeUI.getTotal('G'));
            setValue("compT", barCodeUI.getTotal('T'));
            setValue("compC", barCodeUI.getTotal('C'));
            setValue("ambiguous", barCodeUI.getTotal('X'));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (formViewObj != null && dataObj != null)
        {
            adjustTotals();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        CommandDispatcher.unregister("Data_Entry", this);
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        /* This was added as part of the BOLD demo
        if (cmdAction.isAction("CLOSE_SUBVIEW") && formViewObj != null)
        {
            Pair<Object, Object> data = (Pair<Object, Object>)cmdAction.getData();
            if (data.first instanceof DNASequence)
            {
                for (Object obj : (TreeSet<?>)data.second)
                {
                    DNASequenceAttachment dnaAttachment = (DNASequenceAttachment)obj;
                    if (dnaAttachment != null && dnaAttachment.getAttachment() != null && dnaAttachment.getAttachment().getAttachmentId() == null)
                    {
                        Random rand = new Random(12345678L);
                        char[] syms = {'A', 'C', 'T', 'G', };
                        StringBuilder sb = new StringBuilder();
                        for (int j=0;j<((8*75)+52);j++)
                        {
                            sb.append(syms[rand.nextInt(syms.length)]);
                        }
                        JTextArea ta = (JTextArea)formViewObj.getCompById("4");
                        ta.setText(sb.toString());
                        
                        ValFormattedTextFieldSingle seqDateTF = (ValFormattedTextFieldSingle)formViewObj.getCompById("3");
                        
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(2008, rand.nextInt(12)+1, rand.nextInt(28)+1, 06, 12, 00);
                        seqDateTF.setValue(calendar, null);
                    }
                }
            }
        }*/
    }

}
