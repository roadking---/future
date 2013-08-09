package price

import groovy.sql.Sql

class HistoryLoader{
	static def FUTUREDB_DIR = 'FutureDB'
	def sql, commodity, contract, tablename
	def failed = false
	
	static main(args){
//		def hl = new HistoryLoader('ZN201208')
//		println hl.getHolds('2011-10-10')
//		hl.close()
//		return
//		
//		hl = new HistoryLoader('RO201007')
//		println hl.getAvg('2009-8-10', 10)
//		hl.close()
//		return
		
		def hl = new HistoryLoader('A201007')
		println hl.getAvg('2009-3-3', 10)
		println hl.getHolds('2009-3-3')
		def (d1, d2) =  hl.getDealDateExtent('2009-1-1', '2010-6-1')
		println d1
		println d2
		println hl.nextDate(d1, 4)
		hl.close()
	}
	
	def getDealDateExtent(String start, String end){
		getDealDateExtent Date.parse('yyyy-MM-dd',start), Date.parse('yyyy-MM-dd',end)
	}
	def getDealDateExtent(Date start, Date end){
		def e = maxDate, s = minDate
		s = start.before(s) ? s : start
		e = end.after(e) ? e : end
		[s,e]
	}
	
	def nextDate(String date, delay=0){
		nextDate(Date.parse('yyyy-MM-dd', date), delay)
	}
	def nextDate(Date date, delay=0){
		if(sql == null) return
		def seq = sql.firstRow("select max(seq) from " + tablename + " where deal_date <= ?", [date])[0]
		
		def row = sql.firstRow("select min(deal_date) from " + tablename + " where seq > ?", [seq+delay])
		return row == null ? null : row[0]
	}
	
	def getHolds(Date deal_date){
		if(sql == null) return
		sql.firstRow("select holds from " + tablename + " where deal_date = ?", [deal_date])?.holds
	}
	def getHolds(String date){
		date == null ? null : getHolds(Date.parse('yyyy-MM-dd',date))
	}
	def getPrice(Date deal_date){
		if(sql == null) return
		sql.firstRow("select price from " + tablename + " where deal_date = ?", [deal_date])?.price
	}
	def getPrice(String date){
		date == null ? null : getPrice(Date.parse('yyyy-MM-dd',date))
	}
	def getPricesAfter(String date, ndays){
		return getPricesAfter(Date.parse('yyyy-MM-dd',date), ndays)
	}
	def getPricesAfter(Date date, ndays){
		if(sql == null) return
		def seq = sql.firstRow("select max(seq) from " + tablename + " where deal_date <= ?", [date])[0]
		def items = []
		sql.eachRow("select price from " + tablename + " where seq between ? and ?", [seq, seq+ndays-1]){ row ->
			items << row.price
		}
		items.retainAll{it > 0}
		items
	}
	
	def getMaxDate(){
		if(sql == null) return
		sql.firstRow("select max(deal_date) from " + tablename)[0]
	}
	def getMinDate(){
		if(sql == null) return
		sql.firstRow("select min(deal_date) from " + tablename)[0]
	}
	
	def getPricesBefore(String date, ndays){
		return getPricesBefore(Date.parse('yyyy-MM-dd',date), ndays)
	}
	def getPricesBefore(Date date, ndays){
		if(sql == null) return
		
		def seq = sql.firstRow("select max(seq) from " + tablename + " where deal_date <= ?", [date])[0]
		if(seq == null) return
		
		def items = []
		sql.eachRow("select price from " + tablename + " where seq between ? and ?", [seq-ndays+1, seq]){ row ->
			items << row.price
		}
		items.retainAll{it > 0}
		items
	}
	
	def getAvg(String date, n){
		def deal_date = Date.parse('yyyy-MM-dd',date)
		getAvg(deal_date, n)
	}
	def getAvg(Date deal_date, n){
		def items = getPricesBefore(deal_date, n)
		def avg = new Double(items.sum()/items.size())
		commodity == 'AU' ? avg.round(2) : avg.round()
	}
	
	def addTodaysPrice(price){
		if(sql == null) return
	
		if(price instanceof String) price = price.toDouble()
		
		def seq = sql.firstRow("select max(seq) from " + tablename)[0]
		def future = sql.dataSet(tablename)
		future.add(	seq			: seq+1,
					deal_date	: new Date(),
					price		: price,
					holds		: 500
					)
	}
	
	HistoryLoader(contract){
		this.contract = contract
		this.tablename = "future_${contract}"
		
		def info = getContractInfo(contract)
		commodity = info.code
		def f = getFile(info)
		if(f == null){
			println "Fail to load the history file for ${contract}!"
			failed = true
			return
		}
		
		sql = Sql.newInstance("jdbc:hsqldb:mem:futuredb", "sa", "", "org.hsqldb.jdbcDriver")
		
		sql.call("create table " + tablename + "(seq int, deal_date date, price dec(7,2), holds int)")
		
		def future = sql.dataSet(tablename)
		def seqNum = 1
		f.eachLine{
			def fields = it.split("\t")
			//def m = it =~ /(\d+\-\d+\-\d+)\s([\d|\.]*)\s([\d|\.]*)\s([\d|\.]*)\s([\d|\.]*)\s([\d|\.]*).+/
			def m = it =~ /(\S+)\s(\S*)\s(\S*)\s(\S*)\s(\S*)\s(\S*).+\s(\S*)$/
			def prices = [	date	:	m[0][1],
							open	:	m[0][3],
							close	:	m[0][6],
							high	:	m[0][4],
							low		:	m[0][5],
							holds	:	m[0][7],
						]
			
			future.add(	seq		:	seqNum++,
						deal_date : Date.parse('yyyy-MM-dd',prices.date),
						price	:	findAppropriatePrice(prices),
						holds	:	prices.holds.trim()==~/\d+/ ? prices.holds.toInteger() : 0
						)
		}
	}
	
	def close(){
		sql?.call("drop table " + tablename)
		sql?.close()
	}
	
	def findAppropriatePrice(prices){
		def lst = [prices.open, prices.close, prices.high, prices.low]
		lst = lst.collect{it ==~ /\-?\d*\.?\d+/ ? it.toDouble() : 0}
		lst.retainAll{it > 0}
		lst.size() > 0 ? lst.sum()/lst.size() : 0
	}
	
	def getFile(info){
		def f = new File("${FUTUREDB_DIR}\\${info.name}\\${info.name}${info.month}.txt")
		if(f.exists())return f
		
		f = new File("${FUTUREDB_DIR}\\${info.name}\\${info.name}${info.month.substring(2)}.txt")
		if(f.exists())return f
	}
	
	def getContractInfo(contract){
		def match = contract =~ /(\D+)\d+/
		def code = match[0][1]
		
		def commodityName = getCommodityName(code)
		['name':commodityName, 'code':code, 'month':contract.substring(code.length())]
	}
	
	static def getCommodityName(code){
		def code2NameMapping = [:] 
//			['A':'¶¹Ò»', 'B':'¶¹¶þ', 'M':'¶¹ÆÉ', 'Y':'¶¹ÓÍ', 'AU':'»Æ½ð', 
//			'J':'½¹Ì¿', 'V':'¾ÛÂÈÒÒÏ©', 'L':'¾ÛÒÒÏ©', 'AL':'ÂÁ', 'RB':'ÂÝÎÆ¸Ö', 'PB':'Ç¦',
//			'FU':'È¼ÁÏÓÍ', 'RU':'ÌìÈ»Ïð½º', 'CU':'Í­', 'WR':'Ïß²Ä', 'ZN':'Ð¿', 'C':'ÓñÃ×',
//			'P':'×ØéµÓÍ']
		code2NameMapping.containsKey(code) ? code2NameMapping.get(code) : code
	}
	
	static def loadContractList(code){
		def commodityName = getCommodityName(code)
		def lst = []
		new File("${FUTUREDB_DIR}\\${commodityName}").eachFile{
			def str = it.getName().replace("${FUTUREDB_DIR}\\${commodityName}", '').replace('.txt','').replace(commodityName, code)
			def month = str.substring(code.length())
			if(month.length() == 4) str = "${code}20${month}"
			lst << str
		}
		lst
	}
}