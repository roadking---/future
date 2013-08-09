package analysis.old
import price.*

class TrendCalc extends TraditionalTrend {
	def phases = [], trends
	def evaluation = [numOfDays:0, total:0, success:0, fail:0]
	def h
	def useDefault = false
	def days2Cover = 0
	
	TrendCalc(){}
	
	TrendCalc(String contract, startDate, endDate, SHORT_AVG_NUM=4, LONG_AVG_NUM=15, days2Cover=0){
		this.SHORT_AVG_NUM = SHORT_AVG_NUM
		this.LONG_AVG_NUM = LONG_AVG_NUM
		this.days2Cover = days2Cover
	
		h = new HistoryLoader(contract)
		init(startDate, endDate)
		h.close()
	}
	TrendCalc(HistoryLoader h, startDate, endDate, useDefault=false){
		this.h = h
		this.useDefault = useDefault
		init(startDate, endDate)
	}
	
	def init(String startDate, String endDate){
		init(Date.parse('yyyy-MM-dd',startDate), Date.parse('yyyy-MM-dd',endDate))
	}
	def init(Date startDate, Date endDate){
		trends = h.sql.rows("select deal_date from " + h.tablename + " where deal_date between ? and ? ", [startDate, endDate])
		trends.each{
			it.trend = guess(h, it.deal_date, useDefault)
		}
		
		findPhases(trends)
		if(trends.size() > 0) evaluation = evaluate()
		
	}
	
	def evaluate(){
		def solve = {
			//calc with the first 2 days
			lst, direction ->
			//the number first few days to be involved in the trading
			def MAX_SIZE = 3 //lst.size()
			def last_date = days2Cover>0 && days2Cover+MAX_SIZE<lst.size() ? lst[days2Cover+MAX_SIZE].deal_date : lst.last().deal_date
			def last_price = h.getPrice(last_date)
			def rlt = [numOfDays:0, total:0, success:0, fail:0]
			def UPPER_BOUND = lst.size()>=MAX_SIZE ? MAX_SIZE : lst.size()
			lst[0..UPPER_BOUND-1].each{
				def price = h.getPrice(it.deal_date)
				if(price>0 && last_price>0 ){
					def value = (last_price - price)*(direction == 'UP' ? 1 : -1)
					rlt.total += value
					if(value>0) rlt.success++
					if(value<0) rlt.fail++
					if(value!=0) rlt.numOfDays++
				}
			}
			rlt
		}
		def evaluate_phase = {
			phase ->
			def lst = trends.findAll{it.deal_date >= phase.start && it.deal_date <= phase.end}
			solve(lst, phase.direction)
		}
		
		def rlt = phases.collect{evaluate_phase(it)}
		if(rlt.size() > 0){
			evaluation.numOfDays = rlt.sum{it.numOfDays}
			evaluation.total = rlt.sum{it.total}
			evaluation.success = rlt.sum{it.success}
			evaluation.fail = rlt.sum{it.fail}
		}
		evaluation
	}
	
	def findPhases(trends){
		def tmp = []
		trends.each{
			def direction = it.trend.startsWith('UP') ? 'UP' : (it.trend.startsWith('DOWN') ? 'DOWN' : 'COMMON')
			tmp << [deal_date:it.deal_date, direction:direction]
		}
		def lastDirection = 'COMMON', startDate, numOfDays = 0
		tmp.each{
			numOfDays++
			if(it.direction != lastDirection){
				if(lastDirection != 'COMMON'){
					phases << [start:startDate, end:it.deal_date, direction:lastDirection, numOfDays:numOfDays]
				}
					numOfDays = 1
					lastDirection = it.direction
					startDate = it.deal_date
			}
		}
		if(lastDirection != 'COMMON')
			phases << [start:startDate, end:tmp.last().deal_date, direction:lastDirection, numOfDays:numOfDays]
	}
	
	static main(args){
		def endDate = new Date()
		def startDate = endDate - 30
		
		def fp = new FuturePrice()
		fp.contractList.each{
			if(it.startsWith('IF')) return
			def h = new HistoryLoader(it)
			if(!h.failed){
				def current = fp.getPrice(it).toDouble()
				h.addTodaysPrice(current)
				
				def tc = new TrendCalc(h, startDate, endDate, true)
				def trend = tc.guess(h, new Date().format('yyyy-MM-dd'), true)
				h.close()
				
				def percent = 0.0
				if(current>0 && tc.params.containsKey(h.commodity)){
					percent = 100*tc.params[h.commodity][2]/current
					percent = new Double(percent).round(1)
				}
				
				if(percent > 0){
					println it + (it.length()==7 ? "\t\t" : "\t") + 
						trend +
						(trend != 'COMMON' && tc.phases.size()>0 ? "\t" + tc.phases[-1].numOfDays : "\t") +
						//(tc.params.containsKey(h.commodity)?"\t\t*":"") +
						(tc.params.containsKey(h.commodity) && percent>0 ? "\t\t${percent}%" : "")  +
						(trend != 'COMMON' && tc.phases[-1].numOfDays <= 3 ? '\t<--' : '')
				}
			}
		}
	}
}