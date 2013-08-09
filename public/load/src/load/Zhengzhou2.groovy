package load;

public class Zhengzhou2 extends Shanghai {
	Zhengzhou2(dir, startDate, endDate){
		this.dir = dir
		this.startDate = Date.parse('yyyyMMdd', startDate)
		this.endDate = Date.parse('yyyyMMdd', endDate)
		
		(this.startDate+1 .. this.endDate).each{download(it)}
	}
	
	static main(argv){
		new Zhengzhou2(null, '20090522', '20090523')
	}
	
	def download(String date){
		println "ZZ: ${date}"
		def url = "http://www.czce.com.cn/portal/exchange/${date[0..3]}/datadaily/${date}.htm"
		
		def priceNode
		try{
			def WEB_HTML = downloadWebPage(url)
			def PRICE_XML = transfer(WEB_HTML, getClass().getResource('zz.xsl').getFile())
			new File(WEB_HTML).delete()
			priceNode = new XmlSlurper().parseText(PRICE_XML.text.replaceAll(',',''))
		}catch(e){
			println "Error happens on ${date}"
			println e
			return
		}
		
		priceNode.item.each{
			def contract = it.commodity.toString().trim() + makeupMonthStr(it.month.toString().trim())
			def dateStr = "${date[0..3]}-${date[4..5]}-${date[6..7]}"
			def data = [	date:dateStr,		contract:contract,		lastSettle:it.last_settlement,
									open:it.open,		high:it.high,				low:it.low,
									close:it.close,	settlement:it.settlement,	diff:it.diff,
									amount:it.amount, commodity:it.commodity, holds:it.holds ]
			write(data)
		}
	}
	
}
