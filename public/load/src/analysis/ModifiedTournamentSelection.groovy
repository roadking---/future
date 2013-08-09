package analysis

import org.apache.commons.math.genetics.ChromosomePair;
import org.apache.commons.math.genetics.GeneticAlgorithm;
import org.apache.commons.math.genetics.ListPopulation;
import org.apache.commons.math.genetics.Population;
import org.apache.commons.math.genetics.SelectionPolicy;

class ModifiedTournamentSelection implements SelectionPolicy {
	
	def arity
	
	ModifiedTournamentSelection(int arity){
		this.arity = arity;
	}
	
	def static main(args){
		def lst = [8, 8, 9, 4]
		println lst.pop()
		println lst
		println lst.pop()
		println lst
		println lst.pop()
		println lst
		println lst.pop()
		println lst
	}

	@Override
	public ChromosomePair select(Population population) {
		def chromosomes = new ArrayList(population.getChromosomes())
		def fittest = population.getFittestChromosome()
		def flst = population.chromosomes.findAll { it.isSame(fittest) }
		
		def max = chromosomes.size()/3
		int remove = flst.size() - max
		remove = remove > 0 ? remove : 0
		remove = chromosomes.size() - remove >= arity ? remove : (chromosomes.size() - arity)/2
		remove = remove > 0 ? remove : 0
		while(remove-- > 0) chromosomes.remove(flst.pop())
		
		return new ChromosomePair(tournament(chromosomes), tournament(chromosomes));
	}

	protected Chromosome tournament(chromosomesList) {
		if(chromosomesList.size() < arity)
			throw new IllegalArgumentException("Tournament arity cannot be bigger than population size.");
		def tournamentPopulation = new ListPopulation(arity) {
			public Population nextGeneration(){return null;}
		};
		List chromosomes = new ArrayList(chromosomesList);
		for(int i = 0; i < arity; i++){
			int rind = GeneticAlgorithm.getRandomGenerator().nextInt(chromosomes.size());
			tournamentPopulation.addChromosome((Chromosome)chromosomes.get(rind));
			chromosomes.remove(rind);
		}

		return tournamentPopulation.getFittestChromosome();
	}
}
