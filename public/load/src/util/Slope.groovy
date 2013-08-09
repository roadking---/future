package util

import analysis.trend.Trend;
import price.FuturePrice;
import price.HistoryLoader;

class Slope {

	def history
	
	static main(args){
//		new Holds().holds.keySet().collect{ (it =~ /(\D+)\d+/)[0][1] }.each{
//			println Trend.params[it]
//		}
		showCurrentSlopes()
	}
	
	def static showCurrentSlopes(){
		def fp = new FuturePrice()
		println "contract\tdirection\tslope\tref"
		fp.contractList.each{contract->
			def h = new HistoryLoader(contract)
			if(h.failed || Trend.params[h.commodity] == null) return
			def price = fp.getPrice(contract).toDouble()
			h.addTodaysPrice(price)
			
			def rlt = new Slope(history:h).getSlope(new Date())
			if(rlt.direction != 'COMMON')
				println "$contract\t$rlt.direction\t$rlt.slope\t$rlt.ref"
		}
	}
	
	def getSlope(date){
		def t = new Trend(history:history)
		def param = Trend.params[history.commodity]
		switch(t.guess(date)){
			case 'COMMON': 	return [direction:'COMMON', slope:0, ref:0]
			case 'UP':		return [direction:'UP', slope:t.getSlope(date, param.up_extent).round(2), ref:param.up]
			case 'DOWN':	return [direction:'DOWN', slope:t.getSlope(date, param.down_extent).round(2), ref:param.down]
		}
	}
}
