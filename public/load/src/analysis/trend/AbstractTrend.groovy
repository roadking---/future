package analysis.trend;

import java.util.Date;

abstract class AbstractTrend {
	def history
	
	def abstract guess(Date date)
	
	def getRevertDate(String date){
		getRevertDate(Date.parse('yyyy-MM-dd', date))
	}
	def getRevertDate(Date date){
		def trend = guess(date)
		if(trend == 'COMMON' || trend == 'NONE') return null
		
		def count = 60
		while(count-- > 0){
			date = date.next()
			def p = history.getPrice(date)
			if(p == null || p == 0) continue
			if(guess(date) != trend) return date.format('yyyy-MM-dd')
		}
		history.maxDate
	}
	
	def win(date){
		def price = history.getPrice(date)
		if(price == null || price == 0) return 0
		
		def trendStr = guess(date)
		if(trendStr == 'COMMON') return 0
		
		def revert = getRevertDate(date)
		if(revert == null) revert = history.maxDate
		def revertPrice = history.getPrice(revert)
		if(revertPrice == null || revertPrice == 0) return 0
		(trendStr.startsWith('UP')?1:-1) * (revertPrice - price)
	}
	
	
}
