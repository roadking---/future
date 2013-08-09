package analysis

import org.apache.commons.math.distribution.ExponentialDistributionImpl;
import org.apache.commons.math.distribution.PoissonDistributionImpl;
import org.apache.commons.math.genetics.Chromosome;
import org.apache.commons.math.genetics.GeneticAlgorithm;

class MutationPolicy implements org.apache.commons.math.genetics.MutationPolicy {

	@Override
	public Chromosome mutate(Chromosome initial) {
		def repr = initial.representation;
		(0 .. poisson.sample()).each{ repr = mutateOnce(repr) }
		initial.newFixedLengthChromosome(repr)
	}
	
	def mutateOnce(repr){
		def rInd = GeneticAlgorithm.getRandomGenerator().nextInt(repr.size())
		def newRepr = []
		newRepr.addAll(repr)
		switch(rInd){
			case 0: randomHappen(
				0.7, 
				{newRepr[0] = randomExtent(newRepr[0], 3, 0.2)},
				{newRepr[0] = randomExtent(newRepr[0], 3, 0.2); newRepr[2] = randomExtent(newRepr[0], 3, 0.2);}
				); 
				break;
			case 1: newRepr[rInd] = randomSlope(); break;
			case 2: randomHappen(
				0.7, 
				{newRepr[2] = randomExtent(newRepr[2], 3, 0.2)},
				{newRepr[0] = randomExtent(newRepr[2], 3, 0.2); newRepr[2] = randomExtent(newRepr[2], 3, 0.2);}
				);  break;
			case 3: newRepr[rInd] = -randomSlope(); break;
			case 4: newRepr[4] = randomExtent(newRepr[4], 2, 0.2); break;
			case 5: newRepr[5] = randomSlope(); break;
			case 6: newRepr[rInd] = GeneticAlgorithm.getRandomGenerator().nextInt(9); break
			case 7: newRepr[7] = randomExtent(newRepr[7], 2, 0.2); break;
			case 8: newRepr[8] = -randomSlope(); break;
			case 9: newRepr[rInd] = GeneticAlgorithm.getRandomGenerator().nextInt(9); break
			case 10: newRepr[rInd] = 1 + GeneticAlgorithm.getRandomGenerator().nextInt(40); break
			case 11: newRepr[rInd] = 1 + GeneticAlgorithm.getRandomGenerator().nextInt(40); break
		}
		newRepr
	}
	
	def randomSlope(){
		def randomBetween = { down_limit, up_limit ->
			down_limit + (up_limit - down_limit) * GeneticAlgorithm.getRandomGenerator().nextDouble()
		}
		def r = GeneticAlgorithm.getRandomGenerator().nextDouble()
		double slope = 0
		[	[prob:0.3, up_limit:0.15, down_limit:0],
			[prob:0.5, up_limit:0.5, down_limit:0.15],
			[prob:0.2, up_limit:2, down_limit:0.5],].each{
				r -= it.prob
				if(r <= 0 && r + it.prob >= 0 )
					slope = randomBetween(it.down_limit, it.up_limit)
			}
		round(slope)
	}
	
	def randomHappen(ratio, event1, event2){
		GeneticAlgorithm.getRandomGenerator().nextInt(100) < ratio*100 ? event1() : event2()
	}
	
	def randomExtent(mean, dev, ratio){
		if(GeneticAlgorithm.getRandomGenerator().nextInt(100) < ratio*100){
			def nearby = mean + GeneticAlgorithm.getRandomGenerator().nextInt(2*dev+1) - dev
			if(nearby >= 3 && nearby != mean) return nearby
		}
		GeneticAlgorithm.getRandomGenerator().nextInt(48) + 3
	}

	def round(data){
		Math.round(100*data)/100
	}
	
	def static exp = new ExponentialDistributionImpl(1.42)
	def static poisson = new PoissonDistributionImpl(0.85)
	
	def randomAncle(min, max){
		def r = GeneticAlgorithm.getRandomGenerator().nextDouble()
		r = r*(max - min) + min
	}
	
	def static main(args){
		println new MutationPolicy().randomSlope()
	}
}
