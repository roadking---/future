_ = require 'underscore'
fs = require 'fs'
experiment = require './experiment'
trend = require './trend'
date = require 'date'
cache = {}


currentCommodity = null
exports.start = '20120601'
exports.end = new Date().toString('yyyyMMdd')

exports.param2repr = param2repr = (param)->
	[
		param.UP.extent
		param.UP.threshold
		param.UP.revert_extent
		param.UP.revert_param
		param.UP.revert_delay
		param.DOWN.extent
		param.DOWN.threshold
		param.DOWN.revert_extent
		param.DOWN.revert_param
		param.DOWN.revert_delay
	]
exports.repr2param = repr2param = (repr)->
	start: exports.start
	end: exports.end
	UP:
		extent:	repr[0]
		threshold:	repr[1]
		revert_extent:	repr[2]
		revert_param:	repr[3]
		revert_delay:	repr[4]
	DOWN:
		extent:	repr[5]
		threshold:	repr[6]
		revert_extent:	repr[7]
		revert_param:	repr[8]
		revert_delay:	repr[9]

class Chromosome
	constructor: (@repr_list) -> 
		@fit = null
		if _.isNumber @repr_list
			throw new Error()
	
	repr: -> @repr_list
	
	equals: (another) -> @key() is another.key()
	
	key: -> @repr().join ','
	
	toString: ->
		repr = @repr()
		repr[0...repr.length/2].join(", ") + " | " + repr[repr.length/2..].join(", ")
			
	fitness: ->
		return @fit if @fit?
		return cache[@key()] if cache[@key()]?
		
		#cache[@key()] = @fit = _.reduce @repr(), (memo, x)-> memo+x		#test
		@result = experiment.run(currentCommodity, repr2param(@repr()))
		delete @result.trades
		cache[@key()] = @fit = @result.fitness
		@fit = -1 if _.isNaN @fit
		@fit

exports.Chromosome = Chromosome

class ListPopulation
	constructor: (@chromosomes, @populationLimit) ->
	
	getPopulationSize: -> @chromosomes.length
	
	add: (chromosome) -> 
		throw 'add null chromosome' if not chromosome?
		@chromosomes.push chromosome
	
	snapshot: -> _.map @chromosomes, (c)->c.repr()
	
	getFittestChromosome: ->
		_.reduce @chromosomes, (best, c)->
			if isNaN(best.fitness()) or best.fitness() < c.fitness() then c else best

class ElitisticListPopulation extends ListPopulation
	constructor: (@chromosomes, @populationLimit, @elitismRate) ->
	
	nextGeneration: ->
		next = new ElitisticListPopulation [], @populationLimit, @elitismRate
		oldChromosomes = _.chain(@chromosomes).clone().sortBy((x)->x.fitness()).value()
		
		boundIndex = Math.ceil (1 - @elitismRate) * oldChromosomes.length
		next.add oldChromosomes[i] for i in [boundIndex ... oldChromosomes.length]
		next


class RankSelection
	constructor: (@arity) ->
	
	buildRankList: (chromosomes) ->
		flst = _.chain(chromosomes).map((c)->c.fitness()).uniq().value()
		_.chain(chromosomes).map((c)->c.fitness()).uniq().sortBy((f)->-f).map((f, i)->
			find = _.filter chromosomes, (c)-> c.fitness() is f
			if find.length < rank = i+1
				_.times rank-find.length, (n)-> find.push find[_.random find.length-1]
			find
			).flatten().value()
	
	tournament: (chromosomes) ->
		if chromosomes.length < @arity
			throw 'Tournament arity cannot be bigger than population size.'
			
		popu = new ListPopulation [], @arity
		_.times @arity, (n)->
			rind = _.random chromosomes.length-1
			popu.add chromosomes[rind]
			chromosomes = (c for c, idx in chromosomes when idx isnt rind)
		popu.getFittestChromosome()

	select: (population) ->
		clst = @buildRankList population.chromosomes
		
		try
			[@tournament(clst), @tournament(clst)]
		catch e
			[@tournament(population.chromosomes), @tournament(population.chromosomes)]


class FixedGenerationCount
	constructor: (@maxGenerations) -> @numGenerations = 0
	
	isSatisfied: (population) -> @numGenerations++ >= @maxGenerations

class OnePointCrossover		
	crossover: (pair) ->
		ridx = _.random 1, pair[0].repr().length-2
		repr0 = _.clone pair[0].repr()
		repr1 = _.clone pair[1].repr()
		repr0[0...ridx] = pair[1].repr()[0...ridx]
		repr1[0...ridx] = pair[0].repr()[0...ridx]
		[new Chromosome(repr0), new Chromosome(repr1)]

class MutationPolicy
	mutate: (chromosome) ->
		_.times _.random(1,3), (n)=> chromosome = @mutateOnce chromosome
		chromosome
		
	mutateOnce: (chromosome) ->
		repr =  chromosome.repr()
		ridx = _.random chromosome.repr().length-1
		if ridx in [0, 2, 4, 5, 7, 9]
			scope = if Math.random() < .4 then 20 else 8
			repr[ridx] += scope * (Math.random() - .5)
			repr[ridx] = Math.round repr[ridx]
		else if ridx in [1, 3, 6, 8]
			scope = if Math.random() < .4 then .1 else .02
			repr[ridx] += scope * (Math.random() - .5)
			repr[ridx] = Math.round(repr[ridx] * 1000) / 1000
		new Chromosome repr

class exports.GA extends require('events').EventEmitter
	constructor: (@commodity, @histories) ->
		@generationsEvolved = 0
		
		[@crossoverRate, @mutationRate] = [0.6, 0.1]
		@crossoverPolicy = new OnePointCrossover
		@mutationPolicy = new MutationPolicy
		@selectionPolicy = new RankSelection 3
	
	evolve: (initialPopulation, stoppingCondition) ->
		current = initialPopulation
		until stoppingCondition.isSatisfied(current)
			current = @nextGeneration current
			@generationsEvolved++
			@emit 'nextGeneration', @commodity, @generationsEvolved, current.getFittestChromosome(), current
		current
	
	nextGeneration: (currentGeneration) ->
		next = currentGeneration.nextGeneration()
		
		randGen = Math.random()
		while next.getPopulationSize() < next.populationLimit
			pair = @selectionPolicy.select currentGeneration
			if randGen < @crossoverRate
				pair = @crossoverPolicy.crossover pair
			if randGen < @mutationRate
				pair = [@mutationPolicy.mutate(pair[0]), @mutationPolicy.mutate(pair[1])]
			
			next.add pair[0]
			next.add pair[1] if next.getPopulationSize() < next.populationLimit
			
		next

# -----------------------------------------------------

class exports.Run_GA
	constructor: ->
		@done = false
		
	run: ->
		initialPopulation = (allParams) ->
			try
				content = fs.readFileSync("_cache/#{currentCommodity}.population")
				suggested = JSON.parse content
			catch e
				suggested = [
									[43, 0.01, 12, 0.18, 6, 15, -0.01, 5, -0.04, 3]
									[5, 0.16, 22, 0.02, 5, 7, -0.05, 10, -0.02, 4]
									[43, 0.03, 8, 0.04, 4, 18, -0.09, 6, -0.1, 7]
									[12, 0.11, 34, 0.11, 8, 25, -0.07, 22, -0.15, 8]
									[27, 0.12, 4, 0, 0, 48, -0.05, 42, 0, 0]
									[50, 0.08, 48, 0.24, 5, 48, -0.02, 47, -0.15, 8]
									[44, 0.07, 21, 0.3, 7, 52, 0, 9, -0.05, 5]
									[42, 0.04, 25, 0.08, 4, 15, -0.02, 11, -0.05, 4]
									[31, 0.08, 24, 0.04, 1, 34, 0, 21, -0.18, 8]
								]
			suggested = suggested.concat _.chain(allParams).values().map((x)->param2repr x).value()
			initial = new ElitisticListPopulation [], POPULATION_LIMIT = 60, ELITISM_RATE = .1
			_.each suggested, (repr)-> initial.add new Chromosome repr
			initial
		
		
		commodities = 'CF ER SR AU AG AL FG OI PM RI RM RS WH WR Y L A M C TA J V RB PB FU RU CU ZN P ME'.split(' ') #PM
		commodities = 'CF ER SR FG OI PM RI RM RS WH WR Y L A M C TA J V P ME PM RO'.split(' ')
		
		allParams = {}
		same = true
		
		while true
			return if @done
			if same
				currentCommodity = commodities[_.random commodities.length-1]
				[NUM_GENERATIONS, lastFitness] = [3, 0]
				
				try
					allParams = JSON.parse fs.readFileSync('allParams.json')
				catch e
				
				try
					cache = JSON.parse fs.readFileSync("_cache/#{currentCommodity}.cache")
				catch e
					cache = {}
			else
				NUM_GENERATIONS = 10
				
			console.log currentCommodity
			ga = new exports.GA currentCommodity
			ga.on 'nextGeneration', (commodity, generationsEvolved, fittest, current) =>
				fs.writeFileSync 'allParams.json', JSON.stringify(allParams)
				fs.mkdirSync '_cache' if not require('fs').existsSync '_cache'
				fs.writeFileSync "_cache/#{commodity}.cache", JSON.stringify(cache)
				fs.writeFileSync "_cache/#{commodity}.population", JSON.stringify(current.snapshot())
			initialPopulation(allParams)
			best = ga.evolve(initialPopulation(allParams), new FixedGenerationCount(NUM_GENERATIONS)).getFittestChromosome()
			
			same = allParams[currentCommodity] and new Chromosome(param2repr allParams[currentCommodity]).equals(best)
			same or= lastFitness > 0 and best.fitness() is lastFitness
			
			if not same
				console.log "updated! #{best.fitness()} <- #{lastFitness}"
				console.log best.toString()
				best_result = repr2param best.repr()
				best_result.result = if best.result then best.result else allParams[currentCommodity]?.result
				allParams[currentCommodity] = best_result
				fs.writeFileSync 'allParams.json', JSON.stringify(allParams)
			
			lastFitness = best.fitness()

