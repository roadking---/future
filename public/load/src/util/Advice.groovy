package util

import analysis.StatsExpr;
import price.FuturePrice;

class Advice extends Holds {
	def getProbability(commodities){
		def p = new Probability(commodities).rlt
		StatsExpr.pool?.shutdown()
		p
	}
	
	def getFilteredTrends(){
		def lst = new CurrentTrend(fp:new FuturePrice()).analyzeAll()
		def flt = lst.findAll { it.action == 'ADD' }
		flt.addAll lst.findAll{null != holds[it.contract]}
		flt.unique()
//		flt.find {it.contract=='M201205'}.action = 'REVERT'
//		flt.find {it.contract=='V201204'}.action = 'REVERT'
//		flt.find {it.contract=='CU201109'}.action = 'REVERT'
		flt
	}
	
	def rank(){
		def lst = filteredTrends
		
		def rlst = lst.findAll { it.action == 'REVERT' }
		lst.removeAll rlst
		def alst = lst.findAll { it.action == 'ADD' }
		def hlst = lst.findAll { null != holds[it.contract] }
		
		if(alst.size() > 0){
			def commodities = alst.contract.collect{(it =~ /(\D+)(\d+)/)[0][1]}.unique()
			def prob = getProbability(commodities)
			alst.sort{
				def c = (it.contract =~ /(\D+)(\d+)/)[0][1]
				def p = prob.find { it.commodity == c }
				def freq_win = it.trend=='UP' ? p.result.up_freq_win : p.result.down_freq_win
				freq_win = freq_win.round(2)
				def freq_loss = it.trend=='UP' ? p.result.up_freq_loss : p.result.down_freq_loss
				freq_loss = freq_loss.round(2)
				it.comment = "${freq_win}-${freq_loss} | ${p.result.mean_pct}/${p.result.stddev_pct}"
				-1 * (freq_win - freq_loss) * p.result.mean_pct/p.result.stddev_pct
			}
		}
		
		[REVERT:rlst, ADD:alst, HOLDS:hlst]
	}
	static main(args){
		def a = new Advice()
		def rlt = a.rank()
		if(rlt.REVERT.size() > 0)
			println "REVERT ->\t" + rlt.REVERT.contract.join(", ") + "\n"
		
		if(rlt.HOLDS.size() > 0){
			println "HOLDS ->"
			rlt.HOLDS.each { println "${it.contract}\t${it.trend}-${it.seq}" }
			println ""
		}
		
		if(rlt.ADD.size() > 0){
			println "ADD ->"
			rlt.ADD.each { println "${it.contract}\t${it.trend}-${it.seq}\t\t\t${it.comment}" }
		}
	}
}
