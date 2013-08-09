<?xml version="1.0" encoding="gb2312"?>
<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="no" encoding="gb2312"/>
	<xsl:template match="/html">
		<prices>
			<xsl:for-each select="body/table[2]/tbody/tr[td[2]!='' and td[1]!='总计' and td[2]!='小计']">
				<item>
					<commodity><xsl:value-of select="td[1]"/></commodity>
					<month><xsl:value-of select="td[2]"/></month>
					<last_settlement><xsl:value-of select="td[3]"/></last_settlement>
					<open><xsl:value-of select="td[4]"/></open>
					<high><xsl:value-of select="td[5]"/></high>
					<low><xsl:value-of select="td[6]"/></low>
					<close><xsl:value-of select="td[7]"/></close>
					<settlement><xsl:value-of select="td[8]"/></settlement>
					<diff><xsl:value-of select="td[10]"/></diff>
					<amount><xsl:value-of select="td[11]"/></amount>
					<holds><xsl:value-of select="td[12]"/></holds>
				</item>
			</xsl:for-each>
		</prices>
	</xsl:template>
</xsl:stylesheet>