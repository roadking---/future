_ = require 'underscore'
fs = require 'fs'
hexun = require('../price/hexun').hexun
trend = require './trend'
history = require '../price/history'
require 'date'

exports.advise = (holds, cb)->
	allParams = JSON.parse fs.readFileSync("#{__dirname}/../../allParams.json")
	today = new Date().toString('yyyyMMdd')
	
	hexun (prices)->
		fn = history.get
		
		cb _.chain(prices).filter((x)->
			/\D+\d+/.test x.contract
		).filter((x)->
			allParams[x.contract.match(/(\D+)\d+/)[1]]
		).map((x)->
			history.get = (contract, date)->
				if date is today
					x
				else
					fn contract, date
			
			history.cache.reset()
			trend.cache.reset()
			trend.param = allParams[x.contract.match(/(\D+)\d+/)[1]]
			
			item = history.get(x.contract, today)
			item.direction = trend.guess x.contract, today
			item.decision = trend.decide x.contract, today
			if hold = _.find(holds, (h)->h.contract is x.contract)
				item.win = (item.price - hold.op) * (if hold.direction is 'UP' then 1 else -1)
				if today isnt trend.findRevertDate(x.contract, today) or trend.revert(x.contract, today, item.direction) \
				or hold.direction isnt item.direction
					item.revert = true
			item
		).compact().value()
		
		###
		cb _.chain(prices.contracts).filter((c)->/\D+\d+/.test c).filter((c)->allParams[c.match(/(\D+)\d+/)[1]]
		).map((c)->
			try
				info = history.constract_info c
				return if not info
			catch e
				return
			
			history.cache.set c + '|' + today, 
				date: today
				seq: info.size
				price: prices[c].current
				hold: prices[c].hold
			
			info.end = today
			info.size++
			info.dates.push today
			history.constract_info_cache.set c, info
			
			trend.param = allParams[c.match(/(\D+)\d+/)[1]]
			item = history.get(c, today)
			item.direction = trend.guess c, today
			item.decision = trend.decide c, today
			item.contract = c
			
			if hold = _.find(holds, (h)->h.contract is c)
				item.win = (item.price - hold.op) * (if hold.direction is 'UP' then 1 else -1)
				if today isnt trend.findRevertDate(c, today) or trend.revert(c, today, item.direction) \
				or hold.direction isnt item.direction
					item.revert = true
			item
		).compact().value()
		###

#exports.current [{contract:'ZN201308', direction:'UP', od:'2012-09-27', op:17005, amount:1}], (rlts)-> #console.log rlts
#exports.current [], (rlts)-> console.log rlts