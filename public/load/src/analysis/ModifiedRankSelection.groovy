package analysis

import org.apache.commons.math.genetics.ChromosomePair;
import org.apache.commons.math.genetics.GeneticAlgorithm;
import org.apache.commons.math.genetics.Population;

class ModifiedRankSelection extends RankSelection{

	ModifiedRankSelection(int arity){
		super(arity)
	}
	
	@Override
	public ChromosomePair select(Population population) {
		def clst = buildRankList(population.getChromosomes())
		
		def extent_lst = []
		population.getChromosomes().each{extent_lst << it.representation[0,2]}
		extent_lst.unique()
		
		extent_lst.remove extent_lst.max{extent -> 
			clst.count{it.representation[0] == extent[0] && it.representation[2] == extent[1]}
		}
		
		def minority = extent_lst.collect{extent ->
			clst.findAll{it.representation[0] == extent[0] && it.representation[2] == extent[1]}
		}
		if(minority.size() > 0){
			int count = clst.size() / 2
			while(count-- > 0){
				def target = minority[count % minority.size()]
				if(target.size() > 0){
					def idx = GeneticAlgorithm.getRandomGenerator().nextInt(target.size())
					clst << target[idx]
				}
			}
		}
		return new ChromosomePair(tournament(clst), tournament(clst))
	}

}
