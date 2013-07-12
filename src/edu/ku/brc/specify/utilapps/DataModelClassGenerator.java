/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.utilapps;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.specify.datamodel.Appraisal;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 28, 2007
 *
 */
public class DataModelClassGenerator extends JFrame
{
    enum DataType {String, Memo, Set}
    
    private Hashtable<String, Boolean> baseClassHash = new Hashtable<String, Boolean>();
    
    protected Class<?> dataClass;
    
    protected JTextArea  logArea;
    protected JTextField tableIdTxt;
    protected JTextField devTxt;
    
    protected Vector<UIRow>           rows         = new Vector<UIRow>();
    protected Hashtable<Field, UIRow> fieldRowhash = new Hashtable<Field, UIRow>();
    
    /**
     * @param dataClass
     * @param tableId
     */
    public DataModelClassGenerator(final Class<?> dataClass)
    {
        this.dataClass = dataClass;
        
        Class<?>[] baseClasses = {Boolean.class, Integer.class, Double.class, String.class, Float.class,
                                  Character.class, Short.class, Byte.class, BigDecimal.class, Date.class, Calendar.class};
        for (Class<?> cls : baseClasses)
        {
            baseClassHash.put(cls.getSimpleName(), true);
        }
        
        int cnt = 0;
        for (Field field : dataClass.getDeclaredFields())
        {
            if (field.getType() == String.class || Collection.class.isAssignableFrom(field.getType()))
            {
                cnt++;
            }
        }
        Font font = new Font("Courier", Font.PLAIN, 10);

        logArea = new JTextArea();
        logArea.setFont(font);
        
        tableIdTxt = new JTextField(10);
        devTxt     = new JTextField(10);
        
        PanelBuilder    outer = new PanelBuilder(new FormLayout("p,2px,p,2px,f:p:g", "200px:g,4px,f:300px:g,4px,p"));
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,p,2px,l:p:g,2px,l:p", UIHelper.createDuplicateJGoodiesDef("p", "2px", cnt+2)));
        CellConstraints cc = new CellConstraints();
        
        pb.add(new JLabel("Table Id"), cc.xy(1, 1));
        pb.add(tableIdTxt, cc.xywh(3, 1, 3, 1));
        
        pb.add(new JLabel("Dev"), cc.xy(1, 3));
        pb.add(devTxt, cc.xywh(3, 3, 3, 1));

        int y = 5;
        for (Field field : dataClass.getDeclaredFields())
        {
            if (field.getType() == String.class)
            {
                UIRow row = new UIRow(DataType.String, field);
                pb.add(new JLabel(field.getName()), cc.xy(1, y));
                pb.add(row.getSizetxt(), cc.xywh(3, y, 1, 1));
                y += 2;
                rows.add(row);
                fieldRowhash.put(field, row);
            }
            
            if (Collection.class.isAssignableFrom(field.getType()))
            {
                UIRow row = new UIRow(DataType.Set, field);
                pb.add(new JLabel(field.getName()), cc.xy(1, y));
                pb.add(row.getSizetxt(), cc.xywh(3, y, 3, 1));
                pb.add(new JLabel("(Set)", SwingConstants.LEFT), cc.xy(7, y));
                y += 2;
                rows.add(row);
                fieldRowhash.put(field, row);
            }
        }

        JButton processBtn = UIHelper.createButton("Process");
        JButton cancelBtn  = UIHelper.createButton("Cancel");
        
        processBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                process();
            }
        });
        
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        });
        pb.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        JPanel panel = ButtonBarFactory.buildGrowingBar(new JButton[] {processBtn, cancelBtn});
        
        outer.add(new JScrollPane(pb.getPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), cc.xywh(1, 1, 5, 1));
        outer.add(new JScrollPane(logArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), cc.xywh(1, 3, 5, 1));
        outer.add(panel, cc.xy(5, 5));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        JPanel p = new JPanel(new BorderLayout());
        p.add(outer.getPanel(), BorderLayout.CENTER);
        setContentPane(p);
        //setSize(500,500);
        pack();
        setSize(500, getSize().height);
    }
    
    public void process()
    {

        String template = "";
        
        try
        {
            File templateFile = new File("src/edu/ku/brc/specify/utilapps/DataModelGenTemplate.txt");
            template = FileUtils.readFileToString(templateFile);
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataModelClassGenerator.class, ex);
            ex.printStackTrace();
            return;
        }
        
        String className      = dataClass.getSimpleName();
        String noCapClassName = className.substring(0,1).toLowerCase() + className.substring(1);
        String idName         = className.substring(0,1).toLowerCase() + className.substring(1) + "Id";
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        
        template = StringUtils.replace(template, "<!-- TableName -->", className.toLowerCase());
        template = StringUtils.replace(template, "<!-- ClassName -->", className);
        template = StringUtils.replace(template, "<!-- Id -->", idName);
        template = StringUtils.replace(template, "<!-- IdNum -->", tableIdTxt.getText());
        template = StringUtils.replace(template, "<!-- Dev -->", devTxt.getText());
        template = StringUtils.replace(template, "<!-- Date -->", sdf.format(Calendar.getInstance().getTime()));
        
        Hashtable<String, Boolean> duplicateClassNames = new Hashtable<String, Boolean>();
        for (Field field : dataClass.getDeclaredFields())
        {
            UIRow row = fieldRowhash.get(field);
            String  fieldsType = (row != null && row.getSizetxt() != null) ? row.getSizetxt().getText() : field.getType().getSimpleName();
            Boolean bool = duplicateClassNames.get(fieldsType);
            if (bool == null)
            {
                duplicateClassNames.put(fieldsType, false);
            } else if (!bool)
            {
                duplicateClassNames.put(fieldsType, true);
            }
        }
        StringBuilder declarationsSB = new StringBuilder();
        StringBuilder initersSB      = new StringBuilder();
        StringBuilder methodsSB      = new StringBuilder();
        
        StringBuilder osDeclarationsSB = new StringBuilder();
        StringBuilder osInitersSB      = new StringBuilder();
        StringBuilder osMethodsSB      = new StringBuilder();

        // Initializer
        int maxWidth = 0;
        for (Field field : dataClass.getDeclaredFields())
        {
            String typeStr;
            UIRow row = fieldRowhash.get(field);
            if (row != null && row.isSet())
            {
                typeStr = field.getType().getSimpleName() +"<" + row.getSizetxt().getText() + ">";
            } else
            {
                typeStr = field.getType().getSimpleName();
            }
            maxWidth = Math.max(maxWidth, typeStr.length());
        }
        
        int initerMaxWidth = 0;
        for (Field field : dataClass.getDeclaredFields())
        {
            initerMaxWidth = Math.max(initerMaxWidth, field.getName().length());
        }
        
        String prevType = "";

        for (Field field : dataClass.getDeclaredFields())
        {
            String  fieldName = field.getName();
            UIRow   row       = fieldRowhash.get(field);
            boolean isSet     = row != null && row.isSet();
            
            boolean isThereMoreThanOne  = false;
            String  othersideClassType  = "";
            String  othersideFieldName  = "";
            String  othersideMethodName = "";
            String  otherSideID         = "";
            boolean isFieldObj          = false;
            String  fieldsType;
            if (row != null)
            {
                fieldsType  = isSet ? row.getSizetxt().getText() : field.getType().getSimpleName();
            } else
            {
                fieldsType = field.getType().getSimpleName();
            }
            //System.out.println("isField["+fieldsType+"]["+isFieldObj + "] " + fieldName);
            
            //String checkClass = "";
            if (!isSet)
            {
                // Check to see if the field is a basic Java type we should ignore 
                // or a Data Object we need to hook up
                isFieldObj = baseClassHash.get(fieldsType) != null;// Check to see if it is a basic type
                if (!isFieldObj)
                {
                    othersideClassType = "Set<" + className + ">";
                    
                    isThereMoreThanOne = duplicateClassNames.get(fieldsType) != null && duplicateClassNames.get(fieldsType);
                    if (isThereMoreThanOne)
                    {                    // OK since it isn't a basic field than it is a Set of this class
                        String fName = fieldName.substring(0, fieldName.length()-1);
                        othersideFieldName  = fName + className + "s";
                        othersideMethodName = StringUtils.capitalize(fName) + className + "s";
                        otherSideID         = fName + className + "ID";
                    } else
                    {
                        othersideFieldName  = noCapClassName + "s";
                        othersideMethodName = className + "s";
                        otherSideID         = className + "ID";
                    }
                }

            } else
            {
                // Since this side is a Set than the otherside will be just this class
                othersideClassType = className;
                
                isThereMoreThanOne = duplicateClassNames.get(fieldsType) != null && duplicateClassNames.get(fieldsType);
                if (isThereMoreThanOne)
                {
                    String fName = fieldName.substring(0, fieldName.length()-1);
                    othersideFieldName  = fName + className;
                    othersideMethodName = StringUtils.capitalize(fName) + className;
                    otherSideID         = fName + className + "ID";
                } else
                {
                    othersideFieldName  = noCapClassName;
                    othersideMethodName = className;
                    otherSideID         = className + "ID";
                }
            }
            
            boolean isNewOSClassType = !fieldsType.equals(prevType);
            if (isNewOSClassType)
            {
                prevType = fieldsType;
            }
            
            boolean doOtherSide = isSet || !isFieldObj;

            //-------------------------------------
            // Declarations
            //-------------------------------------
            String typeStr;
            
            if (row != null && row.isSet())
            {
                typeStr = field.getType().getSimpleName() +"<" + row.getSizetxt().getText() + ">";
            } else
            {
                typeStr = field.getType().getSimpleName();
            }
            declarationsSB.append("    protected ");
            declarationsSB.append(typeStr);
            for (int i=0;i<(maxWidth - typeStr.length()+1);i++)
            {
                declarationsSB.append(' ');
            }
            declarationsSB.append(field.getName());
            declarationsSB.append(";\n");
            
            if (doOtherSide)
            {
                if (isNewOSClassType)
                {
                    osDeclarationsSB.append("\n\n    // Copy this Code into Class "+fieldsType+"\n\n");
                }
                // use the same width (just because)
                osDeclarationsSB.append("    protected ");
                osDeclarationsSB.append(othersideClassType);
                for (int i=0;i<(maxWidth - typeStr.length()+1);i++)
                {
                    osDeclarationsSB.append(' ');
                }
                osDeclarationsSB.append(othersideFieldName);
                osDeclarationsSB.append(";\n");
            }

            //-------------------------------------
            // Initers
            //-------------------------------------
            if (row != null && row.isSet())
            {
                typeStr = "new HashSet<" + row.getSizetxt().getText() + ">()";
            } else
            {
                typeStr = "null";
            }
            initersSB.append("        ");
            initersSB.append(field.getName());
            for (int i=0;i<(initerMaxWidth - field.getName().length()+1);i++)
            {
                initersSB.append(' ');
            }
            initersSB.append(" = ");
            initersSB.append(typeStr);
            initersSB.append(";\n");
            
            if (doOtherSide && !isFieldObj)
            {
                if (isNewOSClassType)
                {
                    osInitersSB.append("\n\n    // Copy this Code into Class "+fieldsType+"\n\n");
                }
                osInitersSB.append("        ");
                osInitersSB.append(othersideFieldName);
                for (int i=0;i<(initerMaxWidth - field.getName().length()+1);i++)
                {
                    osInitersSB.append(' ');
                }
                osInitersSB.append(" = ");
                if (othersideClassType.startsWith("Set"))
                {
                    osInitersSB.append("new HashSet<" + (row != null ? row.getSizetxt().getText() : className) + ">()");
                } else
                {
                    osInitersSB.append("null");
                }

                osInitersSB.append(";\n");
            }

            // Do Methods

            if (isSet)
            {
                typeStr = field.getType().getSimpleName() +"<" + row.getSizetxt().getText() + ">";
                isSet = true;
                
            } else
            {
                typeStr = field.getType().getSimpleName();
            }
            
            methodsSB.append("    /**\n");
            methodsSB.append("     *\n");
            methodsSB.append("     */\n");
            
            String size = "";
            if (row != null)
            {
                if (row.isCBXString())
                {
                    size = row.getSizetxt().getText();
                } else if (row.getType() != DataType.Set)
                {
                    methodsSB.append("    @Lob\n");
                }
            } else if (field.getType() == Date.class || field.getType() == Calendar.class)
            {
                methodsSB.append("    @Temporal(TemporalType.DATE)\n");
            }
            
            if (isSet)
            {
                String singularFieldName = field.getName().substring(0, fieldName.length()-1);
                methodsSB.append("    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = \""+singularFieldName+"\")\n");

            } else if (baseClassHash.get(field.getType()) != null)
            {
                methodsSB.append("    @Column(name = \""+StringUtils.capitalize(fieldName)+"\", unique = false, nullable = true, insertable = true, updatable = true" + 
                          (StringUtils.isNotEmpty(size) ? (", length = "+size+"") : "") + ")\n");
            } else
            {
                methodsSB.append("    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)\n");
                methodsSB.append("    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })\n");
                methodsSB.append("    @JoinColumn(name = \""+className+"ID\", unique = false, nullable = true, insertable = true, updatable = true)\n");
            }
            methodsSB.append("    public "+typeStr+" get"+StringUtils.capitalize(fieldName)+"()\n    {\n");
            methodsSB.append("        return this."+fieldName+";\n");
            methodsSB.append("    }\n\n");
            methodsSB.append("    public void set"+StringUtils.capitalize(fieldName)+"(final "+typeStr+" "+fieldName+")\n    {\n");
            methodsSB.append("        this."+fieldName+" = "+fieldName+";\n");
            methodsSB.append("    }\n\n");
            
            if (fieldsType.startsWith("Agent"))
            {
                int x = 0;
                x++;
            }
            if (doOtherSide && !isFieldObj)
            {
                if (isNewOSClassType)
                {
                    osMethodsSB.append("\n\n    // Copy this Code into Class "+fieldsType+"\n\n");
                }
                
                osMethodsSB.append("    /**\n");
                osMethodsSB.append("     *\n");
                osMethodsSB.append("     */\n");
                if (isSet)
                {
                    osMethodsSB.append("    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = \""+fieldName+"\")\n");

                } else
                {
                    osMethodsSB.append("    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)\n");
                    osMethodsSB.append("    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })\n");
                    osMethodsSB.append("    @JoinColumn(name = \""+otherSideID+"\", unique = false, nullable = true, insertable = true, updatable = true)\n");
                }
                
                osMethodsSB.append("    public "+othersideClassType+" get"+othersideMethodName+"()\n    {\n");
                osMethodsSB.append("        return this."+othersideFieldName+";\n");
                osMethodsSB.append("    }\n\n");
                osMethodsSB.append("    public void set"+othersideMethodName+"(final "+othersideClassType+" "+othersideFieldName+")\n    {\n");
                osMethodsSB.append("        this."+othersideFieldName+" = "+othersideFieldName+";\n");
                osMethodsSB.append("    }\n\n");                    
    
            }
        }
        template = StringUtils.replace(template, "<!-- Fields -->",  declarationsSB.toString());
        template = StringUtils.replace(template, "<!-- Initers -->", initersSB.toString());
        template = StringUtils.replace(template, "<!-- Methods -->", methodsSB.toString());

        StringBuilder sb = new StringBuilder();
        sb.append("/* ---------------------------------------------\n");
        sb.append(osDeclarationsSB);
        sb.append(osInitersSB);
        sb.append(osMethodsSB);
        sb.append("\n*/");

        logArea.setText(template + sb.toString());
        
        try
        {
            File outFile = new File(className+".java");
            FileUtils.writeStringToFile(outFile, template + sb.toString());
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataModelClassGenerator.class, ex);
            ex.printStackTrace();
        }
  
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("synthetic-access")
          public void run()
            {
                try
                {
                    UIHelper.OSTYPE osType = UIHelper.getOSType();
                    if (osType == UIHelper.OSTYPE.Windows )
                    {
                        //UIManager.setLookAndFeel(new WindowsLookAndFeel());
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        
                    } else if (osType == UIHelper.OSTYPE.Linux )
                    {
                        //UIManager.setLookAndFeel(new GTKLookAndFeel());
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        //PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
                        //PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
                        //PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        //PlasticLookAndFeel.setPlasticTheme(new DesertGreen());
                       
                    }
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataModelClassGenerator.class, e);
                    //log.error("Can't change L&F: ", e);
                }
                DataModelClassGenerator dmcg = new DataModelClassGenerator(Appraisal.class);
                UIHelper.centerAndShow(dmcg);

            }
        });
    }
    
    class UIRow 
    {
        protected Field      field;
        protected JTextField sizetxt;
        protected JComboBox  typeCBX  = null;
        protected DataType   type;
        
        public UIRow(final DataType type, final Field field)
        {
            this.type = type;
            
            /*String[] options = new String[DataType.values().length];
            int i = 0;
            for (DataType dt : DataType.values())
            {
                options[i++] = dt.toString();
            }*/
            this.field    = field;
            sizetxt       = new JTextField(type == DataType.String ? 5 : 15);
            if (type != DataType.Set)
            {
                typeCBX = UIHelper.createComboBox(new String[] {"Memo", "String"});
                typeCBX.setSelectedIndex(0);
            }
        }
        /**
         * @return the sizetxt
         */
        public JTextField getSizetxt()
        {
            return sizetxt;
        }

        /**
         * @return the typeCBX
         */
        public JComboBox getTypeCBX()
        {
            return typeCBX;
        }
        /**
         * @return the field
         */
        public Field getField()
        {
            return field;
        }
        /**
         * @param field the field to set
         */
        public void setField(Field field)
        {
            this.field = field;
        }
        
        public boolean isSet()
        {
            return this.type == DataType.Set;
        }
        
        public boolean isCBXString()
        {
            return sizetxt.getText() != null;
            /*
            if (typeCBX != null)
            {
                String value = (String)typeCBX.getSelectedItem();
                DataType dt = value != null ? DataType.valueOf(value) : DataType.Memo;
                return dt == DataType.String;
            }
            return false;*/
        }
        /**
         * @return the type
         */
        public DataType getType()
        {
            return type;
        }
        
    }

}
