<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
     <html><head>
     <title>Form Controls</title>
     <style>
     td.control { font-size: 14pt; font-weight: bold; }
     td { border-left: 1px solid black; border-top: 1px solid black; }
     table { border-bottom: 1px solid black; border-right: 1px solid black; }
     table.subcontrol { background-color: rgb(240,240,240); }
     table.paramTable { background-color: rgb(240,240,240); }
     </style>
     </head><body>


     <xsl:apply-templates select="uicontrols/control">
       <xsl:sort select="@type" />
     </xsl:apply-templates>

     </body></html>
  </xsl:template>

  <xsl:template match="control">
<table border="0" cellspacing="0" width="100%">
      <tr><td colspan="3" class="control"><xsl:value-of select="@type" /></td></tr>
    
       <tr><td colspan="3">
       <xsl:value-of select="desc"/>
       </td></tr>
       
       <tr><td align="center" width="33%"><b>Name</b></td><td align="center" width="34%"><b>Type</b></td><td align="center" width="33%"><b>Is Required</b></td></tr>
       
       <xsl:apply-templates select="attr"/>
       
       <xsl:if test="count( subcontrols/subcontrol ) > 0">
       <tr><td colspan="3"><br/>
       <xsl:apply-templates select="subcontrols/subcontrol">
       	<xsl:sort select="@type"/>
       </xsl:apply-templates>
       </td></tr>
       </xsl:if>
    </table><br/><br/>

  </xsl:template>

  <xsl:template match="desc">
  <!--  <tr><td colspan="2"><xsl:value-of select="desc"/></td></tr> -->
  </xsl:template>

  <xsl:template match="attr">

<!--
        <xsl:text disable-output-escaping="yes">
            &lt;li&gt;
        </xsl:text>
  -->
        <tr>
        <td align="center"><xsl:value-of select="@name"/></td>
        <td align="center"><xsl:value-of select="@type"/></td>
        <td align="center"><xsl:value-of select="@required"/></td>
        </tr>

  </xsl:template>
  
  <xsl:variable name="empty_string"/>
  
  <xsl:template match="param">
            <tr>
	          <td align="center"><xsl:value-of select="@name"/></td>
	          <td align="center"><xsl:value-of select="@type"/></td>
	          <td align="center"><xsl:value-of select="@required"/></td>
              
	          <xsl:choose>
                <xsl:when test="@default != $empty_string">
                   <td align="center"><xsl:value-of select="@default"/></td>
                </xsl:when>  
                <xsl:otherwise><td align="center"><xsl:text>&#160;</xsl:text></td></xsl:otherwise>
              </xsl:choose>
              
 	          <td align="left" width="50%"><xsl:value-of select="desc"/></td>
              
            </tr>
  </xsl:template>
  
  <xsl:template match="subcontrol">
    <table class="subcontrol" border="0" cellspacing="0" width="100%">
       <tr>
       	<td colspan="3" class="control"><xsl:value-of select="@type" /> - <xsl:value-of select="@dsp" /></td>
       </tr>
    
       <tr><td colspan="3">
       <xsl:value-of select="desc"/>
       </td></tr>
       
       <xsl:if test="(count( attr ) > 0) or (count( param ) > 0)">
       </xsl:if>
       
       <xsl:if test="count( attr ) > 0">
	       <tr>
	       	   <td align="center" width="33%"><b>Name</b></td><td align="center" width="34%"><b>Type</b></td><td align="center" width="33%"><b>Is Required</b></td>
	       </tr>
	       <xsl:apply-templates select="attr"/>
       </xsl:if>
       
       <xsl:if test="count( param ) > 0">
       <tr>
       <td colspan="3"><br/><b>Parameters</b></td>
       </tr>
       <tr>
       <td colspan="3">
         <table class="paramTable" border="0" cellspacing="0" width="100%">
           <tr>
               <td align="center"><b>Name</b></td><td align="center"><b>Type</b></td>
               <td align="center"><b>Is Required</b></td><td align="center"><b>Default</b></td><td align="center"><b>Description</b></td>
           </tr>
       <xsl:apply-templates select="param"/>

         </table>
         </td>
         </tr>
       </xsl:if>
       
    </table><br/>

  </xsl:template>
  

</xsl:stylesheet>
