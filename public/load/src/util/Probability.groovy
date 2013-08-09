package util

import analysis.StatsExpr;

class Probability {
	def rlt
	Probability(commodities){
		rlt = commodities.collect {
			[commodity:it, result:StatsExpr.doExpr(it)]
		}
	}
	static main(args) {
		//def commodities = 'ER PB RO J'.split(' ')
		def commodities = 'CF ER RO SR WS AU L A M C TA J V RB PB FU RU CU ZN P'.split(' ')
		
		new Probability(commodities).rlt.sort{-it.result.mean_pct/it.result.stddev_pct}.each{
			println "${it.commodity}\t${it.result.mean_pct}\t${it.result.stddev_pct}\t"
		}

//		new Probability(commodities).rlt.sort{it.result.freq_loss - it.result.freq_win}.each{
//			println "${it.commodity}\t${it.result.freq_win.round(2)}\t${it.result.freq_loss.round(2)}\t${it.result.freq.getSumFreq()}"
//		}
		
//		println([	'commodity',
//			
//					'win-pct', 
//					'win-pct-up',
//					'win-pct-down',
//					
//					'loss-pct',
//					'loss-pct-up',
//					'loss-pct-down',
//					
//					'net-pct',
//					'net-pct-up',
//					'net-pct-down',
//					
//					'total-nbr',
//					'up-nbr',
//					'down-nbr',
//					
//					'mean',
//					'stddev',
//					].join("\t"))
//		new Probability(commodities).rlt.sort{it.commodity}.each{
//			println ([	it.commodity,
//				
//						it.result.freq_win.round(2),
//						it.result.up_freq_win.round(2),
//						it.result.down_freq_win.round(2),
//						
//						it.result.freq_loss.round(2),
//						it.result.up_freq_loss.round(2),
//						it.result.down_freq_loss.round(2),
//						
//						it.result.freq_win.round(2) - it.result.freq_loss.round(2),
//						it.result.up_freq_win.round(2) - it.result.up_freq_loss.round(2),
//						it.result.down_freq_win.round(2) - it.result.down_freq_loss.round(2),
//						
//						it.result.freq.getSumFreq(),
//						it.result.up_freq.getSumFreq(),
//						it.result.down_freq.getSumFreq(),
//						
//						it.result.mean_pct,
//						it.result.stddev_pct,
//					].join("\t"))
//		}
		StatsExpr.pool?.shutdown()
	}
}
