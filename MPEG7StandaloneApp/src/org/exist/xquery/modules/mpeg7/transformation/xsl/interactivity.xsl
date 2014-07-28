<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:mpeg:mpeg7:schema:2001" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001" xmlns:str="http://exslt.org/strings" xmlns:functx="http://www.functx.com" xmlns:math="http://exslt.org/math" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template name="Interaction_Descriptions">
        <xsl:if test="/X3D/Scene/descendant::*[contains(name(.),'Interpolator') or contains(name(.),'Sensor') or contains(name(.),'Trigger') or contains(name(.),'Filter')]">
            <Collection xsi:type="DescriptorCollectionType" id="Interactions">                                
                <xsl:for-each select="/X3D/Scene//*[(self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)] | /X3D/Scene/ProtoDeclare/ProtoBody/*[(self::Transform) or (self::Group) or (self::Anchor) or (self::Collision) or (self::Billboard) or (self::LOD) or (self::Switch)]">
                    <xsl:if test="descendant::*[contains(name(.),'Interpolator') or contains(name(.),'Sensor') or contains(name(.),'Trigger') or contains(name(.),'Filter')]">
                        <xsl:variable name="nodeName" select="name(.)"/>
                        <DescriptorCollection>
                            <xsl:attribute name="id">
                                <xsl:value-of select="concat('Interaction_',generate-id(.))"/>
                            </xsl:attribute>
                            <xsl:attribute name="name">
                                <xsl:choose>
                                    <xsl:when test="@DEF != ''">
                                        <xsl:value-of select="concat('Interaction_',@DEF)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="concat(name(),'_')"/>
                                        <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$nodeName])"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:for-each select="(.//*)">
                                <xsl:if test="contains(name(.),'Interpolator')">
                                    <xsl:choose>
                                        <xsl:when test="@USE">
                                            <xsl:variable name="Interpolator_DEF" select="@USE"/>
                                            <xsl:value-of select="//*[contains(name(.),'Interpolator')]/@DEF=$Interpolator_DEF"/>
                                            <Descriptor xsi:type="MotionTrajectoryType">                                                
                                                <xsl:attribute name="motionType">
                                                    <xsl:value-of select="name(.)"/>
                                                </xsl:attribute>
                                                <CoordDef units="meter">                                                    
                                                </CoordDef>
                                                <Params>
                                                    <KeyTimePoint>
                                                        <xsl:for-each select="tokenize(@key,' ')">
                                                            <MediaRelIncrTimePoint>
                                                                <xsl:value-of select="number(.)"/>
                                                            </MediaRelIncrTimePoint>
                                                        </xsl:for-each>
                                                    </KeyTimePoint>
                                                    <InterpolationFunctions>
                                                        <xsl:for-each select="tokenize(@keyValue, ' ')">
                                                            <KeyValue>
                                                                <xsl:value-of select="number(.)"/>
                                                            </KeyValue>
                                                        </xsl:for-each>
                                                    </InterpolationFunctions>
                                                </Params>
                                            </Descriptor>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <Descriptor xsi:type="MotionTrajectoryType">                                                
                                                <xsl:attribute name="motionType">
                                                    <xsl:value-of select="name(.)"/>
                                                </xsl:attribute>
                                                <CoordDef units="meter">                                                    
                                                </CoordDef>
                                                <Params>
                                                    <KeyTimePoint>
                                                        <xsl:for-each select="tokenize(@key,' ')">
                                                            <MediaRelIncrTimePoint>
                                                                <xsl:value-of select="number(.)"/>
                                                            </MediaRelIncrTimePoint>
                                                        </xsl:for-each>
                                                    </KeyTimePoint>
                                                    <InterpolationFunctions>
                                                        <xsl:for-each select="tokenize(@keyValue, ' ')">
                                                            <KeyValue>
                                                                <xsl:value-of select="number(.)"/>
                                                            </KeyValue>
                                                        </xsl:for-each>
                                                    </InterpolationFunctions>
                                                </Params>
                                            </Descriptor>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:if>
                            </xsl:for-each>
                            <xsl:for-each select=".//ROUTE">
                                <xsl:variable name="from_node" select="@fromNode"/>
                                <xsl:variable name="to_node" select="@toNode"/>
                                <xsl:variable name="nodeName" select="name(.)"/>
                                <Descriptor xsi:type="Interactivity3DType">                                    
                                    <TriggerSource>
                                        <xsl:choose>
                                            <xsl:when test="name(/X3D/Scene/descendant::*[@DEF=$from_node])='Script'">
                                                <xsl:choose>
                                                    <xsl:when test="/X3D/Scene/descendant::*[@DEF=$from_node]/@url">
                                                        <xsl:text>ExternalScript</xsl:text>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:text>InternalScript</xsl:text>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:text>UserDefined</xsl:text>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </TriggerSource>
                                    <Route fromNode="$from_node" toNode="$to_node">                                        
                                        <xsl:attribute name="fromNodeType">
                                            <xsl:value-of select="name(/X3D/Scene/descendant::*[@DEF=$from_node])"/>
                                        </xsl:attribute>                                        
                                        <xsl:attribute name="toNodeType">
                                            <xsl:value-of select="name(/X3D/Scene/descendant::*[@DEF=$to_node])"/>
                                        </xsl:attribute>
                                        <xsl:text>/X3D/Scene</xsl:text>
                                        <xsl:for-each select="ancestor-or-self::*">
                                            <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::*[name(.)=$nodeName]),']')"/>                                            
                                        </xsl:for-each>
                                        <xsl:for-each select="descendant-or-self::ROUTE">
                                            <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::ROUTE),']')"/>
                                        </xsl:for-each>
                                    </Route>
                                </Descriptor>
                            </xsl:for-each>
                        </DescriptorCollection>
                        <DescriptorCollectionRef>
                            <xsl:variable name="nodeNameRef" select="name(.)"/>
                            <xsl:attribute name="href">
                                <xsl:choose>
                                    <xsl:when test="@DEF != ''">
                                        <xsl:value-of select="concat('#',@DEF)"/>
                                    </xsl:when>
                                    <xsl:otherwise>                                        
                                        <xsl:value-of select="concat('#',name(),'_')"/>
                                        <xsl:value-of select="1+count(preceding-sibling::*[name(.)=$nodeNameRef])"/>                                    
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                        </DescriptorCollectionRef>
                    </xsl:if>
                </xsl:for-each>
                <xsl:for-each select="/X3D/Scene | /X3D/Scene/ProtoDeclare/ProtoBody">
                    <xsl:if test="child::*[contains(name(.),'Interpolator') or contains(name(.),'Sensor') or contains(name(.),'Trigger') or contains(name(.),'Filter')]">
                        <DescriptorCollection name="Scene">
                            <xsl:attribute name="id">
                                <xsl:value-of select="concat('Interaction_',generate-id(.))"/>
                            </xsl:attribute>                            
                            <xsl:for-each select="(./*)">
                                <xsl:if test="contains(name(.),'Interpolator')">
                                    <xsl:choose>
                                        <xsl:when test="@USE">
                                            <xsl:variable name="Interpolator_DEF" select="@USE"/>
                                            <xsl:variable name="Interpolator_Path" select="/X3D/Scene//*[(contains(name(.),'Interpolator')) and (@DEF=$Interpolator_DEF)]"/>
                                            <Descriptor xsi:type="MotionTrajectoryType">                                                
                                                <xsl:attribute name="motionType">
                                                    <xsl:value-of select="name($Interpolator_Path)"/>
                                                </xsl:attribute>
                                                <CoordDef units="meter">                                                    
                                                </CoordDef>
                                                <Params>
                                                    <KeyTimePoint>
                                                        <xsl:for-each select="tokenize($Interpolator_Path/@key,' ')">
                                                            <MediaRelIncrTimePoint>
                                                                <xsl:value-of select="number(.)"/>
                                                            </MediaRelIncrTimePoint>
                                                        </xsl:for-each>
                                                    </KeyTimePoint>
                                                    <InterpolationFunctions>
                                                        <xsl:for-each select="tokenize($Interpolator_Path/@keyValue, ' ')">
                                                            <KeyValue>
                                                                <xsl:value-of select="number(.)"/>
                                                            </KeyValue>
                                                        </xsl:for-each>
                                                    </InterpolationFunctions>
                                                </Params>
                                            </Descriptor>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <Descriptor xsi:type="MotionTrajectoryType">                                                
                                                <xsl:attribute name="motionType">
                                                    <xsl:value-of select="name(.)"/>
                                                </xsl:attribute>
                                                <CoordDef units="meter">                                                    
                                                </CoordDef>
                                                <Params>
                                                    <KeyTimePoint>
                                                        <xsl:for-each select="tokenize(@key,' ')">
                                                            <MediaRelIncrTimePoint>
                                                                <xsl:value-of select="number(.)"/>
                                                            </MediaRelIncrTimePoint>
                                                        </xsl:for-each>
                                                    </KeyTimePoint>
                                                    <InterpolationFunctions>
                                                        <xsl:for-each select="tokenize(@keyValue, ' ')">
                                                            <KeyValue>
                                                                <xsl:value-of select="number(.)"/>
                                                            </KeyValue>
                                                        </xsl:for-each>
                                                    </InterpolationFunctions>
                                                </Params>
                                            </Descriptor>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:if>
                            </xsl:for-each>
                            <xsl:for-each select="./ROUTE">
                                <xsl:variable name="from_node" select="@fromNode"/>
                                <xsl:variable name="to_node" select="@toNode"/>
                                <xsl:variable name="nodeName" select="name(.)"/>
                                <Descriptor xsi:type="Interactivity3DType">                                    
                                    <TriggerSource>
                                        <xsl:choose>
                                            <xsl:when test="name(/X3D/Scene/descendant::*[@DEF=$from_node])='Script'">
                                                <xsl:choose>
                                                    <xsl:when test="/X3D/Scene/descendant::*[@DEF=$from_node]/@url">
                                                        <xsl:text>ExternalScript</xsl:text>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:text>InternalScript</xsl:text>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:text>UserDefined</xsl:text>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </TriggerSource>
                                    <Route fromNode="$from_node" toNode="$to_node">                                        
                                        <xsl:attribute name="fromNodeType">
                                            <xsl:value-of select="name(/X3D/Scene/descendant::*[@DEF=$from_node])"/>
                                        </xsl:attribute>                                        
                                        <xsl:attribute name="toNodeType">
                                            <xsl:value-of select="name(/X3D/Scene/descendant::*[@DEF=$to_node])"/>
                                        </xsl:attribute>
                                        <xsl:text>/X3D/Scene</xsl:text>
                                        <xsl:for-each select="ancestor-or-self::*">
                                            <xsl:value-of select="concat('/',$nodeName,'[',1+count(preceding-sibling::*[name(.)=$nodeName]),']')"/>
                                        </xsl:for-each>
                                        <xsl:for-each select="descendant-or-self::ROUTE">
                                            <xsl:value-of select="concat('/',name(),'[',1+count(preceding-sibling::ROUTE),']')"/>
                                        </xsl:for-each>
                                    </Route>
                                </Descriptor>
                            </xsl:for-each>
                        </DescriptorCollection>
                        <DescriptorCollectionRef href="#Scene">
                        </DescriptorCollectionRef>
                    </xsl:if>
                </xsl:for-each>
            </Collection>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>