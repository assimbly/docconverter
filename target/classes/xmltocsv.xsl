<?xml version="1.0" encoding="US-ASCII"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<xsl:output method="text"/>

<xsl:variable name="firstFieldName" 
              select="name((//row/*[not(*)])[1])"/>

<xsl:key name="names" match="row/*[not(*)]" use="name(.)"/>

<xsl:template match="/">
  <!--header row-->
  <xsl:for-each select="//row/*[not(*)]
                        [generate-id(.)=
                         generate-id(key('names',name(.))[1])]">
    <xsl:if test="position()>1">,</xsl:if>
    <xsl:value-of select="name(.)"/>
  </xsl:for-each>

  <!--body-->
  <xsl:apply-templates select="*"/>

  <!--final line terminator-->
  <xsl:text>&#xa;</xsl:text>
</xsl:template>

<!--elements only process elements, not text-->
<xsl:template match="*">
  <xsl:apply-templates select="*"/>
</xsl:template>

<!--these elements are CSV fields-->
<xsl:template match="row/*[not(*)]">
  <!--replicate ancestors if necessary-->
  <xsl:if test="position()=1 and ../preceding-sibling::row">
    <xsl:for-each select="ancestor::row[position()>1]/*[not(*)]">
      <xsl:call-template name="doThisField"/>
    </xsl:for-each>
  </xsl:if>
  <xsl:call-template name="doThisField"/>
</xsl:template>

<!--put out a field ending the previous field and escaping content-->
<xsl:template name="doThisField">
  <xsl:choose>
    <xsl:when test="name(.)=$firstFieldName">
      <!--previous line terminator-->
      <xsl:text>&#xa;</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <!--previous field terminator-->
      <xsl:text>,</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
  <!--field value escaped per RFC4180-->
  <xsl:choose>
    <xsl:when test="contains(.,'&#x22;') or 
                    contains(.,',') or
                    contains(.,'&#xa;')">
      <xsl:text>"</xsl:text>
      <xsl:call-template name="escapeQuote"/>
      <xsl:text>"</xsl:text>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--escape a double quote in the current node value with two double quotes-->
<xsl:template name="escapeQuote">
  <xsl:param name="rest" select="."/>
  <xsl:choose>
    <xsl:when test="contains($rest,'&#x22;')">
      <xsl:value-of select="substring-before($rest,'&#x22;')"/>
      <xsl:text>""</xsl:text>
      <xsl:call-template name="escapeQuote">
        <xsl:with-param name="rest" select="substring-after($rest,'&#x22;')"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$rest"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>