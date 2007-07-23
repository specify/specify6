/* This library is free software; you can redistribute it and/or
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.ui.forms.validation;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatter;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 15, 2007
 *
 */
public class FormattedDateValidator implements FormattedTextValidatorIFace
{
    protected int[]       daysInMonth   = {0,31,28,31,30,31,30,31,31,30,31,30,31};
    protected String      reason        = "";
    protected DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
    protected char        separator     = '/';
    protected boolean     isValid       = true;
    
    public FormattedDateValidator()
    {
        separator = discoverSeparator();
    }
    
    protected char discoverSeparator()
    {
        // Here we need to discover what the Date format's separator character is
        // I could figure out a call in java to get it.
        String formatStr = scrDateFormat.getSimpleDateFormat().toPattern();
        int i = 0;
        while (i < formatStr.length())
        {
            if (!Character.isLetter(formatStr.charAt(i)))
            {
                return formatStr.charAt(i); 
            }
            i++;    
        }
        return Character.valueOf('/');
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.FormattedTextValidatorIFace#getReason()
     */
    public String getReason()
    {
        return reason;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.FormattedTextValidatorIFace#isValid(edu.ku.brc.ui.forms.formatters.UIFieldFormatter, java.lang.String)
     */
    public boolean isValid()
    {
        return isValid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.FormattedTextValidatorIFace#isValid(edu.ku.brc.ui.forms.formatters.UIFieldFormatter, java.lang.String)
     */
    public boolean validate(final UIFieldFormatterIFace formatter, final String value)
    {
        reason = "";
        //Calendar cal = Calendar.getInstance();
        //System.out.println(formatter.getDateWrapper().getSimpleDateFormat().toPattern());
            
        int len = formatter.getLength();
        if (StringUtils.isNotEmpty(value))
        {
            if (formatter.isDate())
            {
                /*if (len == value.length())
                {
                    try
                    {
                        cal.setTime(scrDateFormat.getSimpleDateFormat().parse(value));
                        return checkDate(cal.get(Calendar.MONTH)-1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR));
                        
                    } catch (ParseException ex)
                    {
                        reason = "Invalid Date";
                        return false;
                    }
                    

                } if (value.length() > len)
                {
                    reason = "Invalid Date";
                    return false;
                    
                } else*/
                {
                    List<UIFieldFormatterField> fields = formatter.getFields();
                    int pos = 0;
                    int mon   = -1;
                    int day   = -1;
                    int year  = -1;
                    for (UIFieldFormatterField field : fields)
                    {
                        if (pos >= len || pos+field.getSize() > value.length())
                        {
                            reason = "Partial";
                            break;
                        }
                        
                        if (field.getType() == UIFieldFormatterField.FieldType.numeric)
                        {
                            String part = value.substring(pos, pos+field.getSize());
                            if (StringUtils.isNumeric(part))
                            {
                                char c      = field.getValue().charAt(0);
                                int  numPart = Integer.parseInt(part); 
                                if (c == 'M' || c == 'm')
                                {
                                    mon = numPart;
                                    
                                } else if (c == 'd' || c == 'D')
                                {
                                    day = numPart;
                                   
                                } else if (c == 'Y' || c == 'y')
                                {
                                    year = numPart;
                                }
                                
                            } else if (part.indexOf(separator) > -1)
                            {
                                reason = "Wrong format";
                                return isValid = false;
                            } else
                            {
                                reason = getFieldSpecificError(field, " is Not numeric");
                                return isValid = false;
                            }
                        }
                        pos += field.getSize();
                    } // for loop
                    
                    //Calendar now = Calendar.getInstance();
                    if (mon != -1 && day != -1 && year != -1)
                    {

                        daysInMonth[1] = year % 4 == 0 || (year % 100 == 0 && year % 400 != 0) ? 29 : 28;

                        if (mon < 1 || mon > 12)
                        {
                            reason = "Invalid Month";
                            return isValid = false;
                        }
                        // Do Non-Leap Year
                        if (day < 1 || day > daysInMonth[mon-1])
                        {
                            reason = "Invalid Day";
                            return isValid = false;
                        }
                        return isValid = true;
                        
                    } else if (mon != -1 && day != -1)
                    {
                        daysInMonth[1] = 29;
                        if (mon < 1 || mon > 12)
                        {
                            reason = "Invalid Month";
                            return isValid = false;
                        }
                        // Do Non-Leap Year
                        if (day < 1 || day > daysInMonth[mon])
                        {
                            reason = "Invalid Day";
                            return isValid = false;
                        }
                        return isValid = true;
                        
                    } else if (mon != -1)
                    {
                        if (mon < 1 || mon > 12)
                        {
                            reason = "Invalid Month";
                            return isValid = false;
                        }
                    } else if (day> -1)
                    {
                        if (day < 1 || day > 31)
                        {
                            reason = "Invalid Day";
                            return isValid = false;
                        }  
                    }
                }
            }
            
            return isValid = true;
        }
        reason = "Date is Empty";
        
        return isValid = false;
    }
    
    protected String getFieldSpecificError(final UIFieldFormatterField field, final String msg)
    {
        char c = field.getValue().charAt(0);
        if (c == 'M' || c == 'm')
        {
            return "Month" + msg;
            
        } else if (c == 'd' || c == 'D')
        {
            return "Day" + msg;
            
        } else if (c == 'Y' || c == 'y')
        {
            return "Year" + msg;
            
        }
        return "";
    }

    protected static List<UIFieldFormatter> getDateFormatters()
    {
        //DateWrapper dw = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
        
        Vector<UIFieldFormatter> formatters = new Vector<UIFieldFormatter>();
        
        Vector<UIFieldFormatterField> fields = new Vector<UIFieldFormatterField>();
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric,   2, "MM", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.separator, 1, "/", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric,   2, "dd", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.separator, 1, "/", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric,   4, "YYYY", false));
        UIFieldFormatter uif = new UIFieldFormatter("Date", true, UIFieldFormatter.PartialDateEnum.Full, Date.class, true, false, fields);
        //uif.setDateWrapper(dw);
        formatters.add(uif);
        
        fields = new Vector<UIFieldFormatterField>();
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric,   2, "dd", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.separator, 1, "/", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric,   2, "MM", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.separator, 1, "/", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric,   4, "YYYY", false));
        uif = new UIFieldFormatter("Date", true, UIFieldFormatter.PartialDateEnum.Full, Date.class, true, false, fields);
        //uif.setDateWrapper(dw);
        formatters.add(uif);

        fields = new Vector<UIFieldFormatterField>();
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric,   4, "YYYY", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.separator, 1, "/", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric,   2, "MM", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.separator, 1, "/", false));
        fields.add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric,   2, "dd", false));
        uif = new UIFieldFormatter("Date", true, UIFieldFormatter.PartialDateEnum.Full, Date.class, true, false, fields);
        //uif.setDateWrapper(dw);
        formatters.add(uif);

        return formatters;
    }
    /**
    *
    */
   public static void main(String[] args)
   {
       
       FormattedDateValidator formattedDateValidator = new FormattedDateValidator();
       
       String[] dates = {"02/29/2008", "02/29/2007", "01/01/07", "32/01/07", "01/32/07", "01/32", "32/01", "32", "32", "32"};

       for (String dateStr : dates)
       {
           for (UIFieldFormatterIFace formatter : getDateFormatters())
           {
               boolean isValid = formattedDateValidator.validate(formatter, dateStr);

               System.out.println("For ["+formatter.toPattern()+"] ["+dateStr+"]  "+isValid+"   "+formattedDateValidator.getReason());

           }
           System.out.println(" ");
       }
   }

}
