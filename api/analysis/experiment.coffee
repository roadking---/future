_ = require 'underscore'
history = require '../price/history'
trend = require './trend'
gauss = require 'gauss'
track = require '../track'

compared_price = (commodity)-> new gauss.Vector(_.chain(history.last_prices commodity.toUpperCase()).pluck('price').compact().value()).mean()
ratio = 0.014

run = exports.run = (commodity, params)->
	trend.param = params
	trend.cache.reset()
	contracts = history.all_contracts commodity, params.start, params.end
	
	trades = _.chain(contracts).map((contract)->
		dates = history.qualified_contract_dates contract, params.start, params.end
		[
			contract
			_.chain([0...dates.length]).map((i)->
				if trend.decide contract, dates[i]
					direction = trend.guess(contract, dates[i])
					revertDate = trend.findRevertDate contract, dates[i]
					{
						date:	dates[i]
						contract:	contract
						direction:	direction
						seq:	trend.seq(contract, dates[i])
						revertDate:	revertDate
						win:
							if history.get(contract, dates[i]).price > 0 and history.get(contract, revertDate).price > 0
								(history.get(contract, dates[i]).price - history.get(contract, revertDate).price) * (if direction is 'UP' then 1 else -1)
							else
								0
					}
				).compact().value()
		]
	).object().value()
	result = _.chain(trades).values().flatten().value()
	
	compareTo = compared_price commodity
	throw 'NaN compareTo' if _.isNaN compareTo
	
	cumulative_percent = (array, num)->
		if array.length is 0 then 0 else _.filter(array, (x)->x<num).length / array.length
	analysis = 
		trades: trades
		freq_win:	1 - cumulative_percent(_.chain(result).filter((x)->x.win>0).map((x)->x.win).value(), compareTo * ratio)
		up_freq_win:	1 - cumulative_percent(_.chain(result).filter((x)->x.win>0 and x.direction is 'UP').map((x)->x.win).value(), compareTo * ratio)
		down_freq_win:	1 - cumulative_percent(_.chain(result).filter((x)->x.win>0 and x.direction is 'DOWN').map((x)->x.win).value(), compareTo * ratio)
		freq_loss: cumulative_percent(_.chain(result).filter((x)->x.win<0).map((x)->x.win).value(), -compareTo * ratio)
		up_freq_loss: cumulative_percent(_.chain(result).filter((x)->x.win<0 and x.direction is 'UP').map((x)->x.win).value(), -compareTo * ratio)
		down_freq_loss: cumulative_percent(_.chain(result).filter((x)->x.win<0 and x.direction is 'DOWN').map((x)->x.win).value(), -compareTo * ratio)
		count: result.length
		mean:	new gauss.Vector(_.map result, (x)->x.win).mean()/compareTo
		stdev:	new gauss.Vector(_.map result, (x)->x.win).stdev()/compareTo
	analysis.fitness = analysis.mean * analysis.count
	analysis

params = 
	start: '20110701'
	end: '20121031'
	UP:
		extent:	44
		threshold:	0
		revert_extent:	34
		revert_param:	0
		revert_delay:	0
	DOWN:
		extent:	17
		threshold:	0
		revert_extent:	5
		revert_param:	-0.04
		revert_delay:	3

###
console.time 'exp'
track.add trend
console.log run('rb', params)
track.print trend._profile
console.timeEnd 'exp'
###

#history.prepare -> console.log exports.run 'rb', params
#console.log history.get 'C200907', '2008-08-12'
#console.log trend.findRevertDate 'C201209', '2011-11-16'
