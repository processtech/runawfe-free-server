<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:runawfe="http://runa.ru/xml" xmlns:cal="java.util.Calendar" xmlns:wfe_ve="ru.runa.wf.logic.bot.WebServiceTaskHandler">
 <xsl:script implements-prefix="runawfe" language="java" src="java:ru.runa.wf.logic.bot.WebServiceTaskHandler"/>

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="text()|@*|*|processing-instruction()|comment()"> 
        <xsl:copy> 
                <xsl:apply-templates select="text()|@*|*|processing-instruction()|comment()"/> 
        </xsl:copy> 
    </xsl:template> 

    <xsl:template match="runawfe:variable">
      <xsl:variable name="variableName" select="@name"/>
      <xsl:value-of select="wfe_ve:getVariable($variableName)"/>
    </xsl:template>

    <xsl:template match="runawfe:processGraph">
      <xsl:variable name="variableName" select="@processIdVariable"/>
      <xsl:value-of select="wfe_ve:getProcessGraph($variableName)"/>
    </xsl:template>


    <xsl:template match="runawfe:processId">
      <xsl:value-of select="wfe_ve:getProcessId()" />
    </xsl:template>
    
    <xsl:template match="runawfe:newvariable">
      <xsl:variable name="variableName" select="@name"/>
      <xsl:variable name="variableValue" select="@value"/>
      <xsl:copy-of select="wfe_ve:setNewVariable($variableName, $variableValue)"/>
    </xsl:template>

</xsl:stylesheet>
