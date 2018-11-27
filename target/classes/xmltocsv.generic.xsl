<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

    <xsl:template match="node()">
        <xsl:value-of select="name()"/>
        <xsl:text>&#xA;</xsl:text>
        <xsl:call-template name="loop"/>
    </xsl:template>

    <xsl:template name="loop">
        <!-- Output headers -->
        <xsl:for-each select="./*[count(*) = 0]">
            <xsl:value-of select="name()"/>
            <xsl:if test="position() != last()">
                <xsl:text>,</xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>&#xA;</xsl:text>

        <!-- Output values -->
        <xsl:for-each select="./*[count(*) = 0]">
            <xsl:value-of select="."/>
            <xsl:if test="position() != last()">
                <xsl:text>,</xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>&#xA;</xsl:text>

        <!-- Process nodes having childs -->
        <xsl:for-each select="./*[count(*) != 0]">
            <xsl:value-of select="name()"/>
            <xsl:text>&#xA;</xsl:text>
            <xsl:call-template name="loop"/>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>