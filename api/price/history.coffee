_ = require 'underscore'
fs = require('fs')
Cache = require('expiring-lru-cache')
yaml = require('yamljs')
gauss = require 'gauss'
require 'date'

DIR = __dirname + '/../download/stats'
cache = exports.cache = new Cache size:5000, expiry: 24*60*60*1000

exports.all_contracts = (commodity, start_date, end_date)->
	commodity = commodity.toUpperCase()
	contracts = cache.get commodity
	return contracts if contracts
	
	contracts = \
	if fs.existsSync DIR + '/' + commodity
		_.chain(fs.readdirSync DIR + '/' + commodity).reject((x)->
			records = _.keys require("#{DIR}/#{commodity}/#{x}").records
			not records.length or _.min(records) >= end_date or _.max(records) <= start_date
		).map((x)->
			x.split('.')[0]
		).value()
	else
		[]
	cache.set commodity, contracts
	contracts

exports.qualified_contract_dates = (contract, start_date, end_date)->
	dates = cache.get contract + '_qualified_contract_dates'
	return dates if dates
	
	m = /(\D+)\d+/.exec contract
	dates = \
	_.chain(require("#{DIR}/#{m[1]}/#{contract}.yml").records).keys().filter((x)->
		start_date <= x <= end_date
	).value()
	cache.set contract + '_qualified_contract_dates', dates
	dates

exports.all_contract_dates = (contract)->
	dates = cache.get contract + '_all_contract_dates'
	return dates if dates
	
	m = /(\D+)\d+/.exec contract
	dates = _.keys require("#{DIR}/#{m[1]}/#{contract}.yml").records
	cache.set contract + '_all_contract_dates', dates
	dates


exports.get = (contract, date)->
	m = /(\D+)\d+/.exec contract
	record = require("#{DIR}/#{m[1]}/#{contract}.yml").records[date]
	if not record
		throw new Error "no record found for #{contract} #{date}"
	record.price = record.settled
	
	if not record.seq?
		#initial load. set seq for each record
		all_records = require("#{DIR}/#{m[1]}/#{contract}.yml").records
		_.chain(all_records).keys().each (x, i)->
			all_records[x].seq = i
	record



exports.last_prices = (commodity)->
	prices = cache.get commodity + '_last_prices'
	return prices if prices
	
	prices = _.chain(fs.readdirSync DIR + '/' + commodity).map((x)->
		records = require("#{DIR}/#{commodity}/#{x}").records
		max_date = _.chain(records).keys().max().value()
		contract:x, date:max_date, price:records[max_date].settled
	).value()
	cache.set commodity + '_last_prices', prices
	prices


###
exports.cache = cache = new Cache size:500
exports.constract_info_cache = constract_info_cache = new Cache size: 50, expiry: 24*60*60*1000
exports.FutureDB = "#{__dirname}/../../public/FutureDB"

split = (contract)->
	m = contract.match /(\D+)(\d+)/
	[m[1], m[2]]

exports.constract_info = (contract)->
	info = constract_info_cache.get contract
	return info if info
	set_constract_info contract, exports.load contract
	constract_info_cache.get contract
	
set_constract_info = (contract, details)->
	return if not details.length or constract_info_cache.get contract
	info =
		start:	details[0][0]
		end:	details[details.length-1][0]
		size:	details.length
		dates: _.map(details, (x)->x[0])
	constract_info_cache.set contract, info
	
exports.get = (contract, date)->
	key = (constract, date)-> constract + '|' + date
	detail = cache.get key(contract, date)
	return detail if detail
	
	details = exports.load contract
	set_constract_info contract, details
	_.each details, (x, i)->
		cache.set key(contract, x[0]), 
			date:	x[0]
			seq:	i
			price:	new gauss.Vector(_.filter(x[2..5], (y)->_.isNumber(y))).mean()
			hold:	x[9]
	cache.get key(contract, date)
	
exports.load = (contract)->
	[commodity, month] = split contract
	
	file = "#{exports.FutureDB}/#{commodity}/#{contract}.txt"
	content = fs.readFileSync(file, 'utf-8')
	content = content.split '\n'
	content = (line.split '\t' for line in content)
	_.chain(content)	\
		.reject((fields)->fields.length <= 2)	\
		.map((fields)->
			_.map fields, (y)-> if isNaN Number(y) then y else Number(y)
		).filter((fields)->
			_.any fields[2..5], (x)-> _.isNumber(x) and x > 0
		).value()

history_files = null

exports.prepare = (cb)-> 
	new File(exports.FutureDB).list (err, files)->
		history_files = files
		cb?()

exports.all = ->
	switch arguments.length
		when 0
			history_files
		when 1
			[commodity] = arguments
			_.chain(history_files?[commodity]).keys().map((x)->x.split('.')[0]).value()
###
