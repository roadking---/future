_ = require 'underscore'
history = require '../price/history'
Cache = require('expiring-lru-cache')

cache = exports.cache = new Cache size: 10000, expiry: 24*60*60*1000
MIN_HOLDS = 50

exports.param =
	UP:
		extent: 5
		threshold: 0.001
		revert_extent: 8
		revert_param: 0.001
		revert_delay:	2
	DOWN:
		extent: 5
		threshold: -0.001
		revert_extent: 8
		revert_param: -0.001
		revert_delay:	2
	first_n_days: 1
	
decide = exports.decide = (contract, date)->
	history.get(contract, date).hold > MIN_HOLDS \
		and guess(contract, date) isnt 'COMMON' \
		and (exports.param.first_n_days ? 1) < exports.seq(contract, date) \
		and not revert(contract, date, guess(contract, date))

exports.seq = (contract, date)->
	record = history.get(contract, date)
	direction = guess contract, date
	dates = history.all_contract_dates contract
	rlt = _.reduceRight(dates[0..record.seq], ((memo, date)->
		if not memo.found and exports.guess(contract, date) is direction
			memo.rlt++
		else
			memo.found = true
		memo
		), {rlt:0, found:false}).rlt
	#console.log [contract, date, rlt].join "\t"
	rlt

exports.revert = revert = (contract, date, direction)->
	direction ?= guess contract, date
	return false if history.get(contract, date).price <= 0
	return true if direction is 'COMMON' or direction isnt guess(contract, date)
	
	rlt = slope(contract, date, exports.param[direction].revert_extent) - exports.param[direction].revert_param
	if direction is 'UP' then rlt < 0 else rlt > 0

exports.findRevertDate = (contract, date)->
	direction = guess contract, date
	return date if direction is 'COMMON'
	
	revertCache = cache.get 'revertCache'
	revertCache ?= []
	rlt = _.filter revertCache, (x)-> x.contract is contract and (x.date <= date <= x.revert)
	return rlt[0].revert if rlt.length
	
	dates = history.all_contract_dates contract
	record = history.get(contract, date)
	found = _.reduce dates[record.seq...dates.length], ((memo, d)->
		if not memo and revert(contract, d, direction)
			if direction is guess contract, d
				record_d = history.get(contract, d)
				delay = exports.param[direction].revert_delay
				found = _.reduce dates[record_d.seq+1..record_d.seq+delay], ((memo, dd)->
					if not memo and direction isnt guess contract, dd
						memo = dd
					memo
					), null
				found ? _.last dates[record_d.seq+1..record_d.seq+delay]
			else
				d
		else
			memo
		), null
	revertDate = found ? _.last dates
	revertCache.push contract:contract, date:date, revert:revertDate
	cache.set 'revertCache', revertCache
	revertDate
	
#------------------------------------------------
exports.guess = guess = (contract, date)->
	key = contract + '|' + date
	return cache.get(key) if cache.get(key)
	
	record = history.get(contract, date)
	direction = if record.seq < 30 or record.price <= 0
		'COMMON'
	else if slope(contract, date, exports.param.UP.extent) >= exports.param.UP.threshold
		'UP'
	else if slope(contract, date, exports.param.DOWN.extent) <= exports.param.DOWN.threshold
		'DOWN'
	else
		'COMMON'
	
	cache.set key, direction
	direction

class Regression
	constructor: ->
		@n = @sumXY = @sumXX = @sumYY = @sumX = @sumY = @xbar = @ybar = 0
	add: (x, y) ->
		if @n is 0 then [@xbar, @ybar] = [x, y]
		else
			[dx, dy] = [x - @xbar, y - @ybar]
			@sumXX += dx * dx * @n / (@n + 1)
			@sumYY += dy * dy * @n / (@n + 1)
			@sumXY += dx * dy * @n / (@n + 1)
			@xbar += dx / (@n + 1)
			@ybar += dy / (@n + 1)
		[@sumX, @sumY] = [@sumX + x, @sumY + y]
		@n++
		
	slope: ->
		if @n < 2 or Math.abs(@sumXX) < 0.00001 then 0 else @sumXY / @sumXX

slope = (contract, date, extent)->
	record = history.get(contract, date)
	dates = history.all_contract_dates contract
	prices = _.chain(dates[record.seq-extent..record.seq]).map((d)->history.get(contract, d).price).filter((x)->_.isNumber(x) and x > 0).value()
	return 0 if not prices.length
	prices = _.map prices, (p)->p/prices[0]
	
	r = new Regression
	r.add idx, p for p, idx in prices
	r.slope()

