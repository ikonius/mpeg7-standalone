<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:mpeg:mpeg7:schema:2001" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001" xmlns:str="http://exslt.org/strings" xmlns:functx="http://www.functx.com" xmlns:math="http://exslt.org/math" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template name="Declare_All_Transforms">
        <Collection xsi:type="ContentCollectionType" id="Transformation_Paths">
            <xsl:apply-templates select="/X3D/Scene/*[(self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)] | /X3D/Scene/ProtoDeclare/ProtoBody/*[(self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)]" mode="GroupingNodes"/>
            <xsl:apply-templates select="//Shape[(not(parent::Transform)) and (not(parent::Group)) and (not(parent::Anchor)) and (not(parent::Collision)) and (not(parent::Billboard)) and (not(parent::LOD)) and (not(parent::Switch))]" mode="topLevel"/>
        </Collection>
    </xsl:template>
    <xsl:template match="Transform | Group | Anchor | Collision | Billboard | LOD | Switch" mode="GroupingNodes">
        <xsl:variable name="nodeName" select="name(.)"/>
        <ContentCollection>            
            <xsl:attribute name="name">
                <xsl:choose>
                    <xsl:when test="string-length(@DEF) &gt; 0">
                        <xsl:value-of select="@DEF"/>
                    </xsl:when>
                    <xsl:when test="string-length(@USE) &gt; 0">
                        <xsl:value-of select="@USE"/>
                    </xsl:when>
                    <xsl:otherwise>                        
                        <xsl:value-of select="concat($nodeName,'_')"/>                        
                        <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$nodeName])"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <Content xsi:type="MultimediaType">              
                <Multimedia>
                    <MediaLocator>
                        <MediaUri>
                            <xsl:text>/X3D/Scene</xsl:text>                                                        
                            <xsl:value-of select="concat('/',$nodeName,'[',1+count(preceding-sibling::*[name(.)=$nodeName]),']')"/>                            
                        </MediaUri>
                    </MediaLocator>
                </Multimedia>
            </Content>      
            <xsl:apply-templates select=".//Transform | .//Group | .//Anchor | .//Collision | .//Billboard | .//LOD | .//Switch" mode="InnerGroupings"/>      
        </ContentCollection>       
    </xsl:template>
    <xsl:template match="Transform | Group | Anchor | Collision | Billboard | LOD | Switch" mode="InnerGroupings">
        <xsl:variable name="nodeName" select="name(.)"/>       
        <Content xsi:type="MultimediaType">              
            <Multimedia>
                <MediaLocator>
                    <MediaUri>
                        <xsl:text>/X3D/Scene</xsl:text>
                        <xsl:for-each select="ancestor-or-self::*">
                            <xsl:if test="name() = 'Transform'">
                                <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::Transform),']')"/>
                            </xsl:if>
                            <xsl:if test="name() = 'Group'">
                                <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::Group),']')"/>
                            </xsl:if>
                            <xsl:if test="name() = 'Anchor'">
                                <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::Anchor),']')"/>
                            </xsl:if>
                            <xsl:if test="name() = 'Collision'">
                                <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::Collision),']')"/>
                            </xsl:if>
                            <xsl:if test="name() = 'Billboard'">
                                <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::Billboard),']')"/>
                            </xsl:if>
                            <xsl:if test="name() = 'LOD'">
                                <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::LOD),']')"/>
                            </xsl:if>
                            <xsl:if test="name() = 'Switch'">
                                <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::Switch),']')"/>
                            </xsl:if>
                        </xsl:for-each>                           
                    </MediaUri>
                </MediaLocator>
            </Multimedia>
        </Content>                  
    </xsl:template>  
    <xsl:template match="Shape" mode="topLevel">
        <xsl:variable name="nodeName" select="name(.)"/>
        <ContentCollection>           
            <xsl:attribute name="name">
                <xsl:choose>
                    <xsl:when test="string-length(@DEF) &gt; 0">
                        <xsl:value-of select="@DEF"/>
                    </xsl:when>
                    <xsl:when test="string-length(@USE) &gt; 0">
                        <xsl:value-of select="@USE"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($nodeName,'_')"/>                        
                        <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$nodeName])"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <Content xsi:type="MultimediaType">                                
                <Multimedia>
                    <MediaLocator>
                        <MediaUri>
                            <xsl:text>/X3D/Scene</xsl:text>                                                       
                            <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::Shape),']')"/>                            
                        </MediaUri>
                    </MediaLocator>
                </Multimedia>
            </Content>
        </ContentCollection>
    </xsl:template>
</xsl:stylesheet>