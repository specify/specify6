<?xml version="1.0" ?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias">

	<xsl:namespace-alias stylesheet-prefix="axsl" result-prefix="xsl" />

	<xsl:output indent="yes" />

	<xsl:variable name="empty_string" />
	<xsl:variable name="memo_text">text</xsl:variable>
	<xsl:variable name="bodyTextSize">10pt</xsl:variable>

	<xsl:template name="substring-before-last">
		<xsl:param name="input" />
		<xsl:param name="substr" />
		<xsl:if test="$substr and contains($input, $substr)">
			<xsl:variable name="temp"
				select="substring-after($input, $substr)" />
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
		<xsl:param name="input" />
		<xsl:param name="substr" />

		<!-- Extract the string which comes after the first occurence -->
		<xsl:variable name="temp"
			select="substring-after($input,$substr)" />
		<xsl:choose>
			<!-- If it still contains the search string the recursively process -->
			<xsl:when test="$substr and contains($temp,$substr)">
				<xsl:call-template name="substring-after-last">
					<xsl:with-param name="input" select="$temp" />
					<xsl:with-param name="substr" select="$substr" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$temp" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template match="/">
		<html>
			<head>
				<link rel="stylesheet" href="schema.css" type="text/css" />
				<title>Specify 6 Schema</title>
			</head>
			<body>
				<table class="banner" width="100%" border="0"
					cellspacing="0" cellpadding="0">
					<tr bgcolor="#D5EDB3">
						<td width="382" colspan="2" rowspan="2">
							<img src="schema/schema_banner.png"
								alt="Header image" width="382" height="101" border="0" />
						</td>
						<td width="378" height="50" id="logo"
							valign="bottom" align="center" nowrap="nowrap">
							Specify 6
						</td>
						<td width="100%">
							<xsl:text>&#160;</xsl:text>
						</td>
					</tr>

					<tr bgcolor="#D5EDB3">
						<td height="51" id="tagline" valign="top"
							align="center">
							Schema
						</td>
						<td width="100%">
							<xsl:text>&#160;</xsl:text>
						</td>
					</tr>

					<tr>
						<td colspan="4" bgcolor="#5C743D">
							<img src="schema/sp6_spacer.gif" alt=""
								width="1" height="2" border="0" />
						</td>
					</tr>

					<tr>
						<td colspan="4" bgcolor="#99CC66"
							background="sp6_dashed_line.gif">
							<img src="schema/sp6_dashed_line.gif"
								alt="line decor" width="4" height="3" border="0" />
						</td>
					</tr>
				</table>
				<table class="banner" width="100%" border="0"
					cellspacing="0" cellpadding="0">
					<tr bgcolor="#99CC66">
						<td>
							<xsl:text>&#160;</xsl:text>
						</td>
						<td colspan="3" id="dateformat" height="20">
							<a href="javascript:;">HOME</a>
							<xsl:text>&#160;&#160;</xsl:text>
							::
							<xsl:text>&#160;&#160;</xsl:text>
							<script language="JavaScript"
								type="text/javascript">
								document.write(TODAY);
							</script>
						</td>
					</tr>

					<tr>
						<td colspan="4" bgcolor="#99CC66"
							background="sp6_dashed_line.gif">
							<img src="schema/sp6_dashed_line.gif"
								alt="line decor" width="4" height="3" border="0" />
						</td>
					</tr>

					<tr>
						<td colspan="4" bgcolor="#5C743D">
							<img src="schema/sp6_spacer.gif" alt=""
								width="1" height="2" border="0" />
						</td>
					</tr>
				</table>

				<xsl:template match="/">
					<div>
						<b>Contents</b>
						<br />
						<div>
							<a href="#toc">Table of Contents</a>
							<br />
							<a href="#descriptions">
								Table Descriptions
							</a>
							<br />
							<a href="#indexes">Table Indexes</a>
							<br />
						</div>
						<br />
					</div>
					<div>
						<a name="toc"></a>
						<H3>Table of Contents</H3>
						<UL>
							<xsl:for-each select="//table">
								<xsl:sort select="nameDesc" />
								<LI>
									<a href="#{@table}">
										<xsl:value-of select="nameDesc" />
									</a>
								</LI>
							</xsl:for-each>
						</UL>

						<H3>Table Definitions</H3>
						<xsl:apply-templates select="database/table"
							mode="table">
							<xsl:sort select="@table" />
						</xsl:apply-templates>

						<a name="descriptions"></a>
						<H3>Table Descriptions</H3>
						<table class="tbl" border="0" cellspacing="0"
							cellpadding="2" width="75%">
							<xsl:apply-templates select="database/table"
								mode="tabledesc">
								<xsl:sort select="@table" />
							</xsl:apply-templates>
						</table>

						<a name="indexes"></a>
						<H3>Table Indexes</H3>
						<table class="tbl" border="0" cellspacing="0"
							cellpadding="2" width="75%">
							<xsl:apply-templates select="database/table"
								mode="index">
								<xsl:sort select="@table" />
							</xsl:apply-templates>
						</table>
					</div>
				</xsl:template>

				<br />
				<span class="footer">Created: 2008-02-11</span>

			</body>
		</html>
	</xsl:template>


	<xsl:template match="table" mode="table">
		<a name="{@table}"></a>
		<table class="tbl" border="0" cellspacing="0" cellpadding="2"
			width="75%">

			<tr>
				<td colspan="5" class="hdbig">
					<xsl:value-of select="nameDesc" />

					<xsl:apply-templates select="desc" />

				</td>
			</tr>
			<tr>
				<td class="hd">Field</td>
				<td class="hd">Type</td>
				<td class="hd">Length</td>
				<td class="hd">Index Name</td>
				<td class="hd">Description</td>
			</tr>

			<xsl:apply-templates select="id" />

			<xsl:apply-templates select="field">
				<xsl:sort select="@column" />
			</xsl:apply-templates>

			<tr>
				<td colspan="5" class="subhead">Relationships</td>
			</tr>
			<tr>
				<td class="hd" colspan="2">Type</td>
				<td class="hd" colspan="2">Name</td>
				<td class="hd" colspan="1">To Table</td>
			</tr>

			<xsl:apply-templates select="relationship">
				<xsl:sort select="@relationshipname" />
			</xsl:apply-templates>

		</table>
		<br />
		<br />

	</xsl:template>

	<xsl:template match="table" mode="tabledesc">
		<tr>
			<td>
				<a href="#{@table}">
					<xsl:value-of select="nameDesc" />
				</a>
			</td>
			<td>
				<xsl:if test="count( desc ) > 0">
					<xsl:apply-templates select="desc" mode="tbl" />
				</xsl:if>
				<xsl:if test="count( desc ) = 0">
					<xsl:text>&#160;</xsl:text>
				</xsl:if>

			</td>
		</tr>
	</xsl:template>

	<xsl:template match="desc">
		<xsl:choose>
			<xsl:when test=". != $empty_string">
				<br />
				<div class="desc">
					<xsl:value-of select="." />
				</div>
			</xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="desc" mode="tbl">
		<xsl:choose>
			<xsl:when test=". != $empty_string">
				<xsl:value-of select="." />
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>&#160;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="field">
		<tr>
			<!--   <td align="center"><xsl:value-of select="@column"/></td> -->
			<td align="center">
				<xsl:value-of select="nameDesc" />
				<xsl:text>&#160;</xsl:text>
			</td>
			<td align="center">
				<xsl:choose>
					<xsl:when test="@type = $memo_text">
						<xsl:text>Memo</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template
							name="substring-after-last">
							<xsl:with-param name="input" select="@type" />
							<xsl:with-param name="substr">.</xsl:with-param>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<xsl:choose>
				<xsl:when test="@length != $empty_string">
					<td align="center">
						<xsl:value-of select="@length" />
					</td>
				</xsl:when>
				<xsl:otherwise>
					<td align="center">
						<xsl:text>&#160;</xsl:text>
					</td>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="@indexName != $empty_string">
					<td align="center">
						<xsl:value-of select="@indexName" />
					</td>
				</xsl:when>
				<xsl:otherwise>
					<td align="center">
						<xsl:text>&#160;</xsl:text>
					</td>
				</xsl:otherwise>
			</xsl:choose>
			<td align="left">
				<xsl:value-of select="desc" />
				<xsl:text>&#160;</xsl:text>
			</td>
		</tr>

	</xsl:template>

	<xsl:template match="id">
		<tr>
			<td align="center">
				<xsl:value-of select="@column" />
			</td>
			<td align="center">
				<xsl:call-template name="substring-after-last">
					<xsl:with-param name="input" select="@type" />
					<xsl:with-param name="substr">.</xsl:with-param>
				</xsl:call-template>

			</td>
			<xsl:choose>
				<xsl:when test="@length != $empty_string">
					<td align="center">
						<xsl:value-of select="@length" />
					</td>
				</xsl:when>
				<xsl:otherwise>
					<td align="center">
						<xsl:text>&#160;</xsl:text>
					</td>
				</xsl:otherwise>
			</xsl:choose>
			<td align="center">
				<xsl:value-of select="@column" />
			</td>
			<td align="left">
				<xsl:text>Primary Key</xsl:text>
			</td>
		</tr>

	</xsl:template>

	<xsl:template match="relationship">
		<tr>
			<td align="center" colspan="2">
				<xsl:value-of select="@type" />
			</td>
			<td align="center" colspan="2">
				<xsl:value-of select="nameDesc" />
				<xsl:text>&#160;</xsl:text>
			</td>
			<td align="center" colspan="1">
				<xsl:call-template name="substring-after-last">
					<xsl:with-param name="input" select="@classname" />
					<xsl:with-param name="substr">.</xsl:with-param>
				</xsl:call-template>
			</td>
		</tr>
	</xsl:template>



	<xsl:template match="table" mode="index">
		<!--  
			<xsl:for-each select="field">
			<tr><td>{$bodyTextSize}</td><td>
			<a href="#{@table}"><xsl:value-of select="@column"/></a>
			</td></tr>
			</xsl:for-each>
		-->

		<xsl:apply-templates select="field" mode="index">
			<xsl:sort select="@column" />
			<xsl:with-param name="cname" select="@classname" />
			<xsl:with-param name="tname" select="@table" />
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="field" mode="index">
		<xsl:param name="cname" />
		<xsl:param name="tname" />
		<xsl:choose>
			<xsl:when test="@indexName != $empty_string">
				<tr>
					<td align="center">
						<!--  <xsl:value-of select="$tname" />-->
						<a href="#{$tname}">
							<xsl:value-of select="nameDesc" />
						</a>
					</td>
					<xsl:choose>
						<xsl:when test="@indexName != $empty_string">
							<td align="center">
								<xsl:value-of select="@indexName" />
							</td>
						</xsl:when>
						<xsl:otherwise>
							<td align="center">
								<xsl:text>&#160;</xsl:text>
							</td>
						</xsl:otherwise>
					</xsl:choose>
				</tr>
			</xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
