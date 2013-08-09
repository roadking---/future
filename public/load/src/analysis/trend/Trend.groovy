package analysis.trend

import java.util.List;

import org.apache.commons.math.stat.regression.SimpleRegression;

class Trend extends AbstractTrend{
	static def PARAMS_FILE = 'PARAMS.map'
	static def params = new File(PARAMS_FILE).newObjectInputStream()?.readObject()
//	[
//		SR:parse([21, 0.28, 23, -0.02, 5, 0.53, 8, 13, -0.24, 5]),
//		RO:parse([6, 0.58, 39, -0.34, 12, 1.46, 2, 4, -0.02, 5]),
//		AL:parse([21, 0.8, 37, -0.17, 46, 0.49, 2, 16, -0.09, 8]),
////		L:[extent:7, up:0.15, down:-0.08, 
////			revert_up_extent:5, revert_up_param:0.32, revert_up_delay:8,
////			revert_down_extent:4, revert_down_param:-0.03, revert_down_delay:7],
////		SR:[extent:45, up:0.02, down:-1.04, 
////			revert_up_extent:35, revert_up_param:0.06, revert_up_delay:2,
////			revert_down_extent:10, revert_down_param:-0.86, revert_down_delay:2],
////		TA:[extent:28, up:0.18, down:-0.38,
////			revert_up_extent:12, revert_up_param:0.08, revert_up_delay:0,
////			revert_down_extent:10, revert_down_param:-0.37, revert_down_delay:8],
////		RO:[extent:23, up:1.04, down:-0.67,
////			revert_up_extent:3, revert_up_param:2, revert_up_delay:4,
////			revert_down_extent:11, revert_down_param:-0.46, revert_down_delay:8],
////		A:[extent:18, up:0.62, down:0,
////			revert_up_extent:4, revert_up_param:0.25, revert_up_delay:2,
////			revert_down_extent:11, revert_down_param:-0.23, revert_down_delay:8],
//		
//		]
	
	def guess(String date){
		guess(Date.parse('yyyy-MM-dd', date))
	}
	def guess(Date date){
		if(!params.containsKey(history.commodity)) return 'NONE'
		
		getSlope(date, params[history.commodity].up_extent) >= params[history.commodity].up ? 'UP' : (
			getSlope(date, params[history.commodity].down_extent) <= params[history.commodity].down ? 'DOWN' : 
				'COMMON'
		)
	}
	
	def getSlope(date, extent){
		if(date == null) return 0
		
		def prices = history.getPricesBefore(date, extent)
		if(prices == null) return 0
		
		prices.retainAll{it != null && it != 0}
		if(prices.size() < 2) return 0
		
		def regression = new SimpleRegression()
		for(idx in 0 .. prices.size()-1) regression.addData(idx, 100*prices[idx]/prices[0])
		regression.slope
	}
	
	def findRevertDate(String date){
		findRevertDate(Date.parse('yyyy-MM-dd', date))
	}
	def findRevertDate(Date date){
		def direction = guess(date)
		if(direction == 'COMMON' || direction == 'NONE') return null
		def delay = direction == 'UP' ? params[history.commodity].revert_up_delay : params[history.commodity].revert_down_delay
		
		def count = 60
		while(count-- > 0){
			date = date.next()
			def p = history.getPrice(date)
			if(p == null || p == 0) continue
			if(revert(direction, date)) {
				if(direction != guess(date)) return date
				
				while(delay-- > 0 && date != null && direction == guess(date)) date = history.nextDate(date)
				return date
			}
				
		}
		null
	}
	
	def getRevertDate(Date date){
		def revert = findRevertDate(date)
		revert == null ? history.maxDate : revert 
	}
	
	def revert(direction, date){
		if(direction == 'COMMON' || direction != guess(date)) return true
		
		if(direction == 'UP'){
	//		assert params[history.commodity].revert_up_param > params[history.commodity].up
			def slope = getSlope(date, params[history.commodity].revert_up_extent)
			return slope < params[history.commodity].revert_up_param
		} else if(direction == 'DOWN'){
	//		assert params[history.commodity].revert_down_param < params[history.commodity].down
			def slope = getSlope(date, params[history.commodity].revert_down_extent)
			return slope > params[history.commodity].revert_down_param
		}
	}
	
	static def parse(List p){
		[	up_extent:p[0], up:p[1], down_extent:p[2], down:p[3],
			revert_up_extent:p[4], revert_up_param:p[5], revert_up_delay:p[6],
			revert_down_extent:p[7], revert_down_param:p[8], revert_down_delay:p[9],
			first_n_days_up:p[10], first_n_days_down:p[11], 
		]
	}
	
	static def update(commodity, p){
		params[commodity] = p
		def f = new File(Trend.PARAMS_FILE)
		f.delete()
		f.newObjectOutputStream() << Trend.params
	}
}
