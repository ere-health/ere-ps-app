<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:fhir="http://hl7.org/fhir"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:pdf="http://xmlgraphics.apache.org/fop/extensions/pdf"
                xmlns:barcode="http://barcode4j.krysalis.org/ns"
                xmlns:fox="http://xmlgraphics.apache.org/fop/extensions"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xsi:schemaLocation="http://www.w3.org/1999/XSL/Transform http://www.w3.org/2007/schema-for-xslt20.xsd http://www.w3.org/1999/XSL/Format https://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd">

    <xsl:decimal-format name="de" decimal-separator=',' grouping-separator='.'/>

    <xsl:param name="bundleFileUrl"/>

    <xsl:template match="fhir:root">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format"
                 font-family="Courier, Liberation Sans" font-size="12pt" text-align="left"
                 line-height="normal" font-selection-strategy="character-by-character"
                 line-height-shift-adjustment="disregard-shifts" writing-mode="lr-tb"
                 language="DE">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="DIN-A5" column-count="2"
                                       page-width="210mm" page-height="148mm"
                                       margin-top="5mm" margin-bottom="0mm"
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
                                 src="classpath:/fop/img/erezept-app-note.svg"/>
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
                                    <fo:block font-family="Liberation Sans" font-weight="bold" font-size="12pt">
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
                                                <fo:table-cell width="100mm">
                                                    <fo:block font-size="6pt" margin-left="1mm"
                                                              font-family="Liberation Sans" font-weight="bold">für
                                                    </fo:block>
                                                </fo:table-cell>
                                                <fo:table-cell>
                                                    <fo:block font-size="6pt"
                                                              font-family="Liberation Sans" margin-left="5mm" font-weight="bold">geboren am
                                                    </fo:block>
                                                </fo:table-cell>
                                            </fo:table-row>
                                        </fo:table-header>
                                        <fo:table-body>
                                            <fo:table-row>
                                                <fo:table-cell width="100mm">
                                                    <fo:block margin-left="1mm">
                                                        <xsl:choose>
                                                            <xsl:when test="(string-length(fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:given/@value) + string-length(fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:family/@value)) &gt; 75">
                                                                <xsl:attribute name="font-size">10pt</xsl:attribute>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <xsl:attribute name="font-size">12pt</xsl:attribute>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                        <xsl:choose>
                                                            <xsl:when test="(string-length(fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:given/@value) + string-length(fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:family/@value)) &gt; 85">
                                                                <xsl:value-of
                                                                        select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:given/@value"/>
                                                                <xsl:text>  </xsl:text>
                                                                <xsl:value-of
                                                                        select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:family/fhir:extension[@url='http://hl7.org/fhir/StructureDefinition/humanname-own-name']/fhir:valueString/@value"/>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <xsl:value-of
                                                                        select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:prefix/@value"/>
                                                                <xsl:text>  </xsl:text>
                                                                <xsl:value-of
                                                                        select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:given/@value"/>
                                                                <xsl:text>  </xsl:text>
                                                                <xsl:value-of
                                                                        select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:family/@value"/>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </fo:block>
                                                </fo:table-cell>
                                                <fo:table-cell>
                                                    <fo:block margin-left="5mm">
                                                        <xsl:call-template name="formatDate">
                                                            <xsl:with-param name="date" select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:birthDate/@value"/>
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
                                                    <fo:block font-size="6pt" margin-left="1mm"
                                                              font-family="Liberation Sans" font-weight="bold">
                                                        ausgestellt von
                                                    </fo:block>
                                                </fo:table-cell>
                                                <fo:table-cell>
                                                    <fo:block font-size="6pt" margin-left="5mm"
                                                              font-family="Liberation Sans" font-weight="bold">
                                                        ausgestellt am
                                                    </fo:block>
                                                </fo:table-cell>
                                            </fo:table-row>
                                        </fo:table-header>
                                        <fo:table-body>
                                            <fo:table-row height="20mm">
                                                <fo:table-cell width="100mm" margin-left="1mm">
                                                    <xsl:variable
                                                            name="author"
                                                            select="fn:tokenize(fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:author/fhir:reference/@value, '/')[last()]" />
                                                    <xsl:for-each select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner">
                                                        <xsl:if test="$author = fhir:id/@value">
                                                            <fo:block font-size="12pt">
                                                                <xsl:value-of
                                                                        select="fhir:name/fhir:prefix/@value"/>
                                                                <xsl:text>  </xsl:text>
                                                                <xsl:value-of
                                                                        select="fhir:name/fhir:given/@value"/>
                                                                <xsl:text>  </xsl:text>
                                                                <xsl:value-of
                                                                        select="fhir:name/fhir:family/@value"/>
                                                            </fo:block>
                                                        </xsl:if>
                                                    </xsl:for-each>
                                                    <xsl:for-each
                                                            select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization">
                                                        <fo:block font-size="12pt">
                                                            <xsl:value-of
                                                                select="fhir:name/@value"/>
                                                        </fo:block>
                                                    </xsl:for-each>
                                                    <xsl:for-each
                                                            select="fhir:bundle[1]/fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization/fhir:telecom">
                                                        <xsl:if test="fhir:system/@value = 'phone' or fhir:system/@value = 'email'">
                                                            <fo:block font-size="12pt">
                                                                <xsl:value-of select="fhir:value/@value"/>
                                                            </fo:block>
                                                        </xsl:if>
                                                    </xsl:for-each>
                                                </fo:table-cell>
                                                <fo:table-cell>
                                                    <fo:block margin-left="5mm">
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
                        <fo:block font-size="6pt" font-family="Liberation Sans" font-weight="bold" wrap-option="no-wrap">
                            Sammelcode zur Einlösung aller Verordnungen
                        </fo:block>
                    </fo:block-container>
                </fo:table-cell>
                <fo:table-cell display-align="after">
                    <fo:block margin-left="2.5mm">
                        <fo:instream-foreign-object>
                            <barcode:barcode>
                                <xsl:attribute name="message"><xsl:variable name="bundles" select="fhir:bundle"/>{"urls":[<xsl:for-each select="fhir:bundle"><xsl:variable name="qrPos" select="position()"/><xsl:variable name="bundlesCount" select="count($bundles)"/>"Task/<xsl:value-of
                                            select="fhir:Bundle/fhir:identifier/fhir:value/@value"/>/$accept?ac=<xsl:value-of
                                            select="fhir:accessCode"/>"<xsl:if test="$qrPos &lt; $bundlesCount">,</xsl:if></xsl:for-each>]}</xsl:attribute>
                                <barcode:datamatrix>
                                    <barcode:module-width>0.52mm</barcode:module-width>
                                    <barcode:min-symbol-size>90</barcode:min-symbol-size>
                                    <barcode:max-symbol-size>100</barcode:max-symbol-size>
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
                                <fo:table-row height="40mm">
                                    <fo:table-cell width="31mm">
                                        <fo:block>
                                            <fo:instream-foreign-object>
                                                <barcode:barcode>
                                                    <xsl:attribute name="message">{"urls":["Task/<xsl:value-of
                                                            select="fhir:Bundle/fhir:identifier/fhir:value/@value"/>/$accept?ac=<xsl:value-of
                                                            select="fhir:accessCode"/>"]}</xsl:attribute>
                                                    <barcode:datamatrix>
                                                        <barcode:module-width>0.7mm</barcode:module-width>
                                                    </barcode:datamatrix>
                                                </barcode:barcode>
                                            </fo:instream-foreign-object>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block margin-top="3mm" margin-right="3mm" margin-left="1mm">
                                            <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:extension[@url='https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription']/fhir:extension[@url='Kennzeichen']/fhir:valueBoolean/@value = 'true' or string-length(fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:text/@value) &gt; 40 or string-length(fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:dosageInstruction/fhir:text/@value) &gt; 10">
                                                <xsl:attribute name="font-size">10pt</xsl:attribute>
                                            </xsl:if>
                                            <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:extension[@url='https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription']/fhir:extension[@url='Kennzeichen']/fhir:valueBoolean/@value = 'true'">
                                                <fo:block>
                                                    Teil <xsl:value-of
                                                            select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:extension[@url='https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription']/fhir:extension[@url='Nummerierung']/fhir:valueRatio/fhir:numerator/fhir:value/@value" />
                                                    von
                                                    <xsl:value-of
                                                            select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:extension[@url='https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription']/fhir:extension[@url='Nummerierung']/fhir:valueRatio/fhir:denominator/fhir:value/@value" />
                                                    ab
                                                    <xsl:call-template name="formatDate">
                                                        <xsl:with-param name="date" select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:extension[@url='https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription']/fhir:extension[@url='Zeitraum']/fhir:valuePeriod/fhir:start/@value"/>
                                                    </xsl:call-template>
                                                </fo:block>
                                            </xsl:if>
                                            <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:coding/fhir:code/@value = 'freitext'">
                                                <fo:block>
                                                Freitextverordnung
                                                </fo:block>
                                            </xsl:if>
                                            <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:coding/fhir:code/@value = 'rezeptur'">
                                                <fo:block>
                                                Rezeptur
                                                </fo:block>
                                            </xsl:if>
                                            <fo:block font-weight="bold">
                                                <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:dispenseRequest/fhir:quantity/fhir:value/@value"/>
                                                <xsl:text>x </xsl:text>
                                                <xsl:if test="starts-with(string(fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:meta/fhir:profile/@value), 'https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN') or starts-with(string(fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:meta/fhir:profile/@value), 'https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText')">
                                                    <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:text/@value"/>
                                                </xsl:if>
                                                <xsl:if test="starts-with(string(fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:meta/fhir:profile/@value), 'https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient')">
                                                    <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:ingredient/fhir:itemCodeableConcept/fhir:text/@value"/><xsl:text> </xsl:text><xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:ingredient/fhir:strength/fhir:numerator/fhir:value/@value"/><xsl:text> </xsl:text><xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:ingredient/fhir:strength/fhir:numerator/fhir:unit/@value"/><xsl:text> </xsl:text><xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:form/fhir:text/@value"/>

                                                </xsl:if>
                                                <xsl:if test="starts-with(string(fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:meta/fhir:profile/@value), 'https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding')">
                                                    <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:text/@value"/><!-- 123 -->
                                                </xsl:if>

                                                <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:amount/fhir:numerator/fhir:value/@value != '0' or fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:amount/fhir:numerator/fhir:extension/fhir:valueString/@value != '0'">
                                                    <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:coding/fhir:code/@value = 'rezeptur'">
                                                        <fo:block /><xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:amount/fhir:numerator/fhir:extension/fhir:valueString/@value"/><!-- 124 --><xsl:text> </xsl:text><xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:amount/fhir:numerator/fhir:unit/@value"/><!-- 125 -->
                                                        <fo:block />
                                                        <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:form/fhir:text/@value"/><!-- 104 -->
                                                    </xsl:if>
                                                    <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:coding/fhir:code/@value != 'rezeptur'">  
                                                    <xsl:text> / </xsl:text><xsl:value-of
                                                    select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:amount/fhir:numerator/fhir:value/@value" /><xsl:value-of
                                                    select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:amount/fhir:numerator/fhir:extension/fhir:valueString/@value"/><xsl:text> </xsl:text><xsl:value-of
                                                    select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:amount/fhir:numerator/fhir:unit/@value"/>
                                                    </xsl:if>
                                                    
                                                </xsl:if>
                                                <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:extension[@url='http://fhir.de/StructureDefinition/normgroesse']">
                                                    <xsl:text> </xsl:text>
                                                    <xsl:value-of
                                                            select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:extension[@url='http://fhir.de/StructureDefinition/normgroesse']/fhir:valueCode/@value"/>
                                                </xsl:if>
                                            </fo:block>
                                            <fo:block>
                                                <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:dosageInstruction/fhir:text/@value"/>
                                                <xsl:value-of
                                                        select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:dosageInstruction/fhir:patientInstruction/@value"/><!-- 128 -->
                                            </fo:block>
                                            <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:coding/fhir:code/@value != 'freitext' and fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:coding/fhir:code/@value != 'wirkstoff' and fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:coding/fhir:code/@value != 'rezeptur'">
                                                <fo:block>
                                                    PZN:<xsl:value-of
                                                            select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Medication/fhir:code/fhir:coding/fhir:code/@value"/>
                                                    <xsl:if test="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationRequest/fhir:substitution/fhir:allowedBoolean/@value = 'false'">
                                                        Kein Austausch
                                                    </xsl:if>
                                                </fo:block>
                                            </xsl:if>
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
