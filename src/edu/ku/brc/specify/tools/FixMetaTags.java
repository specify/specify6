package edu.ku.brc.specify.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class FixMetaTags
{
    static public String getContents(File aFile) {
        //...checks on aFile are elided
        StringBuffer contents = new StringBuffer();

        //System.out.println(aFile.getName());
        //declared here only to make visible to finally clause
        BufferedReader input = null;
        try 
        {
            String eol = System.getProperty("line.separator");
            
            System.out.println("\n"+aFile.getName());
          //use buffering
          //this implementation reads one line at a time
          //FileReader always assumes default encoding is OK!
          input = new BufferedReader( new FileReader(aFile) );
          String line = null; //not declared within while loop
          //boolean doIt = false;
          int cnt = 0;
          boolean doComment = true;
          boolean doClassDesc = true;
          while (( line = input.readLine()) != null)
          {
              cnt++;
              boolean addLine = true;
              
              if (doComment)
              {
                  int inx = line.indexOf("<!--");
                  if (inx > -1)
                  {
                      do
                      {
                          line = input.readLine();
                          cnt++;
                      } while (line.indexOf("-->") == -1);
                      doComment = false;
                      continue;
                  }
              }
              
              if (doComment && line.indexOf("<class") > -1)
              {
                  doComment = false;
              }
              
              if (doClassDesc)
              {
                  int inx = line.indexOf("class-description");
                  if (inx > -1)
                  {
                      do
                      {
                          line = input.readLine();
                          cnt++;
                      } while (line.indexOf("</meta>") == -1);
                      contents.append("    <meta attribute=\"class-description\" inherit=\"false\"/>");
                      contents.append(eol);
                      doClassDesc = false;
                      continue;
                  }
              }
              
              int ccInx = line.indexOf("class-code");
              int inx = line.indexOf("<meta");
              if (inx > -1 && ccInx == -1)
              {
                  inx = line.indexOf("</meta>");
                  if (inx == -1)
                  {
                      String textLine = input.readLine();
                      cnt++;
                      String endTag = null;
                      if (textLine.indexOf("</meta>") == -1) 
                      {
                          endTag = input.readLine();
                          cnt++;
                      }
                      
                      if (endTag != null && endTag.indexOf("</meta>") == -1)
                      {
                          throw new RuntimeException("Assumption bad about end tab. line "+cnt);
                      }
                      contents.append(StringUtils.stripEnd(line, " "));
                      contents.append(StringUtils.strip(textLine));
                      if (endTag != null)
                      {
                          contents.append(StringUtils.strip(endTag));
                      }
                      contents.append(eol);

                      addLine = false;
                  }
              }
              
              if (addLine)
              {
                  contents.append(line);
                  contents.append(System.getProperty("line.separator"));
              }
          }
        }
        catch (FileNotFoundException ex) {
          ex.printStackTrace();
        }
        catch (IOException ex){
          ex.printStackTrace();
        }
        finally {
          try {
            if (input!= null) {
              //flush and close both "input" and its underlying FileReader
              input.close();
            }
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
        }
        return contents.toString();
      }

      /**
      * Change the contents of text file in its entirety, overwriting any
      * existing text.
      *
      * This style of implementation throws all exceptions to the caller.
      *
      * @param aFile is an existing file which can be written to.
      * @throws IllegalArgumentException if param does not comply.
      * @throws FileNotFoundException if the file does not exist.
      * @throws IOException if problem encountered during write.
      */
      static public void setContents(File aFile, String aContents)
                                     throws FileNotFoundException, IOException {
        if (aFile == null) {
          throw new IllegalArgumentException("File should not be null.");
        }
        if (!aFile.exists()) {
          throw new FileNotFoundException ("File does not exist: " + aFile);
        }
        if (!aFile.isFile()) {
          throw new IllegalArgumentException("Should not be a directory: " + aFile);
        }
        if (!aFile.canWrite()) {
          throw new IllegalArgumentException("File cannot be written: " + aFile);
        }

        //declared here only to make visible to finally clause; generic reference
        Writer output = null;
        try {
          //use buffering
          //FileWriter always assumes default encoding is OK!
          output = new BufferedWriter( new FileWriter(aFile) );
          output.write( aContents );
        }
        finally {
          //flush and close both "output" and its underlying FileWriter
          if (output != null) output.close();
        }
      }
      
    public FixMetaTags()
    {
        super();
        
        String path = "src/edu/ku/brc/specify/datamodel/hbm";
        
        File hbmDir = new File(path);
        String[] names = hbmDir.list();
        if (names != null)
        {
            List<String> fileNames = new ArrayList<String>();
            for (String s : names)
            {
                if (s.indexOf("hbm") > -1)
                {
                    fileNames.add(s);
                }
            }
            Collections.sort(fileNames);

            //names[0] = "Accession.hbm.xml";
            for (String name : fileNames)
            {
                File file = new File(path+"/"+name);
                String buffer = getContents(file);
                
                try
                {
                    setContents(file, buffer);
                    //System.out.println(buffer.toString());
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                //break;
            }
        } else
        {
            System.out.println("Dir was null");
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        @SuppressWarnings("unused")
        FixMetaTags tablesToLower = new FixMetaTags();

    }

}
