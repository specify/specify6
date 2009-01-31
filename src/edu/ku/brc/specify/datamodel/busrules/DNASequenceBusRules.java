package edu.ku.brc.specify.datamodel.busrules;

import javax.swing.JTextField;

import edu.ku.brc.af.ui.IllustrativeBarCodeUI;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.specify.datamodel.DNASequence;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;

public class DNASequenceBusRules extends AttachmentOwnerBaseBusRules implements CommandListener
{
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
        
        /*if (formViewObj != null)
        {
            final JTextArea ta = (JTextArea)formViewObj.getCompById("4");
            ta.getDocument().addDocumentListener(new DocumentAdaptor() {
                @Override
                protected void changed(DocumentEvent e)
                {
                    if (isEditMode())
                    {
                        IllustrativeBarCodeUI  barCodeUI = (IllustrativeBarCodeUI)formViewObj.getControlById("8");
                        barCodeUI.setSequence(ta.getText());
                        adjustTotals();
                    }
                }
            });
        }*/
    }


    private void setValue(final String id, final int value)
    {
        JTextField  tf = (JTextField)formViewObj.getControlById(id);
        tf.setText(Integer.toString(value));
    }
    
    /**
     * 
     */
    private void adjustTotals()
    {
        if (formViewObj != null)
        {
            IllustrativeBarCodeUI  barCodeUI = (IllustrativeBarCodeUI)formViewObj.getControlById("8");
            if (barCodeUI != null)
            {
                setValue("residues", barCodeUI.getTotal('A')+barCodeUI.getTotal('G')+barCodeUI.getTotal('C')+barCodeUI.getTotal('T')+barCodeUI.getTotal('X'));
                setValue("compa", barCodeUI.getTotal('A'));
                setValue("compg", barCodeUI.getTotal('G'));
                setValue("compt", barCodeUI.getTotal('T'));
                setValue("compc", barCodeUI.getTotal('C'));
                setValue("ambiguous", barCodeUI.getTotal('X'));
            }
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
