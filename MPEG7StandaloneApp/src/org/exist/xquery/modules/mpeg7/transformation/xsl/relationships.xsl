<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:mpeg:mpeg7:schema:2001" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001" xmlns:str="http://exslt.org/strings" xmlns:functx="http://www.functx.com" xmlns:math="http://exslt.org/math" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template name="relationship_Descriptions">
        <xsl:element name="Relationships">     
            <xsl:apply-templates select="//Transform | //Group | //Anchor | //Collision | //Billboard | //LOD | //Switch" mode="relations">
            </xsl:apply-templates>
        </xsl:element>
    </xsl:template>

    <xsl:template match="Transform | Group | Anchor | Collision | Billboard | LOD | Switch" mode="relations"> 
        <xsl:variable name="parentNodeName" select="name(..)"/>
        <xsl:variable name="nodeName" select="name(.)"/>        
        <xsl:variable name="parentDEF">
            <xsl:choose>
                <xsl:when test="string-length(../@DEF) &gt; 0">
                    <xsl:value-of select="../@DEF"/>
                </xsl:when>
                <xsl:when test="string-length(../@USE) &gt; 0">
                    <xsl:value-of select="../@USE"/>
                </xsl:when>
            </xsl:choose>            
        </xsl:variable>                   
        <xsl:if test="($parentNodeName='Transform') or ($parentNodeName='Group') or ($parentNodeName='Anchor') or ($parentNodeName='Collision') or ($parentNodeName='Billboard') or ($parentNodeName='LOD') or ($parentNodeName='Switch')">
            <Relation type="urn:mpeg:mpeg7:cs:BaseRelationCS:2001:member">                
                <xsl:attribute name="source">
                    <xsl:choose>
                        <xsl:when test="string-length($parentDEF) &gt; 0">
                            <xsl:value-of select="concat('#',$parentDEF,'_', $parentNodeName)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat('#',$parentNodeName,'_')"/>                            
                            <xsl:number count="/X3D/Scene/node()[name()=$parentNodeName] | /X3D/Scene/ProtoDeclare/ProtoBody/node()[name()=$parentNodeName]" from="Scene" level="single"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="target">
                    <xsl:choose>
                        <xsl:when test="string-length(@DEF) &gt; 0">
                            <xsl:value-of select="concat('#',@DEF,'_', $nodeName)"/>
                        </xsl:when>
                        <xsl:when test="string-length(@USE) &gt; 0">
                            <xsl:value-of select="concat('#',@USE,'_USE')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat('#',$nodeName,'_')"/>                            
                            <xsl:number count="Transform | Group | Anchor | Collision | Billboard | LOD | Switch" from="Scene" level="multiple"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </Relation>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>