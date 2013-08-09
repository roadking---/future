package util

import analysis.trend.CachedTrend;
import analysis.trend.Decision;
import analysis.trend.Trend;
import price.FuturePrice;
import price.HistoryLoader;

class CurrentTrend extends Holds {
	def fp
	
	def static main(args){
		def fp = new FuturePrice()
		//def fp = [contractList:['SR201303'], getPrice:{contract->8000}]
		def lst = new CurrentTrend(fp:fp).analyzeAll()
		lst.each{
			println "${it.contract}\t${it.trend}-${it.seq}\t${it.action}"
		}
	}
	
	def analyzeAll(){
		def lst = fp.contractList.collect{
			if(it.startsWith('IF')) return
			analyze(it)
		}
		lst.retainAll{it != null}
		lst.each{
			it.action = it.trend != 'COMMON' && it.decision ? 'ADD' : (it.revert && it.hasHold ? 'REVERT' : '')
		}
		lst
	}
	
	def analyze(contract){
		def h = new HistoryLoader(contract)
		if(h.failed || Trend.params[h.commodity] == null) return
		
		def rlt = [contract:contract, trend:'COMMON']
		def price = fp.getPrice(contract).toDouble()
		h.addTodaysPrice(price)
		rlt.price = price
		
		def trend = new CachedTrend(history:h)
		trend.load()
		if(trend.dateSeq == null) return rlt 
		
		//def last = Date.parse('yyyy-MM-dd', '2010-10-27')
		def last = trend.dateSeq[-1]
		rlt.trend = trend.guess(last)
		rlt.seq = trend.getTrendSeq(last)
		
		rlt.hasHold = null != holds[contract]
		if(rlt.hasHold) {
			def revertDate = trend.findRevertDate(holds[contract].od)
			rlt.revert = trend.guess(holds[contract].od) == 'COMMON' || 
				(null != revertDate && last >= revertDate )
		}
		
		rlt.decision = new Decision(date:last, 
									trend:trend, 
									in_experiment:false, 
									holds:fp.getHolds(contract).toInteger()
								).decision
		h.close()
		 
		rlt
	}
}
