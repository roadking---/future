package price

import org.htmlcleaner.*
import javax.xml.transform.*
import javax.xml.transform.stream.*

class FuturePrice{

	def url = 'http://quote.futures.hexun.com/EmbPrice.aspx'
	def PRICE_DOC, xpath
	def priceNode
	
	static final def MIN_HOLDS = 50
	
	FuturePrice(){
		//change html content into xml
		CleanerProperties props = new CleanerProperties()
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		TagNode node = new HtmlCleaner(props).clean(new URL(url))
		def WEB_HTML = File.createTempFile('web','.html').name
		new PrettyXmlSerializer(props).writeToFile(node, WEB_HTML, "gb2312")
		
		def PRICE_XML = File.createTempFile('prices','.xml') //new File('data.xml')
		def transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(getClass().getResource('prices.xsl').getFile()))
		transformer.transform(new StreamSource(WEB_HTML), new StreamResult(new FileOutputStream(PRICE_XML)))
		new File(WEB_HTML).deleteOnExit()
		
		priceNode = new XmlSlurper().parseText(PRICE_XML.text)
	}
	
	def show(contract){
		def m = contract =~ /(\D+)(\d+)/
		def node = priceNode.item.find {
			it['contract'] == contract || it['contract'] == "${m[0][1]}${m[0][2][2..-1]}" ||
				it['contract'] == "${m[0][1]}${m[0][2][3..-1]}"
			}
		showNode(node)
	}
	
	def showNode(node){
		def current = node.current.toString().trim().replaceAll("\\.0+\$", "")
		def diff_percent = node.'diff-percent'.toString().trim()
		println "${node.contract}\t${current}\t${diff_percent}"
	}
	
	static main(args){
		def fp = new FuturePrice()
		
		def method = args.length > 0 ? args[0] : 'concern'
		fp."${method}"(args.tail())
	}
	
	def allbyseq(args){
		def threshold = 0.5
		def all = (1..priceNode.item.size()).toList()
		all = all.findAll{priceNode.item[it].'diff-percent'.toString().trim() != '' && priceNode.item[it].'diff-percent'.toString().trim().toDouble().abs() > threshold}
		all = all.sort{
			-priceNode.item[it].'diff-percent'.toString().trim().toDouble().abs()
			}
		all.each{showNode(priceNode.item[it])}
	}
	
	def concern(contracts){
		contracts.each{show(it)}
	}
	
	def getContractList(){
		def lst = []
		priceNode.item.each{lst << it.contract.toString()}
		lst.retainAll{it ==~ /\D+\d+/}
		
		lst.removeAll{
			def m = it =~ /(\D+)(\d+)/
			m[0][1] == 'IF'
		}
		//remove contracts with to few holds
		lst.retainAll{contract-> 
			def node = priceNode.item.find { it['contract'] == contract }
			node.holds.toString().toInteger() >= MIN_HOLDS
			}
		
		lst.collect{
			def m = it =~ /(\D+)(\d+)/
			"${m[0][1]}20${m[0][2]}"
		}
	}
	
	def getPrice(contract){
		def m = contract =~ /(\D+)20(\d+)/
		if(m.matches())
			contract = m[0][1] + m[0][2]
		
		def node = priceNode.item.find { it['contract'] == contract }
		node.current.toString().trim()
	}
	
	def getHolds(contract){
		def m = contract =~ /(\D+)20(\d+)/
		if(m.matches())
			contract = m[0][1] + m[0][2]
		
		def node = priceNode.item.find { it['contract'] == contract }
		node.holds.toString().trim()
	}
}