#fetchUrl = require("fetch").fetchUrl
_ = require 'underscore'
jq = require('../download/jqueryify')
fetch = require("fetch")

hexun = exports.hexun = (cb)->
	fetch.fetchUrl "http://quote.futures.hexun.com/EmbPrice.aspx", (err, meta, body)->
		return cb err if err
		
		jq.parse_local body.toString(), (err, $, html, close)->
			return cb err if err
			contracts = _.map html.match(/showChart\('\w+/g), (x)-> 
				c = x.split("'")[1].toUpperCase()
				if m = c.match /(\D+)(\d+)/
					m[1] + '20' + m[2]
				else
					c
			
			rlt = _.chain( $('#hisBor table tr')).map((x)->
				tds = $(x).find('td')
				if tds.length < 5
					null
				else
					_.chain(tds).map((y)->$(y).text()).compact().value()
			).compact().map((x)->
				x = _.map x, (y, i)-> if i > 0 then Number(y) else y
				_.chain('name price diff buy_price buy_amount sell_price sell_amount deal_amount open last_settled high low hold hold_diff'.split ' ').zip(x).object().value()
			).zip(contracts).map((x)->
				x[0].contract = x[1]
				x[0]
			).value()
			close()
			cb rlt
#hexun (data)-> console.log data