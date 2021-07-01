<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:fhir="http://hl7.org/fhir"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:pdf="http://xmlgraphics.apache.org/fop/extensions/pdf"
                xmlns:barcode="http://barcode4j.krysalis.org/ns"
                xmlns:fox="http://xmlgraphics.apache.org/fop/extensions"
                xsi:schemaLocation="http://www.w3.org/1999/XSL/Transform http://www.w3.org/2007/schema-for-xslt20.xsd http://www.w3.org/1999/XSL/Format https://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd">

    <xsl:decimal-format name="de" decimal-separator=',' grouping-separator='.'/>

    <xsl:param name="bundleFileUrl"/>

    <xsl:template match="fhir:root">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format"
                 font-family="Courier, Arial" font-size="10pt" text-align="left"
                 line-height="normal" font-selection-strategy="character-by-character"
                 line-height-shift-adjustment="disregard-shifts" writing-mode="lr-tb"
                 language="DE">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="DIN-A5" column-count="2"
                                       page-width="210mm" page-height="148mm"
                                       margin-top="5mm" margin-bottom="5mm"
                                       margin-left="8mm" margin-right="5mm">
                    <fo:region-body region-name="body"
                                    margin-top="60mm" margin-bottom="0mm"
                                    margin-left="2mm" margin-right="5mm"/>
                    <fo:region-before region-name="header" extent="55mm"/>
                    <fo:region-after region-name="footer" extent="50mm"/>
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
                <pdf:embedded-file filename="Bundles.xml" description="Embedded Bundles XML">
                    <xsl:attribute name="src">
                        url(<xsl:value-of select="$bundleFileUrl"/>)
                    </xsl:attribute>
                </pdf:embedded-file>
            </fo:declarations>
            <fo:page-sequence master-reference="DIN-A5" initial-page-number="1">
                <fo:static-content flow-name="header">
                    <xsl:call-template name="header"/>
                </fo:static-content>
                <fo:static-content flow-name="footer">
                    <xsl:call-template name="footer"/>
                </fo:static-content>
                <fo:flow flow-name="body">
                    <xsl:call-template name="body"/>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template name="formatDate">
        <xsl:param name="date"/>
        <xsl:variable name="year" select="substring-before($date, '-')"/>
        <xsl:variable name="month" select="substring-before(substring-after($date, '-'), '-')"/>
        <xsl:variable name="day" select="substring-after(substring-after($date, '-'), '-')"/>
        <xsl:value-of select="concat($day, '.', $month, '.', $year)"/>
    </xsl:template>

    <xsl:template name="footer">
        <fo:block text-align="end">
            <fo:external-graphic content-height="41mm" content-width="scale-to-fit"
                                 src="url('img/erezept-app-note.svg')"/>
        </fo:block>
    </xsl:template>

    <xsl:template name="header">
        <fo:table>
            <fo:table-column column-number="1" column-width="70%"/>
            <fo:table-column column-number="2" column-width="2%"/>
            <fo:table-column column-number="3" column-width="28%"/>
            <fo:table-body>
                <fo:table-cell>
                    <fo:table border-separation="1mm" fox:border-radius="3mm"
                              border-collapse="separate">
                        <fo:table-body>
                            <fo:table-row height="5mm">
                                <fo:table-cell number-columns-spanned="2">
                                    <fo:block font-family="Arial" font-weight="bold" font-size="12pt">
                                        Ausdruck zur Einlösung Ihres E-Rezeptes
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell number-columns-spanned="2" fox:border-radius="1mm"
                                               border="solid 0.5pt black">
                                    <fo:table>
                                        <fo:table-column/>
                                        <fo:table-column/>
                                        <fo:table-header>
                                            <fo:table-row>
                                                <fo:table-cell>
                                                    <fo:block font-size="6pt" font-weight="bold" margin-left="1mm"
                                                              font-family="Arial">für
                                                    </fo:block>
                                                </fo:table-cell>
                                                <fo:table-cell>
                                                    <fo:block font-size="6pt" font-weight="bold" margin-left="40mm"
                                                              font-family="Arial">geboren am
                                                    </fo:block>
                                                </fo:table-cell>
                                            </fo:table-row>
                                        </fo:table-header>
                                        <fo:table-body>
                                            <fo:table-row>
                                                <fo:table-cell width="60mm">
                                                    <fo:block margin-left="1mm" font-size="10pt" font-weight="bold">
                                                        <xsl:value-of
                                                                select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:prefix/@value"/>
                                                        <xsl:text>  </xsl:text>
                                                        <xsl:value-of
                                                                select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:given/@value"/>
                                                        <xsl:text>  </xsl:text>
                                                        <xsl:value-of
                                                                select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:family/@value"/>
                                                    </fo:block>
                                                </fo:table-cell>
                                                <fo:table-cell>
                                                    <fo:block margin-left="40mm" font-weight="bold">
                                                        <xsl:call-template name="formatDate">
                                                            <xsl:with-param name="date" select="
                                                    fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:birthDate/@value"/>
                                                        </xsl:call-template>
                                                    </fo:block>
                                                </fo:table-cell>
                                            </fo:table-row>
                                        </fo:table-body>
                                    </fo:table>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row height="4mm">
                                <fo:table-cell>
                                    <fo:block/>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell number-columns-spanned="2" fox:border-radius="1mm"
                                               border="solid 0.5pt black">
                                    <fo:table>
                                        <fo:table-column/>
                                        <fo:table-column/>
                                        <fo:table-header>
                                            <fo:table-row>
                                                <fo:table-cell>
                                                    <fo:block font-size="6pt" font-weight="bold" margin-left="1mm"
                                                              font-family="Arial">
                                                        ausgestellt von
                                                    </fo:block>
                                                </fo:table-cell>
                                                <fo:table-cell>
                                                    <fo:block font-size="6pt" font-weight="bold" margin-left="10mm"
                                                              font-family="Arial">
                                                        ausgestellt am
                                                    </fo:block>
                                                </fo:table-cell>
                                            </fo:table-row>
                                        </fo:table-header>
                                        <fo:table-body>
                                            <fo:table-row height="20mm">
                                                <fo:table-cell width="90mm" margin-left="1mm">
                                                    <fo:block font-size="10pt" font-weight="bold">
                                                        <xsl:value-of
                                                                select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:prefix/@value"/>
                                                        <xsl:text>  </xsl:text>
                                                        <xsl:value-of
                                                                select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:given/@value"/>
                                                        <xsl:text>  </xsl:text>
                                                        <xsl:value-of
                                                                select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:family/@value"/>
                                                    </fo:block>
                                                    <fo:block font-size="10pt" font-weight="bold">
                                                        <xsl:value-of
                                                                select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization/fhir:name/@value"/>
                                                    </fo:block>
                                                    <xsl:for-each
                                                            select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization/fhir:telecom">
                                                        <fo:block font-size="10pt" font-weight="bold">
                                                            <xsl:value-of select="fhir:value/@value"/>
                                                        </fo:block>
                                                    </xsl:for-each>
                                                </fo:table-cell>
                                                <fo:table-cell>
                                                    <fo:block margin-left="10mm" font-weight="bold">
                                                        <xsl:variable name="authoredOn" select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:authoredOn/@value" />
                                                        <xsl:for-each select="tokenize($authoredOn, 'T')">
                                                            <xsl:if test="position()=1">
                                                                <xsl:call-template name="formatDate">
                                                                    <xsl:with-param name="date" select="."/>
                                                                </xsl:call-template>
                                                            </xsl:if>
                                                        </xsl:for-each>
                                                    </fo:block>
                                                </fo:table-cell>
                                            </fo:table-row>
                                        </fo:table-body>
                                    </fo:table>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>
                </fo:table-cell>
                <fo:table-cell display-align="after">
                    <fo:block-container reference-orientation="90" margin-left="2mm">
                        <fo:block font-size="6pt" font-family="Arial" font-weight="bold" wrap-option="no-wrap">
                            Sammelcode zur Einlösung aller Verordnungen
                        </fo:block>
                    </fo:block-container>
                </fo:table-cell>
                <fo:table-cell display-align="after">
                    <fo:block margin-left="2mm">
                        <fo:instream-foreign-object>
                            <barcode:barcode>
                                <xsl:attribute name="message">
                                    <xsl:variable name="bundles" select="fhir:bundle"/>{"urls": [<xsl:for-each select="fhir:bundle">
                                        <xsl:variable name="qrPos" select="position()"/>
                                        <xsl:variable name="bundlesCount" select="count($bundles)"/>"Task/<xsl:value-of
                                            select="fhir:Bundle/fhir:identifier/fhir:value/@value"/>/$accept?ac=<xsl:value-of
                                            select="fhir:accessCode"/>"<xsl:if test="$qrPos &lt; $bundlesCount">,</xsl:if>
                                    </xsl:for-each>]}
                                </xsl:attribute>
                                <barcode:datamatrix>
                                    <barcode:module-width>0.6mm</barcode:module-width>
                                </barcode:datamatrix>
                            </barcode:barcode>
                        </fo:instream-foreign-object>
                    </fo:block>
                </fo:table-cell>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="body">
        <fo:table table-layout="fixed" width="190mm">
            <fo:table-column column-number="1" column-width="50%"/>
            <fo:table-column column-number="2" column-width="50%"/>
            <fo:table-body>
                <xsl:for-each select="fhir:bundle">
                    <xsl:variable name="pos" select="position()"/>
                    <fo:table-cell>
                        <xsl:if test="not(($pos mod 3) mod 2) or not(($pos mod 3) mod 3)">
                            <xsl:attribute name="ends-row">true</xsl:attribute>
                        </xsl:if>
                        <fo:table border-collapse="separate">
                            <fo:table-column/>
                            <fo:table-column/>
                            <fo:table-body>
                                <fo:table-row height="35mm">
                                    <fo:table-cell width="32mm">
                                        <fo:block>
                                            <fo:instream-foreign-object>
                                                <barcode:barcode>
                                                    <xsl:attribute name="message">
                                                        {"urls":["Task/<xsl:value-of
                                                            select="fhir:Bundle/fhir:identifier/fhir:value/@value"/>/$accept?ac=<xsl:value-of
                                                            select="fhir:accessCode"/>"]}
                                                    </xsl:attribute>
                                                    <barcode:datamatrix>
                                                        <barcode:module-width>0.6mm</barcode:module-width>
                                                    </barcode:datamatrix>
                                                </barcode:barcode>
                                            </fo:instream-foreign-object>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block margin-top="3mm">
                                            <fo:block font-weight="bold">
                                                <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:dispenseRequest/fhir:quantity/fhir:value/@value"/>
                                                <xsl:text>x </xsl:text>
                                                <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:text/@value"/>
                                            </fo:block>
                                            <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:amount/fhir:numerator/fhir:value/@value > 0">
                                                <fo:block font-weight="bold">
                                                    <xsl:value-of
                                                            select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:amount/fhir:numerator/fhir:unit/@value"/>
                                                    <xsl:text> / </xsl:text>
                                                    <xsl:value-of
                                                            select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:amount/fhir:numerator/fhir:value/@value"/>
                                                    <xsl:text> St</xsl:text>
                                                </fo:block>
                                            </xsl:if>
                                            <fo:block>
                                                <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:dosageInstruction/fhir:text/@value"/>
                                            </fo:block>
                                            <fo:block>
                                                PZN:
                                                <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:coding/fhir:code/@value"/>
                                            </fo:block>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>
                    </fo:table-cell>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>
    </xsl:template>
</xsl:stylesheet>
