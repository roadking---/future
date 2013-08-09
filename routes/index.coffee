exec = require('child_process').exec
fs = require 'fs'
_ = require 'underscore'
hexun = require('../api/price/hexun').hexun
yaml = require('yamljs')

exports.index = (req, res)->
	res.render 'index'

exports.load = (req, res)->
	res.type 'text'
	exec "call ./load.bat", (err, stdout, stderr)->
		if err
			res.send err.stack
		else
			res.send stdout
			
exports.current = (req, res)->
	hexun (prices)->
		res.render 'current', prices:prices
	

exports.ga = require './ga'
exports.trades = require './trades'


exports.contract = (req, res)->
	m = /(\D+)\d/.exec req.params.contract
	res.render 'contract', data:yaml.load("api/download/stats/#{m[1]}/#{req.params.contract}.yml")
	
