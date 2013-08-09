package analysis

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.regression.SimpleRegression;

import analysis.trend.Decision;
import analysis.trend.OldTrend;
import analysis.trend.CachedTrend;
import analysis.trend.Trend;

import price.HistoryLoader;

class StatsExpr {
	def start, end
	
	static def pool, THREAD_POOL = 2
	
	static startExpr = '2010-1-1', endExpr = '2011-12-2'
	static DAYS_IN_TEST = Date.parse('yyyy-MM-dd', endExpr) - Date.parse('yyyy-MM-dd', startExpr)
	static ratio = 0.014, CUMMULATIVE_CONST = [
		TA:10000, SR:7000, CF:15000, ER:2600, WS:2600,
		RO:10000, L:10000, M:3400, A:4700, P:9000,
		C:2350, V:7800, J:2200, RU:33000, ZN:17000,
		RB:4800, CU:67000, AL:17000, FU:4900, PB:16000,
		AU:388, ME:3050,
	]

	static main(args) {
		//def rlt = se.run('SR200905', win_alg)
		//def rlt = se.run(pool, ['SR200905'], win_alg)
		def rlt = doExpr('fu'.toUpperCase())
		//def rlt = doExpr('A', [11, 0.78, 39, -0.18, 46, 0.17, 1, 18, -0.04, 1])
		pool?.shutdown()
		
		rlt.details.each{it.each{println it}}
		
		println ""
		println "WIN Percents:\t${rlt.freq_win.round(2)}"
		println "WIN Percents UP:\t${rlt.up_freq_win.round(2)}"
		println "WIN Percents DOWN:\t${rlt.down_freq_win.round(2)}"
		println ""
		println "LOSS Percents:\t${rlt.freq_loss.round(2)}"
		println "LOSS Percents UP:\t${rlt.up_freq_loss.round(2)}"
		println "LOSS Percents DOWN:\t${rlt.down_freq_loss.round(2)}"
		println ""
		println "Number:\t${rlt.freq.getSumFreq()}"
		println "Fitness:\t${rlt.fitness}"
		println "Mean Pct:${rlt.mean_pct}\tStd Dev Pct:${rlt.stddev_pct}"
	}

	static def doExpr(commodity, p = null){
		if(pool == null) pool = Executors.newFixedThreadPool(THREAD_POOL)
		CUMMULATIVE_CONST.CURRENT = CUMMULATIVE_CONST[commodity] * ratio
		
		if(p != null)
			Trend.params[commodity] = Trend.parse(p)
		def se = new StatsExpr(start:startExpr, end:endExpr)
		def win_alg = {trend, date ->
			trend.win(date)
		}
		
		//def rlt = se.run(pool, ['SR201203'], win_alg)
		def rlt = se.run(pool, HistoryLoader.loadContractList(commodity), win_alg)
//		pool.shutdown()
		rlt
	}
	
	
	def run(String contract, algorithm){
		def h = new HistoryLoader(contract)
		def rlt = []
		def (date, endDate) = h.getDealDateExtent(start, end)
		def trend = new CachedTrend(history:h)
		trend.load()
		while(date <= endDate){
			def price = h.getPrice(date)
			if(price != null && price > 0){
				def d = new Decision(date:date, price:price, trend:trend, holds:h.getHolds(date),
					algorithm:algorithm)
				if(d.decision) rlt << d
			}
			date = date.next()
		}
		h.close()
		rlt
	}
	
	def modifyStats(date, stats){
		def slope = (2-1)/DAYS_IN_TEST
		def d = Date.isInstance(date) ? date : Date.parse('yyyy-MM-dd', date)
		def x = d - Date.parse('yyyy-MM-dd', startExpr)
		def y = slope * x + 1
		y * stats
	}
	
	def run(pool, List contracts, algorithm){
		def result = [:]
		def lst = contracts.collect{
			pool.submit(
				new RunTask( 
					contract:it, start:start, end:end, algorithm:algorithm))
			}
		result.details = lst.collect { it.get() }
		
		result.freq = new Frequency(); result.up_freq = new Frequency(); result.down_freq = new Frequency();
		result.stats = new SummaryStatistics(); result.up_stats = new SummaryStatistics(); result.down_stats = new SummaryStatistics();
		result.modified_up_stats = new SummaryStatistics(); result.modified_down_stats = new SummaryStatistics();
		result.details.each{
			it.each{
				if(it.direction == 'UP'){
					result.up_freq.addValue(it.result)
					result.up_stats.addValue(it.result)
					result.modified_up_stats.addValue(modifyStats(it.date, it.result))
				} else {
					result.down_freq.addValue(it.result)
					result.down_stats.addValue(it.result)
					result.modified_down_stats.addValue(modifyStats(it.date, it.result))
				}
				result.freq.addValue(it.result)
				result.stats.addValue(it.result)
			}
		}
		result.mean_pct = Math.round(1000 * result.stats.mean * ratio / CUMMULATIVE_CONST.CURRENT) / 1000
		result.stddev_pct = Math.round(1000 * result.stats.standardDeviation * ratio / CUMMULATIVE_CONST.CURRENT) / 1000
		
		def nvl = {value -> value == Double.NaN ? 0d : value}
		
		result.freq_win = nvl(1 - result.freq.getCumPct(CUMMULATIVE_CONST.CURRENT)) 
		result.up_freq_win = nvl(1 - result.up_freq.getCumPct(CUMMULATIVE_CONST.CURRENT))
		result.down_freq_win = nvl(1 - result.down_freq.getCumPct(CUMMULATIVE_CONST.CURRENT))
		
		result.freq_loss = nvl(result.freq.getCumPct(-1 * CUMMULATIVE_CONST.CURRENT))
		result.up_freq_loss = nvl(result.up_freq.getCumPct(-1 * CUMMULATIVE_CONST.CURRENT))
		result.down_freq_loss = nvl(result.down_freq.getCumPct(-1 * CUMMULATIVE_CONST.CURRENT))
		
//		def up_fitness = result.up_freq_win <= result.up_freq_loss ? 0 : nvl(result.up_stats.sum) * (result.up_freq_win - result.up_freq_loss)
//		def down_fitness = result.down_freq_win <= result.down_freq_loss ? 0 : nvl(result.down_stats.sum) * (result.down_freq_win - result.down_freq_loss)
		def up_fitness = result.up_freq_win <= result.up_freq_loss ? 0 : 
			nvl(result.modified_up_stats.sum) * nvl(result.modified_up_stats.mean) * (result.up_freq_win - result.up_freq_loss)
		def down_fitness = result.down_freq_win <= result.down_freq_loss ? 0 : 
			nvl(result.modified_down_stats.sum) * nvl(result.modified_down_stats.mean) * (result.down_freq_win - result.down_freq_loss)
		result.fitness = up_fitness + down_fitness
		
//		result.fitness = nvl(result.stats.sum)
		result
	}
}

class RunTask implements Callable{
	def contract, start, end, algorithm
	def call(){
		new StatsExpr(start:start, end:end).run(contract,algorithm)
	}
}
