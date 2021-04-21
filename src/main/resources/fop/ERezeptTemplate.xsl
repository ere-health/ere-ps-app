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
                xmlns:fox="http://xmlgraphics.apache.org/fop/extensions"
                xsi:schemaLocation="http://www.w3.org/1999/XSL/Transform http://www.w3.org/2007/schema-for-xslt20.xsd http://www.w3.org/1999/XSL/Format https://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd">

    <xsl:decimal-format name="de" decimal-separator=',' grouping-separator='.'/>
    <xsl:variable name="root" select="/fhir:Bundle"/>

    <xsl:param name="bundleFileUrl"/>

    <xsl:template match="fhir:Bundle">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format"
                 font-family="Verdana,Arial,Symbola" font-size="6pt" text-align="left"
                 line-height="normal" font-selection-strategy="character-by-character"
                 line-height-shift-adjustment="disregard-shifts" writing-mode="lr-tb"
                 language="DE">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="DIN-A5"
                                       page-width="148mm" page-height="105mm"
                                       margin-top="5mm" margin-bottom="5mm"
                                       margin-left="10mm" margin-right="10mm">
                    <fo:region-body region-name="body"
                                    margin-top="50mm" margin-bottom="0mm"
                                    margin-left="0mm" margin-right="0mm"/>
                    <fo:region-before region-name="header" extent="55mm"/>
                    <fo:region-after region-name="footer" extent="15mm"/>
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
                <pdf:embedded-file filename="Bundle.xml"
                                   description="Embedded Bundle XML">
                     <xsl:attribute name="src">
                        url(<xsl:value-of select="$bundleFileUrl"/>)
                    </xsl:attribute>
                </pdf:embedded-file>
            </fo:declarations>
            <fo:page-sequence master-reference="DIN-A5"
                              initial-page-number="1">
                <fo:static-content flow-name="header">
                    <xsl:call-template name="header" />
                </fo:static-content>
                <fo:static-content flow-name="footer">
                    <xsl:call-template name="footer" />
                </fo:static-content>
                <fo:flow flow-name="body">
                    <xsl:call-template name="body" />
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template name="header">
        <fo:table table-layout="fixed" border-separation="1mm" fox:border-radius="3mm" width="100%" border-collapse="separate">
            <fo:table-body>
                <fo:table-row height="5mm">
                    <fo:table-cell width="123mm" number-columns-spanned="3">
                        <fo:block font-weight="bold" font-size="10pt">
                            Ausdruck zur Einlösung Ihrer E-Verordnung 
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell width="auto">
                        <fo:block text-align="end">
                            <fo:external-graphic content-height="3mm" content-width="scale-to-fit"
                                                    src="url('img/logo.svg')"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row height="4mm">
                    <fo:table-cell number-columns-spanned="4">
                        <fo:block font-size="4pt" margin-top="2mm">Versicherte Person</fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell number-columns-spanned="3" fox:border-radius="1mm" border="solid 0.5pt black" padding="1mm">
                        <fo:block font-size="6pt" font-weight="bold">
                            <xsl:value-of select="//fhir:Patient/fhir:name/fhir:prefix/@value" />
                            <xsl:value-of select="//fhir:Patient/fhir:name/fhir:given/@value" />
                            <xsl:value-of select="//fhir:Patient/fhir:name/fhir:family/@value" />
                        </fo:block>
                        <fo:block text-align="end">
                            geb. am <xsl:value-of select="//fhir:Patient/fhir:birthDate/@value" />
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell width="auto" number-rows-spanned="5">
                        <fo:block margin-left="12mm">
                            <fo:instream-foreign-object>
                                <barcode:barcode>
                                    <xsl:attribute name="message">
                                        <xsl:value-of select="//fhir:MedicationRequest/fhir:id/@value" />
                                    </xsl:attribute>
                                    <barcode:datamatrix>
                                        <barcode:module-width>1mm</barcode:module-width>
                                    </barcode:datamatrix>
                                </barcode:barcode>
                            </fo:instream-foreign-object>
                        </fo:block>
                        <fo:block margin-left="12mm" font-size="4pt">
                            Sammelcode für alle Verordnungen
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row height="4mm">
                    <fo:table-cell number-columns-spanned="3">
                        <fo:block margin-top="2mm" font-size="4pt">Austellende Person</fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell number-columns-spanned="3" fox:border-radius="1mm" border="solid 0.3pt black" padding="1mm">
                        <fo:block font-size="6pt" font-weight="bold">
                            <xsl:value-of select="//fhir:Practitioner/fhir:name/fhir:prefix/@value" />
                            <xsl:value-of select="//fhir:Practitioner/fhir:name/fhir:given/@value" />
                            <xsl:value-of select="//fhir:Practitioner/fhir:name/fhir:family/@value" />
                        </fo:block>
                        <fo:block>
                            <xsl:value-of select="//fhir:Practitioner/fhir:qualification/fhir:code/fhir:text" />
                        </fo:block>
                        <fo:block>
                            Tel. <xsl:value-of select="//fhir:Organization/fhir:telecom/fhir:value/@value" />
                        </fo:block>
                        <fo:block text-align="end">
                            ausgestellt am <xsl:value-of select="//fhir:MedicationRequest/fhir:authoredOn/@value" />
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row height="2mm">
                    <fo:table-cell><fo:block margin-top="2mm" font-size="4pt">Gültig von - bis</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block /></fo:table-cell>
                    <fo:table-cell><fo:block margin-top="2mm" font-size="4pt">Krankenkasse</fo:block></fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell fox:border-radius="1mm" border="solid 0.5pt black" padding="1mm">
                        <fo:block>
                            <xsl:value-of select="//fhir:MedicationRequest/fhir:authoredOn/@value" /> + 1 Monat
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell fox:border-radius="1mm" border="solid 0.5pt black" padding="1mm">
                        <fo:block>
                            Gebührenpflichtig
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell fox:border-radius="1mm" border="solid 0.5pt black" padding="1mm">
                        <fo:block>
                            <xsl:value-of select="//fhir:Coverage/fhir:payor/fhir:display/@value" />
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>     
    </xsl:template>

    <xsl:template name="footer">
        <fo:block>Mehr Information auf www.das-e-rezept-fuer-deutschland.de oder telefonisch werktags unter 030/800XXXXXX</fo:block>
    </xsl:template>

    <xsl:template name="body">
        <xsl:for-each select="//fhir:Medication">
            <fo:table width="50%">
                <fo:table-body>
                    <fo:table-row>
                        <fo:table-cell width="20mm"><fo:block>
                            <fo:instream-foreign-object>
                                <barcode:barcode>
                                    <xsl:attribute name="message">
                                        <xsl:value-of select="fhir:id/@value" />
                                    </xsl:attribute>
                                    <barcode:datamatrix>
                                        <barcode:module-width>0.8mm</barcode:module-width>
                                    </barcode:datamatrix>
                                </barcode:barcode>
                            </fo:instream-foreign-object>
                        </fo:block></fo:table-cell>
                        <fo:table-cell>
                            <fo:block>
                                <fo:block font-weight="bold"><xsl:value-of select="fhir:code/fhir:text/@value" /></fo:block>
                                PZN: <xsl:value-of select="fhir:code/fhir:coding/fhir:code/@value" />
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table> 
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
