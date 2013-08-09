package analysis;

import org.apache.commons.math.genetics.CrossoverPolicy;
import org.apache.commons.math.genetics.GeneticAlgorithm;
import org.apache.commons.math.genetics.MutationPolicy;
import org.apache.commons.math.genetics.Population;
import org.apache.commons.math.genetics.SelectionPolicy;

import analysis.trend.Trend;

public class DetailedGeneticAlgorithm extends GeneticAlgorithm {

	public DetailedGeneticAlgorithm(CrossoverPolicy crossoverPolicy,
			double crossoverRate, MutationPolicy mutationPolicy,
			double mutationRate, SelectionPolicy selectionPolicy) {
		super(crossoverPolicy, crossoverRate, mutationPolicy, mutationRate,
				selectionPolicy);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Population nextGeneration(Population current) {
		def popu = super.nextGeneration(current)
		def best = popu.getFittestChromosome()
		println best
		popu.keep()
		
		Trend.update GA.commodity, Trend.parse(best.representation)
		
		popu
	}

}
