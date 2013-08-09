package analysis.old
import price.*

class TraditionalTrend{
	def BIG_THRESHOLD = 3, SMALL_THRESHOLD = 1.5
	def LONG_AVG_NUM = 15, SHORT_AVG_NUM = 4
	
	def params = [	'A':[8,22,40.21],
					'AL':[15,30,287.14],
					'C':[7,28,11.58],
					'CF':[7,26,673.03],
					'CU':[12,30,1625.77],
					'ER':[5,26,7.46],			//5/30/4:	S/F 158/187	11.19
					'FU':[10,30,79.98],
					'J':[7,28,0],
					'L':[3,20,230.62],
					'M':[9,23,57.42],
					'P':[6,30,217.02],
					'PB':[3,12,474.12],		//3/12/6:	S/F 42/2	545.54
					'RB':[3,21,90.22],
					'RO':[15,25,101.43],
					'RU':[8,30,722.33],
					'SR':[9,22,73.33],
					'TA':[9,18,143.09],
					'V':[9,27,48.5],			//6/22/6:	S/F 460/319	74.48
					'WS':[12,18,9.89],		//12/18/5:	S/F 405/361	14.21
					'Y':[7,28,147.27],
					'ZN':[12,27,331.28],
								]
	
	def getTrend(current, avgShort, avgLong){
		def indicator = 0
		if(avgLong > 0){
			current = current > 0 ? current : avgLong
			avgShort = avgShort > 0 ? avgShort : avgLong
			indicator = (current + avgShort)/avgLong - 2
			indicator *= 100
		}
		
		indicator >= BIG_THRESHOLD ? 'UP*2' : (
			indicator >= SMALL_THRESHOLD ? 'UP' : (
				indicator <= -BIG_THRESHOLD ? 'DOWN*2' :(
					indicator <= -SMALL_THRESHOLD ? 'DOWN' : 'COMMON'
				)
			)
		)
	}
	
	def guess(history, date, useDefault=true){
		def short_num = SHORT_AVG_NUM, long_num = LONG_AVG_NUM
		if(useDefault && params.containsKey(history.commodity)){
			short_num = params[history.commodity][0]
			long_num = params[history.commodity][1]
		}
		
		getTrend(
			history.getPrice(date),
			history.getAvg(date, short_num),
			history.getAvg(date, long_num)
		)
	}
	
	static main(args){
		def t = new TraditionalTrend()
		def fp = new FuturePrice()
		fp.contractList.each{
			if(it.startsWith('IF')) return
			def h = new HistoryLoader(it)
			def current = fp.getPrice(it)
			h.addTodaysPrice(current)
			def trend = t.guess(h, new Date().format('yyyy-MM-dd'))
			h.close()
			
			println it + (it.length()==7 ? "\t\t" : "\t") + trend + (t.params.containsKey(h.commodity)?"\t\t*":"")
		}
	}
}
