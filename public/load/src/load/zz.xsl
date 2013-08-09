<?xml version="1.0" encoding="gb2312"?>
<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" encoding="gb2312"/>
	<xsl:template match="/html">
		<prices>
			<xsl:for-each select="//table[@id='senfe']/tbody/tr[td[1]!='小计' and td[1]!='总计' and td[1]!='品种月份']">
				<item>
					<commodity><xsl:value-of select="substring(td[1], 1, 2)"/></commodity>
					<month>201<xsl:value-of select="substring(td[1], 3)"/></month>
					<last_settlement><xsl:value-of select="td[2]"/></last_settlement>
					<open><xsl:value-of select="td[3]"/></open>
					<high><xsl:value-of select="td[4]"/></high>
					<low><xsl:value-of select="td[5]"/></low>
					<close><xsl:value-of select="td[6]"/></close>
					<settlement><xsl:value-of select="td[7]"/></settlement>
					<diff><xsl:value-of select="td[8]"/></diff>
					<amount><xsl:value-of select="td[10]"/></amount>
					<holds><xsl:value-of select="td[11]"/></holds>
				</item>
			</xsl:for-each>
		</prices>
	</xsl:template>
</xsl:stylesheet>