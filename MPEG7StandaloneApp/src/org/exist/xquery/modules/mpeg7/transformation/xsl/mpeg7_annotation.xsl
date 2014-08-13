<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:mpeg:mpeg7:schema:2001" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001" xmlns:str="http://exslt.org/strings" xmlns:functx="http://www.functx.com" xmlns:math="http://exslt.org/math" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd">
    <xsl:import href="metadata.xsl" xml:base="http://54.72.206.163/exist/apps/annotation/modules/"/>
    <xsl:import href="content_collections.xsl" xml:base="http://54.72.206.163/exist/apps/annotation/modules/"/>
    <xsl:import href="textures.xsl" xml:base="http://54.72.206.163/exist/apps/annotation/modules/"/>
    <xsl:import href="interactivity.xsl" xml:base="http://54.72.206.163/exist/apps/annotation/modules/"/>
    <xsl:import href="viewpoints.xsl" xml:base="http://54.72.206.163/exist/apps/annotation/modules/"/>
    <xsl:import href="geometries.xsl" xml:base="http://54.72.206.163/exist/apps/annotation/modules/"/>
    <xsl:import href="lighting.xsl" xml:base="http://54.72.206.163/exist/apps/annotation/modules/"/>
    <xsl:import href="relationships.xsl" xml:base="http://54.72.206.163/exist/apps/annotation/modules/"/>
    <xsl:param name="filename"/>
    <xsl:param name="IFSPointsExtraction"/>
    <xsl:param name="ILSPointsExtraction"/>
    <xsl:param name="extrusionPointsExtraction"/>
    <xsl:param name="extrusionBBoxParams"/>
    <xsl:param name="EHDs"/>
    <xsl:param name="SCDs"/>
    <xsl:param name="SURF"/>
    <xsl:param name="SGD"/>    
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template match="/">
        <Mpeg7 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:mpeg:mpeg7:schema:2001">
            <xsl:call-template name="Initialize_Metadata">
                <xsl:with-param name="filename" select="$filename"/>
            </xsl:call-template>
            <Description xsi:type="ContentEntityType">
                <MultimediaContent xsi:type="MultimediaCollectionType">
                    <StructuredCollection>
                        <xsl:call-template name="Declare_All_Transforms"/>
                        <xsl:call-template name="Texture_Descriptions"/>                            
                        <xsl:call-template name="Geometry_Descriptions">
                            <xsl:with-param name="IFSPointsExtraction" select="$IFSPointsExtraction"/>
                            <xsl:with-param name="ILSPointsExtraction" select="$ILSPointsExtraction"/>
                            <xsl:with-param name="extrusionPointsExtraction" select="$extrusionPointsExtraction"/>
                            <xsl:with-param name="extrusionBBoxParams" select="$extrusionBBoxParams"/>
                            <xsl:with-param name="EHDs" select="$EHDs"/>
                            <xsl:with-param name="SCDs" select="$SCDs"/>
                            <xsl:with-param name="SURF" select="$SURF"/>   
                            <xsl:with-param name="SGD" select="$SGD"/>                                 
                        </xsl:call-template>
                        <xsl:call-template name="Lighting_Descriptions"/>
                        <xsl:call-template name="Viewpoint_Descriptions"/>
                        <xsl:call-template name="Interaction_Descriptions"/>
                        <xsl:call-template name="relationship_Descriptions"/>
                    </StructuredCollection>
                </MultimediaContent>
            </Description>
        </Mpeg7>
    </xsl:template>
</xsl:stylesheet>
