<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:mpeg:mpeg7:schema:2001" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001" xmlns:str="http://exslt.org/strings" xmlns:functx="http://www.functx.com" xmlns:math="http://exslt.org/math" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template name="Texture_Descriptions">
        <xsl:if test="//Appearance/*[not(self::Material)]/attribute::url">
            <Collection xsi:type="ContentCollectionType" id="Textures">                  
                <xsl:for-each select="/X3D/Scene//*[(self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)] | /X3D/Scene/ProtoDeclare/ProtoBody//*[(self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)]">
                    <xsl:if test="./Shape/Appearance/*[not(self::Material)]/attribute::url | ./Shape/Appearance/*[not(self::Material)]/attribute::USE | ./Shape/attribute::USE">
                        <ContentCollection>
                            <xsl:attribute name="id">
                                <xsl:value-of select="concat('Textures_',generate-id(.))"/>
                            </xsl:attribute>
                            <xsl:attribute name="name">
                                <xsl:choose>
                                    <xsl:when test="@DEF != ''">
                                        <xsl:value-of select="concat('Textures_',@DEF)"/>
                                    </xsl:when>
                                    <xsl:when test="@USE != ''">
                                        <xsl:value-of select="concat('Textures_',@USE)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="concat(name(.),'_',generate-id(.))"/>                    
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:for-each select="./Shape/Appearance/*[(not(self::Material)) and (attribute::url)]">
                                <Content xsi:type="MultimediaType">                
                                    <xsl:attribute name="id">
                                        <xsl:value-of select="name(.)"/>
                                        <xsl:value-of select="concat('_',generate-id(.))"/>
                                    </xsl:attribute>
                                    <Multimedia>
                                        <MediaLocator>
                                            <MediaUri>
                                                <xsl:value-of select="normalize-space(./attribute::url)"/>
                                            </MediaUri>
                                        </MediaLocator>
                                    </Multimedia>
                                </Content>
                            </xsl:for-each>
                            <xsl:for-each select="./Shape/Appearance/*[(not(self::Material)) and (attribute::USE)]">
                                <xsl:variable name="textures_DEF" select="./attribute::USE"/>
                                <xsl:if test="//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url">
                                    <Content xsi:type="MultimediaType">                    
                                        <xsl:attribute name="id">
                                            <xsl:value-of select="name(.)"/>
                                            <xsl:value-of select="concat('_',generate-id(.))"/>
                                        </xsl:attribute>
                                        <Multimedia>
                                            <MediaLocator>
                                                <MediaUri>
                                                    <xsl:value-of select="normalize-space(//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url)"/>
                                                </MediaUri>
                                            </MediaLocator>
                                        </Multimedia>
                                    </Content>
                                </xsl:if>
                            </xsl:for-each>
                            <xsl:if test="./Shape/attribute::USE">
                                <xsl:variable name="Shape_USE" select="./Shape/attribute::USE"/>
                                <xsl:if test="//Shape[@DEF=$Shape_USE]/Appearance/*[not(self::Material)]/attribute::url | //Shape/Appearance/*[not(self::Material)]/attribute::USE">
                                    <xsl:for-each select="//Shape[@DEF=$Shape_USE]/Appearance/*[(not(self::Material)) and (attribute::url)]">
                                        <Content xsi:type="MultimediaType">                      
                                            <xsl:attribute name="id">
                                                <xsl:value-of select="name(.)"/>
                                                <xsl:value-of select="concat('_',generate-id($Shape_USE))"/>
                                            </xsl:attribute>
                                            <Multimedia>
                                                <MediaLocator>
                                                    <MediaUri>
                                                        <xsl:value-of select="normalize-space(./attribute::url)"/>
                                                    </MediaUri>
                                                </MediaLocator>
                                            </Multimedia>
                                        </Content>
                                    </xsl:for-each>
                                    <xsl:for-each select="//Shape[@DEF=$Shape_USE]/Appearance/*[(not(self::Material)) and (attribute::USE)]">
                                        <xsl:variable name="textures_DEF" select="./attribute::USE"/>
                                        <xsl:if test="//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url">
                                            <Content xsi:type="MultimediaType">                        
                                                <xsl:attribute name="id">
                                                    <xsl:value-of select="name(.)"/>
                                                    <xsl:value-of select="concat('_',generate-id($Shape_USE))"/>
                                                </xsl:attribute>
                                                <Multimedia>
                                                    <MediaLocator>
                                                        <MediaUri>
                                                            <xsl:value-of select="normalize-space(//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url)"/>
                                                        </MediaUri>
                                                    </MediaLocator>
                                                </Multimedia>
                                            </Content>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:if>
                            </xsl:if>
                        </ContentCollection>
                    </xsl:if>
                    <xsl:if test="./attribute::USE">
                        <xsl:variable name="cur_node" select="."/>
                        <xsl:variable name="node_USE" select="./attribute::USE"/>
                        <xsl:variable name="node_DEF" select="/X3D/Scene//*[((self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)) and (@DEF=$node_USE)]"/>
                        <xsl:if test="$node_DEF/Shape/Appearance/*[not(self::Material)]/attribute::url | $node_DEF/Shape/Appearance/*[not(self::Material)]/attribute::USE | $node_DEF/Shape/attribute::USE">
                            <ContentCollection>
                                <xsl:attribute name="id">
                                    <xsl:value-of select="concat('Textures_',generate-id($cur_node))"/>
                                </xsl:attribute>
                                <xsl:attribute name="name">
                                    <xsl:value-of select="concat(name(.),'_',generate-id($cur_node))"/>                  
                                </xsl:attribute>
                                <xsl:for-each select="$node_DEF/Shape/Appearance/*[(not(self::Material)) and (attribute::url)]">
                                    <Content xsi:type="MultimediaType">                    
                                        <xsl:attribute name="id">
                                            <xsl:value-of select="concat(name(.),'_',generate-id($cur_node))"/>                                        
                                        </xsl:attribute>
                                        <Multimedia>
                                            <MediaLocator>
                                                <MediaUri>
                                                    <xsl:value-of select="normalize-space(./attribute::url)"/>
                                                </MediaUri>
                                            </MediaLocator>
                                        </Multimedia>
                                    </Content>
                                </xsl:for-each>
                                <xsl:for-each select="$node_DEF/Shape/Appearance/*[(not(self::Material)) and (attribute::USE)]">
                                    <xsl:variable name="textures_DEF" select="./attribute::USE"/>
                                    <xsl:if test="//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url">
                                        <Content xsi:type="MultimediaType">                      
                                            <xsl:attribute name="id">
                                                <xsl:value-of select="concat(name(.),'_',generate-id($cur_node))"/>                                        
                                            </xsl:attribute>
                                            <Multimedia>
                                                <MediaLocator>
                                                    <MediaUri>
                                                        <xsl:value-of select="normalize-space(//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url)"/>
                                                    </MediaUri>
                                                </MediaLocator>
                                            </Multimedia>
                                        </Content>
                                    </xsl:if>
                                </xsl:for-each>
                                <xsl:if test="$node_DEF/Shape/attribute::USE">
                                    <xsl:variable name="Shape_USE" select="$node_DEF/Shape/attribute::USE"/>
                                    <xsl:if test="//Shape[@DEF=$Shape_USE]/Appearance/*[not(self::Material)]/attribute::url | ./Shape/Appearance/*[not(self::Material)]/attribute::USE">
                                        <xsl:for-each select="//Shape[@DEF=$Shape_USE]/Appearance/*[(not(self::Material)) and (attribute::url)]">
                                            <Content xsi:type="MultimediaType">                        
                                                <xsl:attribute name="id">
                                                    <xsl:value-of select="concat(name(.),'_',generate-id($cur_node))"/>                                                                  
                                                </xsl:attribute>
                                                <Multimedia>
                                                    <MediaLocator>
                                                        <MediaUri>
                                                            <xsl:value-of select="normalize-space(./attribute::url)"/>
                                                        </MediaUri>
                                                    </MediaLocator>
                                                </Multimedia>
                                            </Content>
                                        </xsl:for-each>
                                        <xsl:for-each select="//Shape[@DEF=$Shape_USE]/Appearance/*[(not(self::Material)) and (attribute::USE)]">
                                            <xsl:variable name="textures_DEF" select="./attribute::USE"/>
                                            <xsl:if test="//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url">
                                                <Content xsi:type="MultimediaType">                          
                                                    <xsl:attribute name="id">
                                                        <xsl:value-of select="concat(name(.),'_',generate-id($cur_node))"/>                                                                  
                                                    </xsl:attribute>
                                                    <Multimedia>
                                                        <MediaLocator>
                                                            <MediaUri>
                                                                <xsl:value-of select="normalize-space(//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url)"/>
                                                            </MediaUri>
                                                        </MediaLocator>
                                                    </Multimedia>
                                                </Content>
                                            </xsl:if>
                                        </xsl:for-each>
                                    </xsl:if>
                                </xsl:if>
                            </ContentCollection>              
                        </xsl:if>
                    </xsl:if>
                </xsl:for-each>
                <xsl:for-each select="//Shape[(not(parent::Transform)) and (not(parent::Group)) and (not(parent::Anchor)) and (not(parent::Collision)) and (not(parent::Billboard)) and (not(parent::LOD)) and (not(parent::Switch))]">
                    <xsl:variable name="nodeName" select="name(.)"/>
                    <xsl:if test="./Appearance/*[not(self::Material)]/attribute::url">
                        <ContentCollection>
                            <xsl:attribute name="id">
                                <xsl:value-of select="concat('Textures_',generate-id(.))"/>
                            </xsl:attribute>
                            <xsl:attribute name="name">
                                <xsl:choose>
                                    <xsl:when test="@DEF != ''">
                                        <xsl:value-of select="concat('Textures_',@DEF)"/>
                                    </xsl:when>
                                    <xsl:when test="@USE != ''">
                                        <xsl:value-of select="concat('Textures_',@USE)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="concat($nodeName,'_')"/>                    
                                        <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$nodeName])"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:for-each select="./Appearance/*[(not(self::Material)) and (attribute::url)]">
                                <xsl:variable name="node" select="name(.)"/>
                                <Content xsi:type="MultimediaType">                        
                                    <xsl:attribute name="id">                    
                                        <xsl:value-of select="concat($node,'_',generate-id(.))"/>
                                    </xsl:attribute>
                                    <Multimedia>
                                        <MediaLocator>
                                            <MediaUri>
                                                <xsl:value-of select="normalize-space(./attribute::url)"/>
                                            </MediaUri>
                                        </MediaLocator>
                                    </Multimedia>
                                </Content>
                            </xsl:for-each>
                            <xsl:for-each select="./Appearance/*[(not(self::Material)) and (attribute::USE)]">
                                <xsl:variable name="nodeName" select="name(.)"/>
                                <xsl:variable name="textures_DEF" select="./attribute::USE"/>
                                <xsl:if test="//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url">
                                    <Content xsi:type="MultimediaType">                          
                                        <xsl:attribute name="id">                      
                                            <xsl:value-of select="concat($nodeName,'_',generate-id(.))"/>
                                        </xsl:attribute>
                                        <Multimedia>
                                            <MediaLocator>
                                                <MediaUri>
                                                    <xsl:value-of select="normalize-space(//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url)"/>
                                                </MediaUri>
                                            </MediaLocator>
                                        </Multimedia>
                                    </Content>
                                </xsl:if>
                            </xsl:for-each>
                        </ContentCollection>
                    </xsl:if>
                    <xsl:if test="./attribute::USE">
                        <xsl:variable name="cur_shape" select="name(.)"/>
                        <xsl:variable name="shape_USE" select="./attribute::USE"/>
                        <xsl:variable name="shape_DEF" select="//Shape[@DEF=$shape_USE]"/>
                        <xsl:if test="$shape_DEF/Appearance/*[not(self::Material)]/attribute::url">
                            <ContentCollection>
                                <xsl:attribute name="id">
                                    <xsl:value-of select="concat('Textures_',generate-id(.))"/>
                                </xsl:attribute>
                                <xsl:attribute name="name">
                                    <xsl:choose>
                                        <xsl:when test="$shape_DEF/@DEF != ''">
                                            <xsl:value-of select="concat('Textures_',$shape_DEF/@DEF)"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="concat($cur_shape,'_')"/>                                            
                                            <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$cur_shape])"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:for-each select="$shape_DEF/Appearance/*[(not(self::Material)) and (attribute::url)]">
                                    <xsl:variable name="nodeName" select="name(.)"/>
                                    <Content xsi:type="MultimediaType">                          
                                        <xsl:attribute name="id">                                            
                                            <xsl:value-of select="concat($nodeName,'_',generate-id(.))"/>
                                        </xsl:attribute>
                                        <Multimedia>
                                            <MediaLocator>
                                                <MediaUri>
                                                    <xsl:value-of select="normalize-space(./attribute::url)"/>
                                                </MediaUri>
                                            </MediaLocator>
                                        </Multimedia>
                                    </Content>
                                </xsl:for-each>
                                <xsl:for-each select="$shape_DEF/Appearance/*[(not(self::Material)) and (attribute::USE)]">
                                    <xsl:variable name="nodeName" select="name(.)"/>
                                    <xsl:variable name="textures_DEF" select="./attribute::USE"/>
                                    <xsl:if test="//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url">
                                        <Content xsi:type="MultimediaType">                          
                                            <xsl:attribute name="id">
                                                <xsl:value-of select="concat($nodeName,'_',generate-id(.))"/>
                                            </xsl:attribute>
                                            <Multimedia>
                                                <MediaLocator>
                                                    <MediaUri>
                                                        <xsl:value-of select="normalize-space(//Shape/Appearance/*[(not(self::Material)) and ($textures_DEF=@DEF)]/attribute::url)"/>
                                                    </MediaUri>
                                                </MediaLocator>
                                            </Multimedia>
                                        </Content>
                                    </xsl:if>
                                </xsl:for-each>
                            </ContentCollection>
                        </xsl:if>
                    </xsl:if>
                </xsl:for-each>
            </Collection>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>