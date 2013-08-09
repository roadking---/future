package load

import org.htmlcleaner.*
import javax.xml.transform.*
import javax.xml.transform.stream.*

class Shanghai extends Zhengzhou {
	static main(args){
		new Shanghai('FutureDB', '20110901', '20110902')
	}
	
	Shanghai(){}
	Shanghai(dir, startDate, endDate){
		this.dir = dir
		this.startDate = Date.parse('yyyyMMdd', startDate)
		this.endDate = Date.parse('yyyyMMdd', endDate)
		
		//download('20080301')
		(this.startDate+1 .. this.endDate).each{download(it)}
	}
	
	def transfer(WEB_HTML, XSL){
		def PRICE_XML = File.createTempFile('prices','.xml') //new File('data.xml')
		def transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(XSL))
		transformer.transform(new StreamSource(WEB_HTML), new StreamResult(new FileOutputStream(PRICE_XML)))
		PRICE_XML
	}
	
	def download(Date date){
		download(date.format('yyyyMMdd'))
	}
	
	def download(String date){
		println "SH: ${date}"
		def url = "http://www.shfe.com.cn/dailydata/kx/kx${date}.html"
		def priceNode
		try{
			def WEB_HTML = downloadWebPage(url)
			WEB_HTML = new String(WEB_HTML.getBytes(), 'utf-8')
			def PRICE_XML = transfer(WEB_HTML, getClass().getResource('sh.xsl').getFile())
			new File(WEB_HTML).delete()
			priceNode = new XmlSlurper().parseText(PRICE_XML.text)
		}catch(e){
			println "Error happens on ${date}"
			println e
			return
		}
		
		def tmpCommodity
		priceNode.item.each{
			if(it.commodity.toString().trim() == '' || it.commodity.toString().trim() == '?') it.commodity = tmpCommodity
			else tmpCommodity = it.commodity.toString().trim()
		}
		
		priceNode.item.each{
			def contract = it.commodity.toString().trim() + makeupMonthStr(it.month.toString().trim())
			def dateStr = "${date[0..3]}-${date[4..5]}-${date[6..7]}"
			def data = [	date:dateStr,		contract:contract,		lastSettle:it.last_settlement,
    								open:it.open,		high:it.high,				low:it.low,
    								close:it.close,	settlement:it.settlement,	diff:it.diff,
    								amount:it.amount, commodity:it.commodity, holds:it.holds ]
			name2code data
			write(data)
		}
	}
	
	def name2code(data){
		def code = data.commodity
		switch(code){
			case 'ÂÁ':code = 'AL'; break
			case '»Æ½ð':code = 'AU'; break
			case 'Í­':code = 'CU'; break
			case 'È¼ÁÏÓÍ':code = 'FU'; break
			case 'Ç¦':code = 'PB'; break
			case 'ÂÝÎÆ¸Ö':code = 'RB'; break
			case 'ÌìÈ»Ïð½º':code = 'RU'; break
			case 'Ïß²Ä':code = 'WR'; break
			case 'Ð¿':code = 'ZN'; break
			case '°×Òø':code = 'AG'; break
		}
		if(code != data.commodity){
			String contract = data.contract
			String commodity = data.commodity
			data.contract = code + contract.substring(commodity.length())
			data.commodity = code
		}
	}
	
	def downloadWebPage(url){
		//change html content into xml
		CleanerProperties props = new CleanerProperties()
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		TagNode node = new HtmlCleaner(props).clean(new URL(url))
		def WEB_HTML = File.createTempFile('web','.html').name
		new PrettyXmlSerializer(props).writeToFile(node, WEB_HTML, "gb2312")
		WEB_HTML
	}
}