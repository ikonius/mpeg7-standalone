<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:mpeg:mpeg7:schema:2001" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001" xmlns:str="http://exslt.org/strings" xmlns:functx="http://www.functx.com" xmlns:math="http://exslt.org/math" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>    
    <xsl:template name="Initialize_Metadata">
        <xsl:param name="filename"/>
        <Description xsi:type="ContentEntityType">
            <DescriptionMetadata>
                <xsl:call-template name="version"/>
                <xsl:call-template name="doc_identifiers"/>
                <xsl:call-template name="ProfileType"/>
                <xsl:call-template name="ScriptType"/>
            </DescriptionMetadata>
            <MultimediaContent xsi:type="MultimediaType">
                <xsl:attribute name="id">
                    <xsl:value-of select="tokenize($filename, '/')[last()]"/>
                </xsl:attribute>
                <Multimedia>
                    <MediaLocator>
                        <MediaUri>
                            <xsl:value-of select="$filename"/>
                        </MediaUri>
                    </MediaLocator>
                </Multimedia>
            </MultimediaContent>        
        </Description>
    </xsl:template>
    <xsl:template name="version">
        <xsl:if test="/X3D/@version != ''">
            <Version>
                <xsl:value-of select="/X3D/@version"/>
            </Version>
        </xsl:if>
    </xsl:template>
    <xsl:template name="ProfileType">
        <xsl:if test="/X3D/@profile != ''">
            <Profile3D>
                <xsl:value-of select="/X3D/@profile"/>
            </Profile3D>
        </xsl:if>
    </xsl:template>
    <xsl:template name="ScriptType">
        <xsl:if test="//Script">
            <Script3D>
                <xsl:variable name="url" select="X3D/Scene//Script/@url"/>
                <xsl:choose>
                    <xsl:when test="contains($url,'.class')">
                        <externalScript>
                            <xsl:text>Java</xsl:text>
                        </externalScript>
                    </xsl:when>
                    <xsl:when test="contains($url,'.js')">
                        <externalScript>
                            <xsl:text>JScript</xsl:text>
                        </externalScript>
                    </xsl:when>
                    <xsl:otherwise>
                        <internalScript>
                            <xsl:text>JavaScript</xsl:text>
                        </internalScript>
                    </xsl:otherwise>
                </xsl:choose>
            </Script3D>
        </xsl:if>
    </xsl:template>
    <xsl:template name="doc_identifiers">
        <xsl:if test="/X3D/head/meta[@name='identifier']">
            <PrivateIdentifier>
                <xsl:value-of select="/X3D/head/meta[@name='identifier']/@content"/>
            </PrivateIdentifier>
        </xsl:if>
    </xsl:template>
    <xsl:template name="doc_description">
        <xsl:if test="/X3D/head/meta[@name='description']">
            <Summary>
                <xsl:value-of select="/X3D/head/meta[@name='description']/@content"/>
            </Summary>
        </xsl:if>
    </xsl:template>
	
    <xsl:template name="doc_created">
        <xsl:if test="/X3D/head/meta[@name='created']">
            <CreationTime>
                <xsl:value-of select="/X3D/head/meta[@name='created']/@content"/>
            </CreationTime>
        </xsl:if>
    </xsl:template>
	
    <xsl:template name="doc_modified">
        <xsl:if test="/X3D/head/meta[@name='modified']">
            <LastUpdate>
                <xsl:value-of select="/X3D/head/meta[@name='modified']/@content"/>
            </LastUpdate>
        </xsl:if>
    </xsl:template>
	
    <xsl:template name="doc_creator">
        <xsl:if test="/X3D/head/meta[@name='creator']">
            <Creator>
                <xsl:value-of select="/X3D/head/meta[@name='creator']/@content"/>
            </Creator>
        </xsl:if>
    </xsl:template>
	
    <xsl:template name="doc_rights">
        <xsl:if test="/X3D/head/meta[@name='rights']">
            <Rights>
                <xsl:value-of select="/X3D/head/meta[@name='rights']/@content"/>
            </Rights>
        </xsl:if>
    </xsl:template>
	
    <xsl:template name="doc_comment">
        <xsl:if test="/X3D/head/meta[@name='']">
            <Comment>
                <xsl:value-of select="/X3D/head/meta[@name='']/@content"/>
            </Comment>
        </xsl:if>
    </xsl:template>
	
    <xsl:template name="doc_publicId">
        <xsl:if test="/X3D/head/meta[@name='']">
            <PublicIdentifier>
                <xsl:value-of select="/X3D/head/meta[@name='identifier']/@content"/>
            </PublicIdentifier>
        </xsl:if>
    </xsl:template>
	
    <xsl:template name="doc_confidence">		
        <Confidence>
            <xsl:text>1</xsl:text>
        </Confidence>
    </xsl:template>
	
    <xsl:template name="doc_location">
        <xsl:if test="/X3D/head/meta[@name='']">
            <CreationLocation>
                <xsl:value-of select="/X3D/head/meta[@name='']/@content"/>
            </CreationLocation>			
        </xsl:if>
    </xsl:template>       
</xsl:stylesheet>
