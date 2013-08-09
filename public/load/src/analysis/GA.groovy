package analysis

import org.apache.commons.math.genetics.*;
import analysis.trend.Trend;

class GA {
	static def ARITY = 3, NUM_GENERATIONS = 2000, ELITISM_RATE = 0.1
	
	//It may be surprising, that very big population size usually does not improve 
	//performance of GA (in meaning of speed of finding solution). Good population size is about 20-30, 
	//however sometimes sizes 50-100 are reported as best. Some research also shows, 
	//that best population size depends on encoding, on size of encoded string. 
	//It means, if you have chromosome with 32 bits, the population should be say 32, 
	//but surely two times more than the best population size for chromosome with 16 bits.
	static def POPULATION_LIMIT = 60
	
	//Crossover rate generally should be high, about 80%-95%. 
	//(However some results show that for some problems crossover rate about 60% is the best.)
	static def CROSSOVER_RATE = 0.6
	
	//On the other side, mutation rate should be very low. Best rates reported are about 0.5%-1%
	static def MUTATION_RATE = 0.1
	
	
	static def commodity = 'CF'
	
	def run(){
		def ga = new DetailedGeneticAlgorithm(
			new OnePointCrossover(), CROSSOVER_RATE,
			new MutationPolicy(), MUTATION_RATE,
			new ModifiedRankSelection(ARITY))
		def stopCond = new FixedGenerationCount(NUM_GENERATIONS)
		
		def finalPopulation = ga.evolve(initialPopulation, stopCond)
		
		def bestFinal = finalPopulation.getFittestChromosome()
		bestFinal
	}
	
	def getInitialPopulation(){
		def initial = RecoverablePopulation.recover()
		if(initial != null) return initial
		
		initial = new RecoverablePopulation(POPULATION_LIMIT, ELITISM_RATE)
		[	[4, 0.4, 6, -0.4, 4, 0.2, 3, 4, -0.2, 3, 3, 3],
			[8, 0.2, 5, -0.2, 4, 0.2, 2, 4, -0.3, 1, 3, 3],
			[16, 0.3, 12, -0.2, 6, 0.3, 2, 4, -0.52, 0, 3, 3],
			[9, 0.1, 6, -0.1, 4, 0.4, 0, 4, -0.1, 1, 3, 3],
			[21, 0.28, 5, -0.87, 6, 0.67, 2, 16, -0.78, 1, 3, 3],
			[20, 0.29, 8, -0.08, 5, 0.49, 1, 16, -0.12, 2, 3, 3],
			[16, 0.16, 20, -0.2, 4, 1, 2, 4, -0.2, 2, 3, 3],
			[25, 0.28, 21, -0.27, 16, 0.32, 7, 14, -0.43, 3, 3, 3],
			[11, 0.78, 39, -0.18, 46, 0.17, 1, 18, -0.04, 1, 3, 3],
			].each { initial.addChromosome new Chromosome(it) }
		
		Trend.params?.each{c,p->
			initial.addChromosome new Chromosome(p.values().toList())
		}
		initial
	}

	static main(args) {
		//RecoverablePopulation.DIR = 'old'
		//RecoverablePopulation.clear()
		def best = new GA(commodity:commodity).run()
		println "\n----------------"
		println best
		
		StatsExpr.pool?.shutdown()
	}
}
