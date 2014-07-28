<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:mpeg:mpeg7:schema:2001" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001" xmlns:str="http://exslt.org/strings" xmlns:functx="http://www.functx.com" xmlns:math="http://exslt.org/math" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd">
    <xsl:import href="geometry_helpers.xsl" xml:base="http://54.72.206.163/exist/apps/annotation/modules/"/>
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>    
    <xsl:param name="IFSPointsExtraction"/>
    <xsl:param name="ILSPointsExtraction"/>
    <xsl:param name="extrusionPointsExtraction"/>
    <xsl:param name="extrusionBBoxParams"/>
    <xsl:param name="EHDs"/>
    <xsl:param name="SCDs"/>
    <xsl:param name="SURF"/>
    <xsl:param name="SGD"/>    
    <xsl:template name="Geometry_Descriptions">
        <xsl:if test="//Shape">
            <Collection xsi:type="DescriptorCollectionType" id="Geometries">  
                <xsl:apply-templates select="//Shape" mode="Geometry_Collections">                    
                </xsl:apply-templates>                                                             
            </Collection>
        </xsl:if>
    </xsl:template>
    <xsl:template match="Shape" mode="Geometry_Collections">     
        <xsl:variable name="nodeName" select="name(.)"/>   
        <xsl:variable name="def">
            <xsl:choose>
                <xsl:when test="string-length(@DEF) &gt; 0">
                    <xsl:value-of select="@DEF"/>
                </xsl:when>
                <xsl:when test="string-length(@USE) &gt; 0">
                    <xsl:value-of select="@USE"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <DescriptorCollection>
            <xsl:attribute name="id">
                <xsl:value-of select="concat('Geometry_',generate-id(.))"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:choose>
                    <xsl:when test="string-length($def) &gt; 0">
                        <xsl:value-of select="concat('Geometry_',$def)"/>
                    </xsl:when>                                
                    <xsl:otherwise>
                        <xsl:value-of select="concat($nodeName,'_')"/>
                        <xsl:value-of select="1+count(preceding::*[name()=$nodeName])"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="string-length($def) &gt; 0">
                    <xsl:apply-templates select="//Shape[@DEF=$def]" mode="Geometry_descriptors"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="." mode="Geometry_descriptors"/>
                </xsl:otherwise>
            </xsl:choose>
                            
        </DescriptorCollection>
        <DescriptorCollectionRef>
            <xsl:attribute name="href">
                <xsl:choose>
                    <xsl:when test="string-length($def) &gt; 0">
                        <xsl:value-of select="concat('#',$def)"/>
                    </xsl:when>                                
                    <xsl:otherwise>
                        <xsl:value-of select="concat('#',$nodeName,'_')"/>
                        <xsl:value-of select="1+count(preceding::*[name()=$nodeName])"/>                                                                    
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
        </DescriptorCollectionRef>                
    </xsl:template>
    <xsl:template match="Shape" mode="Geometry_descriptors">
        <!--Metadata3D Descriptor-->
        <xsl:apply-templates select=".//*[contains(name(.),'Metadata')]" mode="MetadataDescriptor"/>        
        <!--BoundingBox3D Descriptor-->
        <xsl:apply-templates select=".//Box | .//Cone | .//Cylinder | .//Sphere | .//IndexedFaceSet | .//IndexedLineSet | .//Extrusion" mode="BoundingBox"/>
        <!-- Geometry3D Descriptor-->
        <xsl:apply-templates select=".//Box | .//Cone | .//Cylinder | .//Sphere | .//IndexedFaceSet | .//IndexedLineSet | .//Extrusion" mode="Geometry3D"/>
        <!--EdgeHistogram Descriptor (EHD), ScalableColor Descriptor (SCD)-->
        <xsl:if test="string-length($EHDs) &gt; 0">                                                
            <xsl:apply-templates select=".//ImageTexture">                
                <xsl:with-param name="position" select="position()"/>               
            </xsl:apply-templates>           
        </xsl:if>
    </xsl:template>
    <xsl:template match="Box | Cone | Cylinder | Sphere | IndexedFaceSet | IndexedLineSet | Extrusion" mode="BoundingBox">
        <xsl:variable name="shapeType" select="name(.)"/>
        <Descriptor xsi:type="BoundingBox3DType">                
            <xsl:choose>
                <xsl:when test="$shapeType = 'Box'">
                    <BoundingBox3DSize>
                        <xsl:choose>
                            <xsl:when test="@size">
                                <xsl:attribute name="BoxWidth">
                                    <xsl:value-of select="tokenize(@size, ' ')[1]"/>
                                </xsl:attribute>
                                <xsl:attribute name="BoxHeight">
                                    <xsl:value-of select="tokenize(@size, ' ')[2]"/>
                                </xsl:attribute>
                                <xsl:attribute name="BoxDepth">
                                    <xsl:value-of select="tokenize(@size, ' ')[3]"/>
                                </xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="BoxWidth">
                                    <xsl:text>2</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="BoxHeight">
                                    <xsl:text>2</xsl:text>
                                </xsl:attribute>
                                <xsl:attribute name="BoxDepth">
                                    <xsl:text>2</xsl:text>
                                </xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                    </BoundingBox3DSize>
                    <BoundingBox3DCenter>
                        <xsl:attribute name="BoxCenterW">
                            <xsl:text>0</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="BoxCenterH">
                            <xsl:text>0</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="BoxCenterD">
                            <xsl:text>0</xsl:text>
                        </xsl:attribute>
                    </BoundingBox3DCenter>
                </xsl:when>
                <xsl:when test="$shapeType = 'Cone'">
                    <BoundingBox3DSize>
                        <xsl:attribute name="BoxWidth">
                            <xsl:choose>
                                <xsl:when test="@bottomRadius">
                                    <xsl:value-of select="@bottomRadius * 2"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>2</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:attribute name="BoxHeight">
                            <xsl:choose>
                                <xsl:when test="@height">
                                    <xsl:value-of select="@height"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>2</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:attribute name="BoxDepth">
                            <xsl:choose>
                                <xsl:when test="@bottomRadius">
                                    <xsl:value-of select="@bottomRadius * 2"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>2</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                    </BoundingBox3DSize>
                    <BoundingBox3DCenter>
                        <xsl:attribute name="BoxCenterW">
                            <xsl:text>0</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="BoxCenterH">
                            <xsl:text>0</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="BoxCenterD">
                            <xsl:text>0</xsl:text>
                        </xsl:attribute>
                    </BoundingBox3DCenter>
                </xsl:when>
                <xsl:when test="$shapeType = 'Cylinder'">
                    <BoundingBox3DSize>
                        <xsl:attribute name="BoxWidth">
                            <xsl:choose>
                                <xsl:when test="@radius">
                                    <xsl:value-of select="@radius * 2"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="2"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:attribute name="BoxHeight">
                            <xsl:choose>
                                <xsl:when test="@height">
                                    <xsl:value-of select="@height"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="2"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:attribute name="BoxDepth">
                            <xsl:choose>
                                <xsl:when test="@radius">
                                    <xsl:value-of select="@radius * 2"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="2"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                    </BoundingBox3DSize>
                    <BoundingBox3DCenter>
                        <xsl:attribute name="BoxCenterW">
                            <xsl:value-of select="0"/>
                        </xsl:attribute>
                        <xsl:attribute name="BoxCenterH">
                            <xsl:value-of select="0"/>
                        </xsl:attribute>
                        <xsl:attribute name="BoxCenterD">
                            <xsl:value-of select="0"/>
                        </xsl:attribute>
                    </BoundingBox3DCenter>
                </xsl:when>
                <xsl:when test="$shapeType = 'Sphere'">
                    <BoundingBox3DSize>
                        <xsl:choose>
                            <xsl:when test="@radius">
                                <xsl:attribute name="BoxWidth">
                                    <xsl:value-of select="@radius * 2"/>
                                </xsl:attribute>
                                <xsl:attribute name="BoxHeight">
                                    <xsl:value-of select="@radius * 2"/>
                                </xsl:attribute>
                                <xsl:attribute name="BoxDepth">
                                    <xsl:value-of select="@radius * 2"/>
                                </xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="BoxWidth">
                                    <xsl:value-of select="2"/>
                                </xsl:attribute>
                                <xsl:attribute name="BoxHeight">
                                    <xsl:value-of select="2"/>
                                </xsl:attribute>
                                <xsl:attribute name="BoxDepth">
                                    <xsl:value-of select="2"/>
                                </xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                    </BoundingBox3DSize>
                    <BoundingBox3DCenter>
                        <xsl:attribute name="BoxCenterW">
                            <xsl:value-of select="0"/>
                        </xsl:attribute>
                        <xsl:attribute name="BoxCenterH">
                            <xsl:value-of select="0"/>
                        </xsl:attribute>
                        <xsl:attribute name="BoxCenterD">
                            <xsl:value-of select="0"/>
                        </xsl:attribute>
                    </BoundingBox3DCenter>
                </xsl:when>
                <xsl:when test="($shapeType = 'IndexedFaceSet') or ($shapeType = 'IndexedLineSet')">
                    <xsl:variable name="pointCoordinates" select="./Coordinate/@point"/>                        
                    <xsl:variable name="pointsNumber" select="count(tokenize($pointCoordinates, ' '))"/>
                    
                    <!--KALEI ENA TEPLATE GIA THN EYRESI TON MAX KAI MIN TOY X APO TO ATTRIBUTE
                    point TOY NODE Coordinate ENOS IndexedFaceSet. OLA TA TEMPLATE PERIKLEIONTAI
                    SE ENA XEXORISTO VARIABLE POU PERIEXEI TA MIN KAI MAX KATHE AXIS POINT-->
                    <xsl:variable name="XpointsResults">
                        <xsl:call-template name="pointsCalculationX">
                            <xsl:with-param name="count" select="$pointsNumber - 2"/>
                            <xsl:with-param name="count2" select="$pointCoordinates"/>
                            <xsl:with-param name="Xpoint" select="number(tokenize($pointCoordinates, ' ')[$pointsNumber - 2])"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <!--KALEI ENA TEPLATE GIA THN EYRESI TON MAX KAI MIN TOY Y APO TO ATTRIBUTE
                    point TOY NODE Coordinate ENOS IndexedFaceSet-->
                    <xsl:variable name="YpointsResults">
                        <xsl:call-template name="pointsCalculationY">
                            <xsl:with-param name="count" select="$pointsNumber - 1"/>
                            <xsl:with-param name="count2" select="$pointCoordinates"/>
                            <xsl:with-param name="Ypoint" select="number(tokenize($pointCoordinates, ' ')[$pointsNumber - 1])"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <!--KALEI ENA TEPLATE GIA THN EYRESI TON MAX KAI MIN TOY Z APO TO ATTRIBUTE
                    point TOY NODE Coordinate ENOS IndexedFaceSet-->
                    <xsl:variable name="ZpointsResults">
                        <xsl:call-template name="pointsCalculationZ">
                            <xsl:with-param name="count" select="$pointsNumber"/>
                            <xsl:with-param name="count2" select="$pointCoordinates"/>
                            <xsl:with-param name="Zpoint" select="number(tokenize($pointCoordinates, ' ')[$pointsNumber])"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <BoundingBox3DSize>
                        <xsl:attribute name="BoxWidth">                           
                            <xsl:value-of select="tokenize($XpointsResults, ' ')[2] - tokenize($XpointsResults, ' ')[1]"/>
                        </xsl:attribute>
                        <xsl:attribute name="BoxHeight">
                            <xsl:value-of select="tokenize($YpointsResults, ' ')[2] - tokenize($YpointsResults, ' ')[1]"/>
                        </xsl:attribute>
                        <xsl:attribute name="BoxDepth">
                            <xsl:value-of select="tokenize($ZpointsResults, ' ')[2] - tokenize($ZpointsResults, ' ')[1]"/>
                        </xsl:attribute>
                    </BoundingBox3DSize>
                    <BoundingBox3DCenter>
                        <xsl:attribute name="BoxCenterW">
                            <xsl:value-of select="(tokenize($XpointsResults, ' ')[1] + tokenize($XpointsResults, ' ')[2]) div 2"/>
                        </xsl:attribute>
                        <xsl:attribute name="BoxCenterH">
                            <xsl:value-of select="(tokenize($YpointsResults, ' ')[1] + tokenize($YpointsResults, ' ')[2]) div 2"/>
                        </xsl:attribute>
                        <xsl:attribute name="BoxCenterD">
                            <xsl:value-of select="(tokenize($ZpointsResults, ' ')[1] + tokenize($ZpointsResults, ' ')[2]) div 2"/>
                        </xsl:attribute>
                    </BoundingBox3DCenter>
                </xsl:when>
                <xsl:when test="$shapeType = 'Extrusion'">
                    <xsl:variable name="positionOfExtrForBBox" select="count(preceding::Extrusion) + 1"/>
                    <xsl:call-template name="extrusionBBoxTemplate">
                        <xsl:with-param name="stringOfExtrBBox" select="tokenize($extrusionBBoxParams, '#')[$positionOfExtrForBBox]"/>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
        </Descriptor>        
    </xsl:template>
    <xsl:template match="Box | Cone | Cylinder | Sphere | IndexedFaceSet | IndexedLineSet | Extrusion" mode="Geometry3D">
        <xsl:variable name="shapeType" select="name(.)"/>
        <Descriptor xsi:type="Geometry3DType">            
            <Geometry3D>
                <xsl:attribute name="ObjectType">
                    <xsl:value-of select="$shapeType"/>
                </xsl:attribute>
                <xsl:if test="@DEF">
                    <xsl:attribute name="DEF">
                        <xsl:value-of select="@DEF"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:if test="@convex">
                    <xsl:attribute name="convex">
                        <xsl:value-of select="@convex"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:if test="@creaseAngle">
                    <xsl:attribute name="creaseAngle">
                        <xsl:value-of select="@creaseAngle"/>
                    </xsl:attribute>
                </xsl:if>             
                <!--DominantColor Descriptor-->
                <xsl:apply-templates select="..//Material" mode="DominantColor"/>                                                 
                <!--Shape3D Descriptor-->
                <xsl:if test="$shapeType = 'IndexedFaceSet'">
                    <xsl:variable name="positionOfIFS" select="count(preceding::IndexedFaceSet) + 1"/>
                    <xsl:call-template name="shapeExtraction">
                        <xsl:with-param name="stringOfIFS" select="tokenize($IFSPointsExtraction, '#')[$positionOfIFS]"/>
                        <xsl:with-param name="stringOfSGD" select="tokenize($SGD, '#')[$positionOfIFS]"/>                                          
                    </xsl:call-template>
                </xsl:if>
                <xsl:if test="$shapeType = 'IndexedLineSet'">                    
                    <xsl:variable name="positionOfILS" select="count(preceding::IndexedLineSet) + 1"/>
                    <xsl:call-template name="shapeExtraction">
                        <xsl:with-param name="stringOfIFS" select="tokenize($ILSPointsExtraction, '#')[$positionOfILS]"/>
                        <xsl:with-param name="stringOfSGD" select="tokenize($SGD, '#')[$positionOfILS]"/>
                    </xsl:call-template>
                </xsl:if>
                <xsl:if test="$shapeType = 'Extrusion'">                    
                    <xsl:variable name="positionOfExtr" select="count(preceding::Extrusion) + 1"/>
                    <xsl:call-template name="extrusionShapeExtraction">
                        <xsl:with-param name="stringOfExtr" select="tokenize($extrusionPointsExtraction, '#')[$positionOfExtr]"/>
                        <xsl:with-param name="stringOfSGD" select="tokenize($SGD, '#')[$positionOfExtr]"/>                    
                    </xsl:call-template>
                </xsl:if>
            </Geometry3D>
            <!--Metadata3D Descriptor-->
            <xsl:apply-templates select=".//*[contains(name(.),'Metadata')]" mode="MetadataDescriptor"/>             
        </Descriptor>
    </xsl:template>
    <xsl:template match="Material" mode="DominantColor">    
        <xsl:variable name="color">
            <xsl:choose>
                <xsl:when test="@diffuseColor">
                    <xsl:value-of select="@diffuseColor"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="'0.8 0.8 0.8'"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <DominantColor3D xsi:type="DominantColorType">                            
            <SpatialCoherency>
                <xsl:text>0</xsl:text>
            </SpatialCoherency>
            <Value>
                <Percentage>
                    <xsl:text>1</xsl:text>
                </Percentage>
                <Index>                    
                    <xsl:value-of select="ceiling((number(tokenize($color, ' ')[1])) * 255)"/>
                    <xsl:value-of select="' '"/>
                    <xsl:value-of select="ceiling((number(tokenize($color, ' ')[2])) * 255)"/>
                    <xsl:value-of select="' '"/>
                    <xsl:value-of select="ceiling((number(tokenize($color, ' ')[3])) * 255)"/>
                </Index>
            </Value>
        </DominantColor3D>    
    </xsl:template>
    <xsl:template match="//*[contains(name(.),'Metadata')]" mode="MetadataDescriptor">    
        <xsl:variable name="nodeName" select="name(.)"/>
        <Descriptor xsi:type="Metadata3DType">
            <xsl:if test="@name">
                <xsl:element name="name">
                    <xsl:value-of select="@name"/>
                </xsl:element>
            </xsl:if>
            <type>
                <xsl:value-of select="name(.)"/>
            </type>
            <value>
                <xsl:choose>
                    <xsl:when test="contains(name(.),'MetadataSet')">
                        <xsl:apply-templates select=".//*[contains(name(.),'Metadata')]" mode="MetadataSet"/>                                               
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="@value"/>
                    </xsl:otherwise>
                </xsl:choose>
            </value>
            <xsl:if test="@reference">
                <reference>
                    <xsl:value-of select="@reference"/>
                </reference>
            </xsl:if>
            <Ref>
                <xsl:text>/X3D/Scene</xsl:text>
                <xsl:for-each select="ancestor::*">
                    <xsl:value-of select="concat('/',name(.),'[',1+count(preceding-sibling::*[name(.)=$nodeName]),']')"/>
                </xsl:for-each>                        
                <xsl:for-each select="descendant-or-self::Shape">
                    <xsl:value-of select="concat('/',name(.),'[',1+count(preceding-sibling::*[name(.)=$nodeName]),']')"/>
                </xsl:for-each>
            </Ref>
        </Descriptor>                
    </xsl:template>
    
    <xsl:template match="//*[contains(name(.),'Metadata')]" mode="MetadataSet">
               
        <xsl:if test="@name">
            <xsl:element name="name">
                <xsl:value-of select="@name"/>
            </xsl:element>
        </xsl:if>
        <type>
            <xsl:value-of select="name(.)"/>
        </type>
        <value>
            <xsl:choose>
                <xsl:when test="contains(name(.),'MetadataSet')">
                    <xsl:apply-templates select=".//*[contains(name(.),'Metadata')]" mode="MetadataSet"/>                                                   
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@value"/>
                </xsl:otherwise>
            </xsl:choose>
        </value>
        <xsl:if test="@reference">
            <reference>
                <xsl:value-of select="@reference"/>
            </reference>
        </xsl:if>                           
    </xsl:template>
    <xsl:template match="ImageTexture">                
        <xsl:param name="position"/>
        <Descriptor xsi:type="EdgeHistogramType">            
            <BinCounts>                       
                <xsl:value-of select="tokenize(tokenize($EHDs,'#')[$position],':')[last()]"/>                
            </BinCounts>
        </Descriptor>
        <xsl:variable name="SCDescrs" select="tokenize($SCDs,'#')[$position]"/>
        <xsl:variable name="SCParts" select="tokenize(tokenize($SCDescrs,':')[last()],';')"/>
        <Descriptor xsi:type="ScalableColorType">            
            <xsl:attribute name="numOfCoeff" select="$SCParts[2]"/>
            <xsl:attribute name="numOfBitplanesDiscarded" select="$SCParts[1]"/>
            <Coeff>                    
                <xsl:value-of select="$SCParts[last()]"/>
            </Coeff>
        </Descriptor>
        <xsl:variable name="SURFDescrs" select="tokenize($SURF,'#')[$position]"/>
        <xsl:variable name="SURFParts" select="tokenize(tokenize($SURFDescrs,':')[last()],';')"/>        
        <Descriptor xsi:type="SURFeaturesType">               
            <Vocabulary size="{$SURFParts[1]}" location="{concat('Vocabularies/SURF/Vocab',$SURFParts[1],'.iVocab')}"/>
            <BagOfSURF>                       
                <xsl:value-of select="$SURFParts[last()]"/>                
            </BagOfSURF>
        </Descriptor>        
    </xsl:template>
</xsl:stylesheet>