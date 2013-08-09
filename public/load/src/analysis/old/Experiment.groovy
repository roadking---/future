package analysis.old
import price.*

class Experiment{
	def start, end
	def allResult = []
	
	def run(List contracts, SHORT_AVG_NUM=3, LONG_AVG_NUM=15, days2Cover=0){
		def rlt = contracts.collect{
			System.gc()
			println "${it} ${SHORT_AVG_NUM} ${LONG_AVG_NUM} ${days2Cover}"
			new TrendCalc(it, start, end, SHORT_AVG_NUM, LONG_AVG_NUM, days2Cover).evaluation
		}
		
		allResult << [	numOfDays : rlt.sum{it.numOfDays}, 
						total : rlt.sum{it.total}, 
						success : rlt.sum{it.success}, 
						fail : rlt.sum{it.fail},
						SHORT_AVG_NUM:SHORT_AVG_NUM,
						LONG_AVG_NUM:LONG_AVG_NUM,
						D2C:days2Cover]
		
	}
	
	def run(List contracts, List params){
		params.each{
			run(contracts, it.SHORT_AVG_NUM, it.LONG_AVG_NUM, it.D2C)
		}
		allResult.sort{it.numOfDays==0? 0 : -it.total/it.numOfDays}
	}
	
	def showAll(commodity){
		println commodity
		allResult.each{
			it.performance = it.numOfDays == 0 ? 0 : new Double(it.total/it.numOfDays).round(2)
			println "${it.SHORT_AVG_NUM}/${it.LONG_AVG_NUM}/${it.D2C}:\tS/F ${it.success}/${it.fail}\t${it.performance}"
		}
		println "Best Solution:\n" + allResult.first()
		println "-----------------------------------------"
		
		
		def report = new File("report_${commodity}.txt")
		if(report.exists()) report.delete()
		
		report << commodity << "\n"
		allResult.each{report << "${it.SHORT_AVG_NUM}/${it.LONG_AVG_NUM}/${it.D2C}:\tS/F ${it.success}/${it.fail}\t${it.performance}" << "\n"}
		def best = allResult.first()
		report << "Best Solution:\n" << "${best.SHORT_AVG_NUM}/${best.LONG_AVG_NUM}/${best.D2C}:\tS/F ${best.success}/${best.fail}\t${best.performance}" << "\n"
		report << "-----------------------------------------" << "\n\n\n"
	}
	
	static main(args){
		def try_once = {commodity, params ->
			def contracts = HistoryLoader.loadContractList(commodity)
			def e = new Experiment(start:'2008-1-1', end:'2011-5-4')
			//def e = new Experiment(start:'2011-1-1', end:'2011-5-4')
			e.run(contracts, params)
			e.showAll(commodity)
			e = null
		}
		
		def params = []
		(3..15).each{short_num ->
			(3..30).each{long_num ->
				(0..0).each{days2Cover->
				//([0,4,5,6]).each{days2Cover->
					if(short_num < long_num)
						params << [SHORT_AVG_NUM:short_num, LONG_AVG_NUM:long_num, D2C:days2Cover]
					}
			}
		}
		
		def commodities = 'ER CF RO SR TA WS WT A AL B C Y M J P V L AU WR FU RU CU ZN PB RB'.split().toList().reverse()
		//def commodities = 'ER'.split().toList().reverse()
		def pool_size = 3, pool = []
		
		while(commodities.size() > 0){
			println "T:\t" + (Thread.activeCount()-1)
			while(Thread.activeCount() < pool_size+1){
				if(commodities.size() == 0) break
				
				def c = commodities.pop()
				pool << Thread.start{try_once(c, params)}
			}
			System.sleep(5000)
		}
		pool*.join()
	}
}