package analysis

import java.util.List;

import org.apache.commons.math.genetics.*;

class RecoverablePopulation extends ElitisticListPopulation{
	
	static def RECOVER_FILE = 'recover.txt', CACHE_FILE = 'cache', DIR = 'backup'

	static def cache = [:]
	
	def static clear(){
		new File("${DIR}/${GA.commodity}/${RECOVER_FILE}").delete()
		new File("${DIR}/${GA.commodity}/${CACHE_FILE}").delete()
	}
	
	public RecoverablePopulation(int populationLimit, double elitismRate) {
		super(populationLimit, elitismRate);
		// TODO Auto-generated constructor stub
	}

	public RecoverablePopulation(List<Chromosome> chromosomes,
			int populationLimit, double elitismRate) {
		super(chromosomes, populationLimit, elitismRate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Population nextGeneration() {
		def popu = super.nextGeneration()
		new RecoverablePopulation(popu.chromosomes, populationLimit, elitismRate)
	}

	def keep(){
		clear()
		new File("${DIR}/${GA.commodity}").mkdirs()
		
		def f = new File("${DIR}/${GA.commodity}/${RECOVER_FILE}")
		chromosomes.each{f << it << "\n"}
		
		def fc = new File("${DIR}/${GA.commodity}/${CACHE_FILE}")
		fc.newObjectOutputStream() << cache
	}
	
	static def recover(){
		cache = [:]		//clear the cache
		
		def f = new File("${DIR}/${GA.commodity}/${RECOVER_FILE}")
		if(!f.exists()) return null
		
		def parse = {line, popu ->
			def m = line =~ /f=(.+)\s+\[(.+)\]/
			def params =  m[0][2].split(',').collect {Double.parseDouble it.trim() }
			[0, 2, 4, 6, 7, 9, 10, 11].each{
				params[it] = (int) params[it]
			}
			
			def c = new Chromosome(params)
			c.fitNum = Double.parseDouble m[0][1]
			popu.addChromosome c
			}
		def popu = new RecoverablePopulation(GA.POPULATION_LIMIT, GA.ELITISM_RATE)
		f.eachLine {parse(it, popu)}
		
		def fc = new File("${DIR}/${GA.commodity}/${CACHE_FILE}")
		cache = fc.newObjectInputStream().readObject()
		popu
	}
}
