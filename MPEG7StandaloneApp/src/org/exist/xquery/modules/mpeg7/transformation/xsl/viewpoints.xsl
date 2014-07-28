<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:mpeg:mpeg7:schema:2001" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001" xmlns:str="http://exslt.org/strings" xmlns:functx="http://www.functx.com" xmlns:math="http://exslt.org/math" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template name="Viewpoint_Descriptions">
        <xsl:if test="//Viewpoint">
            <Collection xsi:type="DescriptorCollectionType" id="Viewpoints">                
                <xsl:for-each select="/X3D/Scene//*[(self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)] | /X3D/Scene/ProtoDeclare/ProtoBody/*[(self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)]">
                    <xsl:variable name="nodeName" select="name(.)"/>
                    <xsl:if test=".//Viewpoint">                        
                        <DescriptorCollection>
                            <xsl:attribute name="id">
                                <xsl:value-of select="concat('Viewpoint_',generate-id())"/>
                            </xsl:attribute>
                            <xsl:attribute name="name">
                                <xsl:choose>
                                    <xsl:when test="@DEF != ''">
                                        <xsl:value-of select="concat('Viewpoint_',@DEF)"/>
                                    </xsl:when>
                                    <xsl:when test="@USE != ''">
                                        <xsl:value-of select="concat('Viewpoint_',@USE)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="concat($nodeName,'_')"/>                        
                                        <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$nodeName])"/>                                 
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:choose>
                                <xsl:when test="(self::*/@USE)">
                                    <xsl:variable name="DEF" select="(self::*/@USE)"/>
                                    <xsl:for-each select="(/X3D/Scene//Transform[@DEF=$DEF]//Viewpoint) | (/X3D/Scene//Group[@DEF=$DEF]//Viewpoint) | (/X3D/Scene//Anchor[@DEF=$DEF]//Viewpoint) | (/X3D/Scene//Collision[@DEF=$DEF]//Viewpoint) | (/X3D/Scene//Billboard[@DEF=$DEF]//Viewpoint) | (/X3D/Scene//LOD[@DEF=$DEF]//Viewpoint) |(/X3D/Scene//Switch[@DEF=$DEF]//Viewpoint)">
                                        <xsl:call-template name="Viewpoint_descriptors">
                                            <xsl:with-param name="path" select="(/X3D/Scene//Transform[@DEF=$DEF]//Viewpoint) | (/X3D/Scene//Group[@DEF=$DEF]//Viewpoint) | (/X3D/Scene//Anchor[@DEF=$DEF]//Viewpoint) | (/X3D/Scene//Collision[@DEF=$DEF]//Viewpoint) | (/X3D/Scene//Billboard[@DEF=$DEF]//Viewpoint) | (/X3D/Scene//LOD[@DEF=$DEF]//Viewpoint) |(/X3D/Scene//Switch[@DEF=$DEF]//Viewpoint)"/>
                                        </xsl:call-template>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:for-each select=".//Viewpoint">
                                        <xsl:choose>
                                            <xsl:when test="@USE">
                                                <xsl:variable name="sh_DEF" select="@USE"/>
                                                <xsl:call-template name="Viewpoint_descriptors">
                                                    <xsl:with-param name="path" select="//Viewpoint[$sh_DEF=@DEF]"/>
                                                </xsl:call-template>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:call-template name="Viewpoint_descriptors">
                                                    <xsl:with-param name="path" select="."/>
                                                </xsl:call-template>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:for-each>
                                </xsl:otherwise>
                            </xsl:choose>
                        </DescriptorCollection>
                        <DescriptorCollectionRef>
                            <xsl:attribute name="href">
                                <xsl:choose>
                                    <xsl:when test="@DEF != ''">
                                        <xsl:value-of select="concat('#',@DEF)"/>
                                    </xsl:when>
                                    <xsl:when test="@USE != ''">
                                        <xsl:value-of select="concat('#',@USE)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="concat('#',$nodeName,'_')"/>                        
                                        <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$nodeName])"/>                                  
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                        </DescriptorCollectionRef>
                    </xsl:if>
                </xsl:for-each>
                <xsl:for-each select="//Viewpoint[(not(parent::Transform)) and (not(parent::Group)) and (not(parent::Anchor)) and (not(parent::Collision)) and (not(parent::Billboard)) and (not(parent::LOD)) and (not(parent::Switch))]">
                    <xsl:variable name="nodeName" select="name(.)"/>                   
                    <DescriptorCollection>
                        <xsl:attribute name="id">
                            <xsl:value-of select="concat('Viewpoint_',generate-id())"/>
                        </xsl:attribute>
                        <xsl:attribute name="name">
                            <xsl:choose>
                                <xsl:when test="@DEF != ''">
                                    <xsl:value-of select="concat('Viewpoint_',@DEF)"/>
                                </xsl:when>
                                <xsl:when test="@USE != ''">
                                    <xsl:value-of select="concat('Viewpoint_',@USE)"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="concat($nodeName,'_')"/>                        
                                    <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$nodeName])"/>    
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:choose>
                            <xsl:when test="@USE">
                                <xsl:variable name="sh_DEF" select="@USE"/>
                                <xsl:call-template name="Viewpoint_descriptors">
                                    <xsl:with-param name="path" select="//Viewpoint[$sh_DEF=@DEF]"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="Viewpoint_descriptors">
                                    <xsl:with-param name="path" select="."/>
                                </xsl:call-template>
                            </xsl:otherwise>
                        </xsl:choose>
                    </DescriptorCollection>
                    <DescriptorCollectionRef>
                        <xsl:attribute name="href">
                            <xsl:choose>
                                <xsl:when test="@DEF != ''">
                                    <xsl:value-of select="concat('#',@DEF)"/>
                                </xsl:when>
                                <xsl:when test="@USE != ''">
                                    <xsl:value-of select="concat('#',@USE)"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="concat('#',$nodeName,'_')"/>                        
                                    <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$nodeName])"/>    
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                    </DescriptorCollectionRef>
                </xsl:for-each>
            </Collection>
        </xsl:if>
    </xsl:template>
    <xsl:template name="Viewpoint_descriptors">
        <xsl:param name="path"/>
        <Descriptor xsi:type="Viewpoint3DType">            
            <Viewpoint3D>
                <xsl:if test="$path/attribute::description">
                    <xsl:attribute name="description">
                        <xsl:value-of select="$path/attribute::description"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:attribute name="isAnimated">
                    <xsl:choose>
                        <xsl:when test="//ROUTE/@toNode = $path/attribute::DEF">
                            <xsl:text>true</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>false</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="fieldOfView">
                    <xsl:choose>
                        <xsl:when test="$path/attribute::fieldOfView">
                            <xsl:value-of select="$path/attribute::fieldOfView"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>0.7854</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </Viewpoint3D>
            <Viewpoint3DPosition>
                <Orientation>
                    <xsl:choose>
                        <xsl:when test="$path/attribute::orientation">
                            <xsl:value-of select="$path/attribute::orientation"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>0 0 1 0</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </Orientation>
                <Position>
                    <xsl:choose>
                        <xsl:when test="$path/attribute::position">
                            <xsl:value-of select="$path/attribute::position"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>0 0 10</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </Position>
                <CenterOfRotation>
                    <xsl:choose>
                        <xsl:when test="$path/attribute::centerOfRotation">
                            <xsl:value-of select="$path/attribute::centerOfRotation"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>0 0 0</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </CenterOfRotation>
            </Viewpoint3DPosition>
        </Descriptor>
    </xsl:template>
</xsl:stylesheet>