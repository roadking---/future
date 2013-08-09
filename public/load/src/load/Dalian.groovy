package load

class Dalian extends Shanghai {
	Dalian(dir, startDate, endDate){
		this.dir = dir
		this.startDate = Date.parse('yyyyMMdd', startDate)
		this.endDate = Date.parse('yyyyMMdd', endDate)
		
		(this.startDate+1 .. this.endDate).each{download(it)}
	}
	
	def download(String date){
		println "DL: ${date}"
		def url = "http://www.dce.com.cn/PublicWeb/MainServlet?action=Pu00011_result&Pu00011_Input.trade_date=${date}&Pu00011_Input.trade_type=0&Pu00011_Input.variety=all"
		def priceNode
		try{
			def WEB_HTML = downloadWebPage(url)
			def PRICE_XML = transfer(WEB_HTML, getClass().getResource('dl.xsl').getFile())
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
			name2code data
			write(data)
		}
	}

	def name2code(data){
		def code = data.commodity
		switch(code){
			case '∂π“ª':code = 'A'; break
			case '∂π∂˛':code = 'B'; break
			case '”Ò√◊':code = 'C'; break
			case 'ΩπÃø':code = 'J'; break
			case 'æ€““œ©':code = 'L'; break
			case '∂π∆…':code = 'M'; break
			case '◊ÿÈµ”Õ':code = 'P'; break
			case 'æ€¬»““œ©':code = 'V'; break
			case '∂π”Õ':code = 'Y'; break
		}
		if(code != data.commodity){
			String contract = data.contract
			String commodity = data.commodity
			data.contract = code + contract.substring(commodity.length())
			data.commodity = code
		}
	}
}