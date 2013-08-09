package analysis.old
import price.*
class Experiment2{
	def startDate = '2008-10-1', endDate = '2011-5-4'
	def rltlst, best
	Experiment2(commodity){
		def contracts = HistoryLoader.loadContractList(commodity)
		def rltlst = (1..20).collect{try_once(contracts,it)}
		rltlst.removeAll{it.numOfDays < 40}
		rltlst.sort{-it.performance}
		if(rltlst.size()>0)best = rltlst.first()
	}
	
	def try_once(contracts, n){
		def rlt = [numOfDays:0, total:0, success:0, fail:0]
		contracts.each{
			def tc2 = new TrendCalc2(it, startDate, endDate, n)
			rlt.numOfDays += tc2.evaluation.numOfDays
			rlt.total += tc2.evaluation.total
			rlt.success += tc2.evaluation.success
			rlt.fail += tc2.evaluation.fail
		}
		rlt.performance = rlt.numOfDays == 0 ? 0 : new Double(rlt.total/rlt.numOfDays).round(2)
		rlt.n = n
		rlt
	}
	
	static main(args){
		'ER CF RO SR TA WS WT A B C Y M J P V L AU WR FU RU CU ZN PB RB'.split().each{
			def e = new Experiment2(it)
			println "${it} ${e.best.n}:\t${e.best.performance} X ${e.best.numOfDays}\t${e.best.success}:${e.best.fail}"
		}
		
		return
		
		def e = new Experiment2('A')
		e.rltlst.each{
			println "${it.n}:\t${it.performance} X ${it.numOfDays}\t${it.success}:${it.fail}"
		}
		println "\nThe Best:"
		println "${e.best.n}:\t${e.best.performance} X ${e.best.numOfDays}\t${e.best.success}:${e.best.fail}"
	}
}

class TrendCalc2 extends TrendCalc{
	def numOfDays
	TrendCalc2(String contract, startDate, endDate, numOfDays){
		this.numOfDays = numOfDays
		this.h = new HistoryLoader(contract)
		super.init(startDate, endDate)
		this.h.close()
	}
	
	def evaluate(){
		def solve = {
			//calc with the first 2 days
			lst, direction ->
			def rlt = [numOfDays:0, total:0, success:0, fail:0]
			if(lst.size() <= numOfDays) return rlt
			
			def last_price = h.getPrice(lst.last().deal_date)
			lst[numOfDays-1 .. numOfDays-1].each{
				def price = h.getPrice(it.deal_date)
				def value = (last_price - price)*(direction == 'UP' ? 1 : -1)
				//println "${it.deal_date} ${price} ${value} ${direction} -> ${lst.last().deal_date} ${last_price}"
				
				rlt.total += value
				if(value>0) rlt.success++
				if(value<0) rlt.fail++
				if(value!=0) rlt.numOfDays++
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
}