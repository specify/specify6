<?xml version="1.0" ?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias">
  
  <xsl:namespace-alias stylesheet-prefix="axsl" result-prefix="xsl"/>
  
  <xsl:output indent="yes"/>

  <xsl:variable name="empty_string"/>
  <xsl:variable name="memo_text">text</xsl:variable>
  
<xsl:template name="substring-before-last">
  <xsl:param name="input" />
  <xsl:param name="substr" />
  <xsl:if test="$substr and contains($input, $substr)">
    <xsl:variable name="temp" select="substring-after($input, $substr)" />
    <xsl:value-of select="substring-before($input, $substr)" />
    <xsl:if test="contains($temp, $substr)">
      <xsl:value-of select="$substr" />
      <xsl:call-template name="substring-before-last">
        <xsl:with-param name="input" select="$temp" />
        <xsl:with-param name="substr" select="$substr" />
      </xsl:call-template>
    </xsl:if>
  </xsl:if>
</xsl:template>
   
<xsl:template name="substring-after-last">
<xsl:param name="input"/>
<xsl:param name="substr"/>
   
<!-- Extract the string which comes after the first occurence -->
<xsl:variable name="temp" select="substring-after($input,$substr)"/>
   
<xsl:choose>
     <!-- If it still contains the search string the recursively process -->
     <xsl:when test="$substr and contains($temp,$substr)">
          <xsl:call-template name="substring-after-last">
               <xsl:with-param name="input" select="$temp"/>
               <xsl:with-param name="substr" select="$substr"/>
          </xsl:call-template>
     </xsl:when>
     <xsl:otherwise>
          <xsl:value-of select="$temp"/>
     </xsl:otherwise>
</xsl:choose>
</xsl:template>
  

  <xsl:template match="/">
     <html><head>
     <title>Specify 6 Schema</title>
     <style>
     body        { font-family: sans-serif; font-size: 11pt;}
     table       { border-bottom: 1px solid black; border-right: 1px solid black; font-size: 11pt;}
     table.tbl   { background-color: rgb(250,250,255); }
     td          { border-left: 1px solid black; border-top: 1px solid black; }
     th          { border-left: 1px solid black; border-top: 1px solid black; }
     td.hd       { text-align:center; font-weight: bold; }
     td.hdbig    { text-align:center; font-weight: bold; font-size: 12pt;}
     td.subhead  { padding-top: 4px; background-color: white; text-align:center; }
     span.footer { font-size: 9pt; font-style: italic;}
     </style>
     </head><body>
 
<xsl:template match="/">

  <H1>Specify 6 Database Schema</H1>

  <H3>Table of Contents</H3>
  <UL>
  <xsl:for-each select="//table">
  
    <LI><a href="#{@table}">
<xsl:call-template name="substring-after-last">
 <xsl:with-param name="input" select="@classname"/>
 <xsl:with-param name="substr">.</xsl:with-param>
</xsl:call-template>
</a></LI>
  </xsl:for-each>
  </UL>
  
  <H3>Table Definitions</H3>
     <xsl:apply-templates select="database/table">
       <xsl:sort select="@table" />
     </xsl:apply-templates>
     
</xsl:template>
<br/>
<span class="footer">Created: 2007-08-23</span>
     </body></html>
  </xsl:template>


<xsl:template match="table">
<a name="{@table}"></a>
  <table class="tbl" border="0" cellspacing="0" cellpadding="2" width="50%">

       <tr><td colspan="4" class="hdbig">
<xsl:call-template name="substring-after-last">
     <xsl:with-param name="input" select="@classname"/>
     <xsl:with-param name="substr">.</xsl:with-param>
</xsl:call-template>
       </td></tr>
       
        <tr>
        <td class="hd">Field</td>
        <td class="hd">Type</td>
        <td class="hd">Length</td>
        <td class="hd">Index Name</td>
        </tr>
        
        <xsl:apply-templates select="id"/>
        
       <xsl:apply-templates select="field">
         <xsl:sort select="@column" />
       </xsl:apply-templates>
       
       <tr><td colspan="4" class="subhead">Relationships</td></tr>
       <tr>
        <td class="hd" colspan="2">Type</td>
        <td class="hd" colspan="2">To Table</td>
        </tr>
       
       <xsl:apply-templates select="relationship">
         <xsl:sort select="@relationshipname" />
       </xsl:apply-templates>

    </table><br/><br/>

  </xsl:template>

  <xsl:template match="field">
        <tr>
        <td align="center"><xsl:value-of select="@column"/></td>
        <td align="center">
          <xsl:choose>
	         <xsl:when test="@type = $memo_text">
	            <xsl:text>Memo</xsl:text>
	         </xsl:when>  
          <xsl:otherwise>
            <xsl:call-template name="substring-after-last">
			<xsl:with-param name="input" select="@type"/>
			 <xsl:with-param name="substr">.</xsl:with-param>
			</xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
        </td>
        <xsl:choose>
          <xsl:when test="@length != $empty_string">
             <td align="center"><xsl:value-of select="@length"/></td>
          </xsl:when>  
          <xsl:otherwise><td align="center"><xsl:text>&#160;</xsl:text></td></xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="@indexName != $empty_string">
             <td align="center"><xsl:value-of select="@indexName"/></td>
          </xsl:when>  
          <xsl:otherwise><td align="center"><xsl:text>&#160;</xsl:text></td></xsl:otherwise>
        </xsl:choose>
        </tr>

  </xsl:template>
  
  <xsl:template match="id">
        <tr>
        <td align="center"><xsl:value-of select="@column"/></td>
        <td align="center">
            <xsl:call-template name="substring-after-last">
			<xsl:with-param name="input" select="@type"/>
			 <xsl:with-param name="substr">.</xsl:with-param>
			</xsl:call-template>

        </td>
        <xsl:choose>
          <xsl:when test="@length != $empty_string">
             <td align="center"><xsl:value-of select="@length"/></td>
          </xsl:when>  
          <xsl:otherwise><td align="center"><xsl:text>&#160;</xsl:text></td></xsl:otherwise>
        </xsl:choose>
        <td align="center"><xsl:value-of select="@column"/></td>
        </tr>

  </xsl:template>
  
  <xsl:template match="relationship">
        <tr>
        <td align="center" colspan="2"><xsl:value-of select="@type"/></td>
        <td align="center" colspan="2">
            <xsl:call-template name="substring-after-last">
			 <xsl:with-param name="input" select="@classname"/>
			 <xsl:with-param name="substr">.</xsl:with-param>
			</xsl:call-template>
        </td>
        </tr>
  </xsl:template>
  
</xsl:stylesheet>
