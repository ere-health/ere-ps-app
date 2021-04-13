<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:fhir="http://hl7.org/fhir"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:math="http://www.w3.org/2005/xpath-functions/math"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pdf="http://xmlgraphics.apache.org/fop/extensions/pdf"
                xmlns:barcode="http://barcode4j.krysalis.org/ns"
                xsi:schemaLocation="http://www.w3.org/1999/XSL/Transform http://www.w3.org/2007/schema-for-xslt20.xsd http://www.w3.org/1999/XSL/Format https://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd">

    <xsl:decimal-format name="de" decimal-separator=',' grouping-separator='.'/>
    <xsl:variable name="root" select="/fhir:Bundle"/>

    <xsl:template match="fhir:Bundle">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format"
                 font-family="Verdana,Arial,Symbola" font-size="10pt" text-align="left"
                 line-height="normal" font-selection-strategy="character-by-character"
                 line-height-shift-adjustment="disregard-shifts" writing-mode="lr-tb"
                 language="DE">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="DIN-A5"
                                       page-width="148mm" page-height="105mm"
                                       margin-top="0mm" margin-bottom="0mm"
                                       margin-left="0mm" margin-right="0mm">
                    <fo:region-body region-name="body"
                                    margin-top="27mm" margin-bottom="17mm"
                                    margin-left="25mm" margin-right="10mm"/>
                    <fo:region-before region-name="header" extent="27mm"/>
                    <fo:region-after region-name="footer" extent="17mm"/>
                    <fo:region-start region-name="left" extent="25mm"/>
                    <fo:region-end region-name="right" extent="10mm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:declarations>
                <x:xmpmeta xmlns:x="adobe:ns:meta/">
                    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <rdf:Description xmlns:dc="http://purl.org/dc/elements/1.1/"
                                         rdf:about="">
                            <dc:title>E-Rezept</dc:title>
                            <dc:description></dc:description>
                        </rdf:Description>
                        <rdf:Description xmlns:pdf="http://ns.adobe.com/pdf/1.3/"
                                         rdf:about=""/>
                        <rdf:Description xmlns:xmp="http://ns.adobe.com/xap/1.0/"
                                         rdf:about="">
                            <xmp:CreatorTool>ere.health</xmp:CreatorTool>
                        </rdf:Description>
                    </rdf:RDF>
                </x:xmpmeta>
                <!-- <pdf:embedded-file filename="Bundle.xml"
                                   description="Embedded Bundle XML">
                     <xsl:attribute name="src">
                        url(<xsl:value-of select="$bundleFileUrl"/>)
                    </xsl:attribute>
                </pdf:embedded-file> -->
            </fo:declarations>
            <fo:page-sequence master-reference="DIN-A5"
                              initial-page-number="1">
                <fo:static-content flow-name="header">
                    <xsl:call-template name="header"/>
                </fo:static-content>
                <fo:static-content flow-name="footer">
                    <xsl:call-template name="footer"/>
                </fo:static-content>
                <fo:static-content flow-name="left">
                    <xsl:call-template name="left"/>
                </fo:static-content>
                <fo:flow flow-name="body">
                    <xsl:call-template name="body"/>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template name="header">
        <fo:table table-layout="fixed" width="100%" >
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:block padding-top="10mm" line-height="0mm">
                            <fo:external-graphic content-height="8mm" content-width="scale-to-fit"
                                                 src="url('img/logo.svg')"/>
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block padding-top="10mm"/>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block padding-top="10mm"  line-height="0mm" text-align="right">
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="footer">
        <fo:table table-layout="fixed" width="100%" font-size="8pt" margin-right="10mm">
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:block/>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block/>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block text-align="right" >
                            Seite:
                            <xsl:text> </xsl:text>
                            <fo:page-number/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="left">
        <fo:block-container absolute-position="fixed"
                            top="87mm" left="1mm" width="6mm" border-top-width="0.2mm"
                            border-top-style="solid" border-top-color="black">
            <fo:block/>
        </fo:block-container>
        <fo:block-container absolute-position="fixed"
                            top="148.5mm" left="1mm" width="9mm" border-top-width="0.2mm"
                            border-top-style="solid" border-top-color="black">
            <fo:block/>
        </fo:block-container>
        <fo:block-container absolute-position="fixed"
                            top="192mm" left="1mm" width="6mm" border-top-width="0.2mm"
                            border-top-style="solid" border-top-color="black">
            <fo:block/>
        </fo:block-container>
    </xsl:template>

    <xsl:template name="body">
        <fo:block>
            <xsl:value-of select="//fhir:Patient/fhir:name/fhir:given/@value" />
            <xsl:value-of select="//fhir:Patient/fhir:name/fhir:family/@value" />
            <fo:block>
                <fo:instream-foreign-object>
                    <barcode:barcode
                        message="my message" orientation="90">
                        <barcode:datamatrix>
                            <barcode:height>8mm</barcode:height>
                        </barcode:datamatrix>
                    </barcode:barcode>
                </fo:instream-foreign-object>
            </fo:block>
        </fo:block>
        <fo:block id="end"/>
    </xsl:template>

</xsl:stylesheet>
