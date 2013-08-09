<?xml version="1.0" encoding="gb2312"?>
<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="no" encoding="gb2312"/>
	<xsl:template match="/html">
		<prices>
			<xsl:for-each select="body/div[@id='hisBor']/table/tbody/tr">
				<xsl:if test="count(td) > 1">
					<item>
						<contract>
							<xsl:call-template name="name2code">
								<xsl:with-param name="contract" select="td[1]"/>
							</xsl:call-template>
						</contract>
						<current><xsl:value-of select="td[3]"/></current>
						<diff><xsl:value-of select="td[5]"/></diff>
						<buy-price><xsl:value-of select="td[7]"/></buy-price>
						<buy-amount><xsl:value-of select="td[9]"/></buy-amount>
						<sell-price><xsl:value-of select="td[11]"/></sell-price>
						<sell-amount><xsl:value-of select="td[13]"/></sell-amount>
						<deal-amount><xsl:value-of select="td[15]"/></deal-amount>
						<open-price><xsl:value-of select="td[17]"/></open-price>
						<last-settlement><xsl:value-of select="td[19]"/></last-settlement>
						<high-price><xsl:value-of select="td[21]"/></high-price>
						<low-price><xsl:value-of select="td[23]"/></low-price>
						<holds><xsl:value-of select="td[25]"/></holds>
						<holds-diff><xsl:value-of select="td[27]"/></holds-diff>
						<diff-percent>
							<xsl:choose>
								<xsl:when test="td[19] > 0">
									<xsl:value-of select="round(td[5]*10000 div td[19]) div 100 "/>
								</xsl:when>
								<xsl:otherwise>0</xsl:otherwise>
							</xsl:choose>
						</diff-percent>
					</item>
				</xsl:if>
			</xsl:for-each>
		</prices>
	</xsl:template>
	
	<xsl:template name="name2code">
		<xsl:param name="contract"/>
		<xsl:choose>
			<xsl:when test="starts-with($contract,'棉花')">
				<xsl:value-of select="concat('CF',substring-after($contract,'棉花'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'早籼稻')">
				<xsl:value-of select="concat('ER',substring-after($contract,'早籼稻'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'菜籽油')">
				<xsl:value-of select="concat('RO',substring-after($contract,'菜籽油'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'白糖')">
				<xsl:value-of select="concat('SR',substring-after($contract,'白糖'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'PTA')">
				<xsl:value-of select="concat('TA',substring-after($contract,'PTA'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'硬麦')">
				<xsl:value-of select="concat('WT',substring-after($contract,'硬麦'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'强麦')">
				<xsl:value-of select="concat('WS',substring-after($contract,'强麦'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'豆一')">
				<xsl:value-of select="concat('A',substring-after($contract,'豆一'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'豆二')">
				<xsl:value-of select="concat('B',substring-after($contract,'豆二'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'玉米')">
				<xsl:value-of select="concat('C',substring-after($contract,'玉米'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'焦炭') and $contract != '焦炭'">
				<xsl:value-of select="concat('J',substring-after($contract,'焦炭'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'聚乙烯')">
				<xsl:value-of select="concat('L',substring-after($contract,'聚乙烯'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'豆粕')">
				<xsl:value-of select="concat('M',substring-after($contract,'豆粕'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'棕榈油')">
				<xsl:value-of select="concat('P',substring-after($contract,'棕榈油'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'聚氯乙烯')">
				<xsl:value-of select="concat('V',substring-after($contract,'聚氯乙烯'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'豆油')">
				<xsl:value-of select="concat('Y',substring-after($contract,'豆油'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'沪铝')">
				<xsl:value-of select="concat('AL',substring-after($contract,'沪铝'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'黄金')">
				<xsl:value-of select="concat('AU',substring-after($contract,'黄金'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'沪铜')">
				<xsl:value-of select="concat('CU',substring-after($contract,'沪铜'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'燃油')">
				<xsl:value-of select="concat('FU',substring-after($contract,'燃油'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'沪铅')">
				<xsl:value-of select="concat('PB',substring-after($contract,'沪铅'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'螺纹钢') and not(starts-with($contract,'螺纹钢西部'))">
				<xsl:value-of select="concat('RB',substring-after($contract,'螺纹钢'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'橡胶')">
				<xsl:value-of select="concat('RU',substring-after($contract,'橡胶'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'线材')">
				<xsl:value-of select="concat('WR',substring-after($contract,'线材'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'沪锌')">
				<xsl:value-of select="concat('ZN',substring-after($contract,'沪锌'))"/>
			</xsl:when>
			<xsl:when test="starts-with($contract,'甲醇')">
				<xsl:value-of select="concat('ME',substring-after($contract,'甲醇'))"/>
			</xsl:when>
			
			<xsl:otherwise><xsl:value-of select="$contract"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>