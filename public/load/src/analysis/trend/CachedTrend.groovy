package analysis.trend

import java.util.Date;

class CachedTrend extends Trend {
	def dateSeq, trendCache, revertCache
	
	def load(){
		def d = history.minDate, max = history.maxDate
		while(d <= max){
			def price = history.getPrice(d)
			if(price != null && price > 0) guess(d)
			
			d = d.next()
		}
	}
	
	def getTrendSeq(String date){
		getTrendSeq Date.parse('yyyy-MM-dd', date)
	}
	
	def getTrendSeq(Date date){
		if(dateSeq == null || dateSeq.size() == 0) return 0
		
		def idx
		if(dateSeq[-1].equals(date)) idx = dateSeq.size()-1
		else idx = dateSeq.findIndexOf { it.equals(date) }
		
		if(idx == -1 || idx >= dateSeq.size()|| trendCache[dateSeq[idx]] == 'COMMON') return 0
		
		def count = 0
		while(idx - ++count >=0
			&& trendCache[dateSeq[idx - count]] == trendCache[dateSeq[idx]])
			;
		count
	}
	
	def addCache(Date date, trend) {
		if(trendCache == null) trendCache = [:]
		if(trendCache[date] != null) return
		trendCache[date] = trend
		
		if(dateSeq == null) dateSeq = []
		dateSeq << date
		if(dateSeq.size() >= 2 && dateSeq[-2].after(date)){
			dateSeq.sort()
		}
	}

	def guess(Date date) {
		if(dateSeq == null) dateSeq = []
		if(trendCache != null && trendCache[date] != null) return trendCache[date]
		
		def trend = dateSeq.size() > 30 ? super.guess(date) : 'COMMON'
		addCache(date, trend)
		trend
	}
	
	def getRevertDate(Date date){
		if(revertCache == null) revertCache = [:]
		if(revertCache[date] != null) return revertCache[date]
		
		def revert = super.getRevertDate(date)
		revertCache[date] = revert
		revert
	}
}
