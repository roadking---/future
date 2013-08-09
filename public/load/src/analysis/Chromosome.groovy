package analysis

import java.io.Serializable;

import org.apache.commons.math.genetics.*

import analysis.trend.Trend;

class Chromosome extends AbstractListChromosome {
	
	//def config
	public Chromosome(List repr) {
		super(repr);
	//	config = parse(representation)
		if(null != RecoverablePopulation.cache[cacheKey])
			fitNum = RecoverablePopulation.cache[cacheKey]
	}

	@Override
	protected boolean isSame(Chromosome another) {
		cacheKey.equals(another.cacheKey)
	}

	def fitNum = null
	
	@Override
	public double fitness() {
		if(fitNum != null) return fitNum

		//println "calc ${representation}"
		fitNum = StatsExpr.doExpr(GA.commodity, representation).fitness
		RecoverablePopulation.cache[cacheKey] = fitNum
		fitNum
	}

	public AbstractListChromosome newFixedLengthChromosome(List rep){
		new Chromosome(rep)
	}
	
	protected void checkValidity(List rep) throws InvalidRepresentationException{
		def p = Trend.parse(rep)
		if(p.up_extent <= 0 || p.up < 0 || p.down_extent <= 0 || p.down >0
			|| p.revert_up_extent <= 0 || p.revert_up_param < 0 || p.revert_up_delay < 0
			|| p.revert_down_extent <= 0 || p.revert_down_param > 0 || p.revert_down_delay < 0
			|| p.first_n_days_up <= 0 || p.first_n_days_down <= 0)
			throw new InvalidRepresentationException(rep.toString())
	}
	
	
	
	def getCacheKey(){
		representation.join('&')
	}
}

