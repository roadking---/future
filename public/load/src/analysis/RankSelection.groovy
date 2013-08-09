package analysis

import org.apache.commons.math.genetics.ChromosomePair;
import org.apache.commons.math.genetics.Population;
import org.apache.commons.math.genetics.SelectionPolicy;
import org.apache.commons.math.stat.ranking.NaNStrategy;
import org.apache.commons.math.stat.ranking.NaturalRanking;
import org.apache.commons.math.stat.ranking.TiesStrategy;

class RankSelection extends ModifiedTournamentSelection {

	RankSelection(int arity){
		super(arity)
	}
	
	@Override
	public ChromosomePair select(Population population) {
		def clst = buildRankList(population.getChromosomes())
		return new ChromosomePair(tournament(clst), tournament(clst))
	}

	def buildRankList(chromosomes){
		def lst = []
		lst.addAll(chromosomes)
		
		def flst = lst.collect { it.fitness() }
		flst.unique()
		
		def ranking = new NaturalRanking(NaNStrategy.MINIMAL, TiesStrategy.MAXIMUM);
		def ranks = ranking.rank((double[])flst.toArray());

		def clst = []
		ranks.eachWithIndex { it, idx ->
			def find = lst.findAll { it.fitness() == flst[idx] }
			
			while(find.size() < it) find.addAll(find)
			
			if(find.size() > it) find = find[0 .. it-1]
			clst.addAll(find)
		}
		clst
	}
	
}
