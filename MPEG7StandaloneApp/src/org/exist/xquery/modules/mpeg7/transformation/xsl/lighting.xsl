<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:mpeg:mpeg7:schema:2001" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001" xmlns:str="http://exslt.org/strings" xmlns:functx="http://www.functx.com" xmlns:math="http://exslt.org/math" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template name="Lighting_Descriptions">
        <xsl:if test="//SpotLight | //DirectionalLight | //PointLight">
            <Collection xsi:type="DescriptorCollectionType" id="Lightings">
                <xsl:for-each select="/X3D/Scene//*[(self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)] | /X3D/Scene/ProtoDeclare/ProtoBody/*[(self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)]">
                    <xsl:if test=".//SpotLight | .//DirectionalLight | .//PointLight">
                        <xsl:variable name="nodeName" select="name(.)"/>
                        <DescriptorCollection>
                            <xsl:attribute name="id">
                                <xsl:value-of select="concat('Lighting_',generate-id(.))"/>
                            </xsl:attribute>
                            <xsl:attribute name="name">
                                <xsl:choose>
                                    <xsl:when test="@DEF != ''">
                                        <xsl:value-of select="concat('Lighting_',@DEF)"/>
                                    </xsl:when>
                                    <xsl:when test="@USE != ''">
                                        <xsl:value-of select="concat('Lighting_',@USE)"/>
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
                                    <xsl:for-each select="(/X3D/Scene//Transform[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Group[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Anchor[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Collision[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Billboard[@DEF=$DEF]//SpotLight) | (/X3D/Scene//LOD[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Switch[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Transform[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Group[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Anchor[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Collision[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Billboard[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//LOD[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Switch[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Transform[@DEF=$DEF]//PointLight) | (/X3D/Scene//Group[@DEF=$DEF]//PointLight) | (/X3D/Scene//Anchor[@DEF=$DEF]//PointLight) | (/X3D/Scene//Collision[@DEF=$DEF]//PointLight) | (/X3D/Scene//Billboard[@DEF=$DEF]//PointLight) | (/X3D/Scene//LOD[@DEF=$DEF]//PointLight) | (/X3D/Scene//Switch[@DEF=$DEF]//PointLight)">
                                        <xsl:call-template name="Lighting_descriptors">
                                            <xsl:with-param name="path" select="(/X3D/Scene//Transform[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Group[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Anchor[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Collision[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Billboard[@DEF=$DEF]//SpotLight) | (/X3D/Scene//LOD[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Switch[@DEF=$DEF]//SpotLight) | (/X3D/Scene//Transform[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Group[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Anchor[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Collision[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Billboard[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//LOD[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Switch[@DEF=$DEF]//DirectionalLight) | (/X3D/Scene//Transform[@DEF=$DEF]//PointLight) | (/X3D/Scene//Group[@DEF=$DEF]//PointLight) | (/X3D/Scene//Anchor[@DEF=$DEF]//PointLight) | (/X3D/Scene//Collision[@DEF=$DEF]//PointLight) | (/X3D/Scene//Billboard[@DEF=$DEF]//PointLight) | (/X3D/Scene//LOD[@DEF=$DEF]//PointLight) | (/X3D/Scene//Switch[@DEF=$DEF]//PointLight)"/>
                                        </xsl:call-template>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:for-each select=".//SpotLight | .//DirectionalLight | .//PointLight">
                                        <xsl:choose>
                                            <xsl:when test="@USE">
                                                <xsl:variable name="sh_DEF" select="@USE"/>
                                                <xsl:call-template name="Lighting_descriptors">
                                                    <xsl:with-param name="path" select="//SpotLight[$sh_DEF=@DEF] | //DirectionalLight[$sh_DEF=@DEF] | //PointLight[$sh_DEF=@DEF]"/>
                                                </xsl:call-template>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:call-template name="Lighting_descriptors">
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
                <xsl:for-each select="//SpotLight[(not(parent::Transform)) and (not(parent::Group)) and (not(parent::Anchor)) and (not(parent::Collision)) and (not(parent::Billboard)) and (not(parent::LOD)) and (not(parent::Switch))] | //DirectionalLight[(not(parent::Transform)) and (not(parent::Group)) and (not(parent::Anchor)) and (not(parent::Collision)) and (not(parent::Billboard)) and (not(parent::LOD)) and (not(parent::Switch))] | //PointLight[(not(parent::Transform)) and (not(parent::Group)) and (not(parent::Anchor)) and (not(parent::Collision)) and (not(parent::Billboard)) and (not(parent::LOD)) and (not(parent::Switch))]">
                    <xsl:variable name="nodeName" select="name(.)"/>                   
                    <DescriptorCollection>
                        <xsl:attribute name="id">
                            <xsl:value-of select="concat('Lighting_',generate-id(.))"/>
                        </xsl:attribute>
                        <xsl:attribute name="name">
                            <xsl:choose>
                                <xsl:when test="@DEF != ''">
                                    <xsl:value-of select="concat('Lighting_',@DEF)"/>
                                </xsl:when>
                                <xsl:when test="@USE != ''">
                                    <xsl:value-of select="concat('Lighting_',@USE)"/>
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
                                <xsl:call-template name="Lighting_descriptors">
                                    <xsl:with-param name="path" select="//Lighting[$sh_DEF=@DEF]"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="Lighting_descriptors">
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
    <xsl:template name="Lighting_descriptors">
        <xsl:param name="path"/>
        <xsl:variable name="nodeName" select="name(.)"/>    
        <Descriptor xsi:type="Lighting3DType">            
            <Lighting3D>
                <xsl:attribute name="name">
                    <xsl:choose>
                        <xsl:when test="@DEF != ''">
                            <xsl:value-of select="@DEF"/>
                        </xsl:when>
                        <xsl:when test="@USE != ''">
                            <xsl:value-of select="@USE"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat($nodeName,'_')"/>                        
                            <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$nodeName])"/>                                      
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="lightingType">
                    <xsl:value-of select="name()"/>
                </xsl:attribute>
                <xsl:attribute name="isOn">
                    <xsl:choose>
                        <xsl:when test="@on">
                            <xsl:value-of select="@on"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>true</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="usedGlobally">
                    <xsl:choose>
                        <xsl:when test="@global">
                            <xsl:value-of select="@global"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>false</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </Lighting3D>
            <Lighting3DCharacteristics>
                <xsl:attribute name="ambientIntensity">
                    <xsl:choose>
                        <xsl:when test="@ambientIntensity">
                            <xsl:value-of select="@ambientIntensity"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>0</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="intensity">
                    <xsl:choose>
                        <xsl:when test="@intensity">
                            <xsl:value-of select="@intensity"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>1</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:if test="name()='SpotLight'">
                    <xsl:attribute name="beamWidth">
                        <xsl:choose>
                            <xsl:when test="@beamWidth">
                                <xsl:value-of select="@beamWidth"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>1.570796</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                </xsl:if>
                <xsl:if test="name()='SpotLight'">
                    <xsl:attribute name="cutOffAngle">
                        <xsl:choose>
                            <xsl:when test="@cutOffAngle">
                                <xsl:value-of select="@cutOffAngle"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>0.7854</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                </xsl:if>
                <xsl:if test="(name()='SpotLight') or (name()='PointLight')">
                    <xsl:attribute name="radius">
                        <xsl:choose>
                            <xsl:when test="@radius">
                                <xsl:value-of select="@radius"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>100</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                </xsl:if>
            </Lighting3DCharacteristics>
            <Lighting3DSpecs>
                <xsl:if test="(name()='SpotLight') or (name()='PointLight')">
                    <Attenuation>
                        <xsl:choose>
                            <xsl:when test="@attenuation">
                                <xsl:value-of select="@attenuation"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>1 0 0</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </Attenuation>
                </xsl:if>
                <DominantColor3D xsi:type="DominantColorType">                    
                    <SpatialCoherency>
                        <xsl:text>0</xsl:text>
                    </SpatialCoherency>
                    <Value>
                        <Percentage>
                            <xsl:text>1</xsl:text>
                        </Percentage>
                        <Index>
                            <xsl:choose>
                                <xsl:when test="@color">
                                    <xsl:value-of select="ceiling((number(tokenize(@color, ' ')[1])) * 255)"/>
                                    <xsl:text/>
                                    <xsl:value-of select="ceiling((number(tokenize(@color, ' ')[2])) * 255)"/>
                                    <xsl:text/>
                                    <xsl:value-of select="ceiling((number(tokenize(@color, ' ')[3])) * 255)"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>1 1 1</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </Index>
                    </Value>
                    <xsl:if test="(name()='SpotLight') or (name()='DirectionalLight')">
                        <Direction>
                            <xsl:choose>
                                <xsl:when test="@direction">
                                    <xsl:value-of select="@direction"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>0 0 0</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </Direction>
                    </xsl:if>
                    <xsl:if test="(name()='SpotLight') or (name()='PointLight')">
                        <Location>
                            <xsl:choose>
                                <xsl:when test="@location">
                                    <xsl:value-of select="@location"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>0 0 0</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </Location>
                    </xsl:if>
                </DominantColor3D>
            </Lighting3DSpecs>
        </Descriptor>        
    </xsl:template>
</xsl:stylesheet>