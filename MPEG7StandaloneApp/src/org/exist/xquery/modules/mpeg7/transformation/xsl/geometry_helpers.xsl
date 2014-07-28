<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:mpeg:mpeg7:schema:2001" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001" xmlns:str="http://exslt.org/strings" xmlns:functx="http://www.functx.com" xmlns:math="http://exslt.org/math" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template name="pointsCalculationX">
        <xsl:param name="count"/>
        <xsl:param name="count2"/>
        <xsl:param name="Xpoint"/>
        <xsl:param name="XpointMax" select="$Xpoint"/>
        <xsl:choose>
            <xsl:when test="$count &gt; 1">
                <xsl:variable name="minXpoint" select="min(($Xpoint,number(tokenize($count2, ' ')[$count])))"/>
                <xsl:variable name="maxXpoint" select="max(($XpointMax,number(tokenize($count2, ' ')[$count])))"/>
                <xsl:call-template name="pointsCalculationX">
                    <xsl:with-param name="count" select="$count - 3"/>
                    <xsl:with-param name="count2" select="$count2"/>
                    <xsl:with-param name="Xpoint" select="$minXpoint"/>
                    <xsl:with-param name="XpointMax" select="$maxXpoint"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="XpointMinAndMax">
                    <xsl:value-of select="min(($Xpoint,number(tokenize($count2, ' ')[$count])))"/>
                    <xsl:value-of select="' '"/>
                    <xsl:value-of select="max(($XpointMax,number(tokenize($count2, ' ')[$count])))"/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="pointsCalculationY">
        <xsl:param name="count"/>
        <xsl:param name="count2"/>
        <xsl:param name="Ypoint"/>
        <xsl:param name="YpointMax" select="$Ypoint"/>
        <xsl:choose>
            <xsl:when test="$count &gt; 2">
                <xsl:variable name="minYpoint" select="min(($Ypoint,number(tokenize($count2, ' ')[$count])))"/>
                <xsl:variable name="maxYpoint" select="max(($YpointMax,number(tokenize($count2, ' ')[$count])))"/>
                <xsl:call-template name="pointsCalculationY">
                    <xsl:with-param name="count" select="$count - 3"/>
                    <xsl:with-param name="count2" select="$count2"/>
                    <xsl:with-param name="Ypoint" select="$minYpoint"/>
                    <xsl:with-param name="YpointMax" select="$maxYpoint"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="YpointMINAndMAX">
                    <xsl:value-of select="min(($Ypoint,number(tokenize($count2, ' ')[$count])))"/>
                    <xsl:value-of select="' '"/>
                    <xsl:value-of select="max(($YpointMax,number(tokenize($count2, ' ')[$count])))"/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="pointsCalculationZ">
        <xsl:param name="count"/>
        <xsl:param name="count2"/>
        <xsl:param name="Zpoint"/>
        <xsl:param name="ZpointMax" select="$Zpoint"/>
        <xsl:choose>
            <xsl:when test="$count &gt; 3">
                <xsl:variable name="minZpoint" select="min(($Zpoint,number(tokenize($count2, ' ')[$count])))"/>
                <xsl:variable name="maxZpoint" select="max(($ZpointMax,number(tokenize($count2, ' ')[$count])))"/>
                <xsl:call-template name="pointsCalculationZ">
                    <xsl:with-param name="count" select="$count - 3"/>
                    <xsl:with-param name="count2" select="$count2"/>
                    <xsl:with-param name="Zpoint" select="$minZpoint"/>
                    <xsl:with-param name="ZpointMax" select="$maxZpoint"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="ZpointMINAndMAX">
                    <xsl:value-of select="min(($Zpoint,number(tokenize($count2, ' ')[$count])))"/>
                   <xsl:value-of select="' '"/>
                    <xsl:value-of select="max(($ZpointMax,number(tokenize($count2, ' ')[$count])))"/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="shapeExtraction">
        <xsl:param name="stringOfIFS"/>
        <xsl:param name="stringOfSGD"/>        
        <xsl:variable name="countPoints" select="count(tokenize($stringOfIFS, ' '))"/>
        <xsl:element name="Shape3D">
            <xsl:attribute name="xsi:type">
                <xsl:text>Shape3DType</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="bitsPerBin">
                <xsl:value-of select="tokenize($stringOfIFS, ' ')[1]"/>
            </xsl:attribute>
            <xsl:element name="Spectrum">
                <xsl:variable name="subtractLastPoint">
                    <xsl:call-template name="substring-before-last">
                        <xsl:with-param name="allIfsString" select="substring-after($stringOfIFS, ' ')"/>
                        <xsl:with-param name="delimiter" select="' '"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:call-template name="substring-before-last">
                    <xsl:with-param name="allIfsString" select="$subtractLastPoint"/>
                    <xsl:with-param name="delimiter" select="' '"/>
                </xsl:call-template>
            </xsl:element>
            <xsl:element name="PlanarSurfaces">
                <xsl:value-of select="tokenize($stringOfIFS, ' ')[$countPoints - 1]"/>
            </xsl:element>
            <xsl:element name="SingularSurfaces">
                <xsl:value-of select="tokenize($stringOfIFS, ' ')[$countPoints]"/>
            </xsl:element>
        </xsl:element>
         
        <xsl:variable name="SGDParts" select="tokenize($stringOfSGD,';')"/>        
        <ShapeGoogle xsi:type="ShapeGoogleType">   
                    
            <Vocabulary size="{$SGDParts[1]}" location="{concat('vocab',$SGDParts[1],'.mat')}"/>
            <BagOfFeatures>                       
                <xsl:value-of select="$SGDParts[2]"/>                
            </BagOfFeatures>
            <SpatiallySensitiveBagOfFeatures>                       
                <xsl:value-of select="$SGDParts[last()]"/>                
            </SpatiallySensitiveBagOfFeatures>
        </ShapeGoogle>  
    </xsl:template>
    <xsl:template name="extrusionShapeExtraction">
        <xsl:param name="stringOfExtr"/>
        <xsl:param name="stringOfSGD"/>           
        <xsl:variable name="countPoints" select="count(tokenize($stringOfExtr, ' '))"/>
        <xsl:element name="Shape3D">
            <xsl:attribute name="xsi:type">
                <xsl:text>Shape3DType</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="bitsPerBin">
                <xsl:value-of select="tokenize($stringOfExtr, ' ')[1]"/>
            </xsl:attribute>
            <xsl:element name="Spectrum">
                <xsl:variable name="subtractLastPoint">
                    <xsl:call-template name="substring-before-last">
                        <xsl:with-param name="allIfsString" select="substring-after($stringOfExtr, ' ')"/>
                        <xsl:with-param name="delimiter" select="' '"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:call-template name="substring-before-last">
                    <xsl:with-param name="allIfsString" select="$subtractLastPoint"/>
                    <xsl:with-param name="delimiter" select="' '"/>
                </xsl:call-template>
            </xsl:element>
            <xsl:element name="PlanarSurfaces">
                <xsl:value-of select="tokenize($stringOfExtr, ' ')[$countPoints - 1]"/>
            </xsl:element>
            <xsl:element name="SingularSurfaces">
                <xsl:value-of select="tokenize($stringOfExtr, ' ')[$countPoints]"/>
            </xsl:element>
        </xsl:element>
        <xsl:variable name="SGDParts" select="tokenize($stringOfSGD,';')"/>        
        <ShapeGoogle xsi:type="ShapeGoogleType">                       
            <Vocabulary size="{$SGDParts[1]}" location="{concat('vocab',$SGDParts[1],'.mat')}"/>
            <BagOfFeatures>                       
                <xsl:value-of select="$SGDParts[2]"/>                
            </BagOfFeatures>
            <SpatiallySensitiveBagOfFeatures>                       
                <xsl:value-of select="$SGDParts[last()]"/>                
            </SpatiallySensitiveBagOfFeatures>
        </ShapeGoogle>  
    </xsl:template>
    <xsl:template name="extrusionBBoxTemplate">
        <xsl:param name="stringOfExtrBBox"/>
        <xsl:element name="BoundingBox3DSize">
            <xsl:attribute name="BoxWidth">
                <xsl:value-of select="tokenize($stringOfExtrBBox, ' ')[1]"/>
            </xsl:attribute>
            <xsl:attribute name="BoxHeight">
                <xsl:value-of select="tokenize($stringOfExtrBBox, ' ')[2]"/>
            </xsl:attribute>
            <xsl:attribute name="BoxDepth">
                <xsl:value-of select="tokenize($stringOfExtrBBox, ' ')[3]"/>
            </xsl:attribute>
        </xsl:element>
        <xsl:element name="BoundingBox3DCenter">
            <xsl:attribute name="BoxCenterW">
                <xsl:value-of select="tokenize($stringOfExtrBBox, ' ')[4]"/>
            </xsl:attribute>
            <xsl:attribute name="BoxCenterH">
                <xsl:value-of select="tokenize($stringOfExtrBBox, ' ')[5]"/>
            </xsl:attribute>
            <xsl:attribute name="BoxCenterD">
                <xsl:value-of select="tokenize($stringOfExtrBBox, ' ')[6]"/>
            </xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template name="substring-before-last">
        <xsl:param name="allIfsString"/>
        <xsl:param name="delimiter"/>
        <xsl:choose>
            <xsl:when test="contains($allIfsString, $delimiter)">
                <xsl:value-of select="substring-before($allIfsString, $delimiter)"/>
                <xsl:choose>
                    <xsl:when test="contains(substring-after($allIfsString, $delimiter), $delimiter)">
                        <xsl:value-of select="$delimiter"/>
                    </xsl:when>
                </xsl:choose>
                <xsl:call-template name="substring-before-last">
                    <xsl:with-param name="allIfsString" select="substring-after($allIfsString, $delimiter)"/>
                    <xsl:with-param name="delimiter" select="$delimiter"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>