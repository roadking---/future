fs = require 'fs'
exec = require('child_process').exec
_ = require 'underscore'
Cache = require('expiring-lru-cache')
trades = require '../trades'

cache = new Cache size:5000, expiry: 24*60*60*1000
experiment = require '../api/analysis/experiment'
ga = require '../api/analysis/ga'

exports.index = (req, res)->
	allParams = JSON.parse fs.readFileSync('./allParams.json')
	round = (x)-> Math.round(x*1000)/1000
	commodities = _.chain(allParams).keys().map((c)->[c, round allParams[c]?.result?.mean]).sortBy((x)->if _.isNaN x[1] then 0 else -x[1]).object().value()
	
	res.render 'ga/index', allParams:allParams, commodities:commodities

exports.population = (req, res)->
	population = JSON.parse fs.readFileSync("./_cache/#{req.params.commodity}.population")
	cacheFiles = JSON.parse fs.readFileSync("./_cache/#{req.params.commodity}.cache")
	
	res.render 'ga/population', population:_.map(population, (p)-> [p, cacheFiles[p.join(',')]])

exports.best = (req, res)->
	params = JSON.parse fs.readFileSync('./allParams.json')
	if not params[req.params.commodity]
		return res.send 'error'
	
	rlt = cache.get req.params.commodity + '_best'
	if rlt
		return res.render 'ga/best', params:params, rlt:rlt
	
	params = params[req.params.commodity]
	params.start = ga.start
	params.end = ga.end
	rlt = experiment.run req.params.commodity, params
	res.render 'ga/best', params:params, rlt:rlt
	cache.set req.params.commodity + '_best', rlt

exports.clean = (req, res)->
  exec "call ./clean.bat", (err, stdout, stderr)->
  	if err
  		res.send err.stack
  	else
			res.redirect '/ga'
