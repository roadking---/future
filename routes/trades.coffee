_ = require 'underscore'
trades = require '../trades'
advise = require('../api/analysis/advise').advise
fs = require 'fs'
hexun = require('../api/price/hexun').hexun

HOLDS_JSON = 'holds.json'

exports.history = (req, res, next)->
	res.render 'trades/history', history:holds.deal


advice_rlt = null
exports.advice = (req, res)->
	if advice_rlt
		return res.render 'trades/advice', trends:_.filter(advice_rlt, (t)->t.decision)
	
	advise trades.holds, (trends)->
		advice_rlt = trends
		res.render 'trades/advice', trends:_.filter(advice_rlt, (t)->t.decision)


exports.deal = (req, res, next)->
	holds = if fs.existsSync HOLDS_JSON
		JSON.parse fs.readFileSync(HOLDS_JSON).toString()
	else
		[]
		
	console.log [action, contract, price] = req.params
	h = _.findWhere holds, contract:contract
	if h
		if action is 'buy' and h.direction is 'UP' or action is 'sell' and h.direction is 'DOWN'
			h.amount++
		else
			h.amount--
	else
		holds.push
			contract:contract
			direction: if action is 'buy' then 'UP' else 'DOWN'
			od: new Date().toString('yyyy-MM-dd')
			op: price
			amount: 1
	holds = _.filter holds, (x)-> x.amount
	fs.writeFileSync HOLDS_JSON, JSON.stringify(holds)
	res.json holds
	
exports.holds = (req, res, next)->
	holds = if fs.existsSync HOLDS_JSON
		JSON.parse fs.readFileSync(HOLDS_JSON).toString()
	return res.send 'no holds' if not holds
	
	hexun (prices)->
		prices = _.chain(prices).map((x)->[x.contract, x]).object().value()
		
		_.each holds, (h)->
			if h.direction is 'UP'
				h.current = prices[h.contract].sell_price
				h.win = h.current - h.op
			else
				h.current = prices[h.contract].buy_price
				h.win =  h.op - h.current
			h.win *= h.amount
		res.render 'holds', holds:holds
		