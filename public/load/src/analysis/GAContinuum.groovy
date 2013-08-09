package analysis

import analysis.trend.Trend;

class GAContinuum extends GA {
	static commodities = 'CF ER RO SR WS AU L A M C TA J V RB PB FU RU CU ZN P'.split(' ')
	//static commodities = 'CU'.split(' ')
	
	static main(args) {
		Trend.params.each{k,v-> println "${k}\t${v.values()}"}
		//return
		
		def rand = new Random()
		def c, theSame = true, lastFitness = 0
		while(true){
			if(theSame){
				c = commodities[rand.nextInt(commodities.size())]
				NUM_GENERATIONS = 3
				lastFitness = 0
			} else{
				NUM_GENERATIONS = 10
			}
			println "${c}"
			
			Trend.params = new File(Trend.PARAMS_FILE).newObjectInputStream()?.readObject()
			def formerBest = Trend.params[c]
			def best = new GAContinuum(commodity:c).run()
			def p = Trend.parse(best.representation)
			
			theSame = true
			if(formerBest == null) theSame = false
			else p.each{k, v ->
					theSame = theSame && v == formerBest[k]
				}
			theSame = theSame || (lastFitness != 0 && best.fitness() == lastFitness)
			if(!theSame){
				println "updated!"
				
			}
			lastFitness = best.fitness()
			println ""
		}
		
		StatsExpr.pool?.shutdown()
	}
}
