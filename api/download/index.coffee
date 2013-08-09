_ = require 'underscore'
_.str = require 'underscore.string'
_.mixin _.str.exports()
_.str.include('Underscore.string', 'string')

fs = require 'fs'
jq = require('./jqueryify')
fetch = require("fetch")
yaml = require('yamljs')
require 'date'
flow = require('./flow')

commodities =
	Y: '豆油'
	V: '聚氯乙烯'
	P: '棕榈油'
	M: '豆粕'
	L: '聚乙烯'
	J: '焦炭'
	C: '玉米'
	B: '豆二'
	A: '豆一'
	AL: '铝'
	AU: '黄金'
	CU: '铜'
	FU: '燃料油'
	PB: '铅'
	RB: '螺纹钢'
	RU: '天然橡胶'
	WR: '线材'
	ZN: '锌'
	AG: '白银'
	JM: '焦煤'
	
sh = (date, cb)->
	console.log 'sh ' + date.toString('yyyyMMdd')
	fetch.fetchUrl url = "http://www.shfe.com.cn/dailydata/kx/kx#{date.toString('yyyyMMdd')}.html", (err, meta, body)->
		return cb err if err
		jq.parse_local body.toString(), (err, $, html, close)->
			return cb err if err
			
			name2code = _.invert commodities
			
			lines = _.chain($('#tableInstrument tr.bgcolorB')).map((x)->
				_.map $(x).find('td'), (y)-> _.trim $(y).text()
			).filter((x)->
				x[0] isnt '总计' and x[1] isnt '小计'
			).map((x)->
				x = _.map x, (y, i)-> if y isnt '' and i > 0 then _(y).toNumber(2) else y
				_.chain('commodity month last_settled open high low close settled diff1 diff2 amount hold hold_diff'.split ' ').zip(x).object().value()
			).value()
			
			if lines.length <= 0
				lines = _.chain($('body table:nth-child(2) tr')).reject((x)->
					$(x).find('td').length < 5
				).map((x)->
					_.map $(x).children('td'), (y)-> _.trim $(y).text()
				).filter((x)->
					x[0] isnt '总计' and x[1] isnt '小计'
				).map((x)->
					x = _.map x, (y, i)-> if y isnt '' and i > 0 then _(y).toNumber(2) else y
					_.chain('commodity month last_settled open high low close settled diff1 diff2 amount hold hold_diff'.split ' ').zip(x).object().value()
				).value()
			
			tmp = null
			_.each lines, (x)->
				if x.commodity is ''
					x.commodity = tmp 
				else
					tmp = x.commodity
			
			lines = _.map lines, (x)->
				x.month = '20' + x.month
				if name2code[x.commodity]
					x.commodity = name2code[x.commodity]
				else
					console.error x.commodity
				x
			close()
			cb err, lines, url

zz = (date, cb)->
	console.log 'zz ' + date.toString('yyyyMMdd')
	fetch.fetchUrl url = "http://www.czce.com.cn/portal/exchange/#{date.toString('yyyy')}/datadaily/#{date.toString('yyyyMMdd')}.htm", (err, meta, body)->
		return cb err if err
		jq.parse_local body.toString(), (err, $, html, close)->
			return cb err if err
	
			lines = _.chain($('#senfe tr')).reject((x)->
				$(x).hasClass('tr0')
			).map((x)->
				_.map $(x).find('td'), (y)-> _.trim $(y).text()
			).reject((x)->
				x[0] in ['总计', '小计']
			).map((x)->
				fields = _.map x, (y, i)-> if i > 0 and y isnt '' then _(y.replace(',','')).toNumber(2) else y
				_.chain('contract last_settled open high low close settled diff1 diff2 amount hold hold_diff amount_money delivery'.split ' ').zip(fields).object().value()
			).map((x)->
				x.commodity = x.contract[0..1]
				x.month = x.contract[2..]
				x.month = if x.month[0] > '8' then '200' + x.month else '201' + x.month
				delete x.contract
				x
			).filter((x)->
				/^\w+$/.test x.commodity
			).value()
			close()
			cb err, lines, url

dl = (date, cb)->
	console.log 'dl ' + date.toString('yyyyMMdd')
	fetch.fetchUrl url = "http://www.dce.com.cn/PublicWeb/MainServlet?action=Pu00011_result&Pu00011_Input.trade_date=#{date.toString('yyyyMMdd')}&Pu00011_Input.trade_type=0&Pu00011_Input.variety=all", (err, meta, body)->
		return cb err if err
		jq.parse_local body.toString(), (err, $, html, close)->
			return cb err if err
		
			name2code = _.invert commodities
			
			lines = _.chain($('.table tr')).reject((x)->
				$(x).hasClass 'tr0'
			).map((x)->
				_.map $(x).find('td'), (y)-> _.trim $(y).text()
			).reject((x)->
				x[0] is '总计' or _(x[0]).endsWith '小计'
			).map((x)->
				fields = _.map x, (y, i)-> if i > 1 and y isnt '' then _(y.replace(',','')).toNumber(2) else y
				_.chain('commodity month open high low close settled last_settled diff1 diff2 amount hold hold_diff amount_money'.split ' ').zip(fields).object().value()
			).map((x)->
				x.month = '20' + x.month
				if name2code[x.commodity]
					x.commodity = name2code[x.commodity]
				else
					console.error x.commodity
				x
			).value()
			close()
			cb err, lines, url

write = (date, data)->
	_.each data, (x)->
		{commodity:commodity, month:month} = x
		delete x.month
		delete x.commodity
		
		if not fs.existsSync "#{__dirname}/stats/#{commodity}"
			fs.mkdirSync "#{__dirname}/stats/#{commodity}"
		
		json = if fs.existsSync "#{__dirname}/stats/#{commodity}/#{commodity}#{month}.yml"
			yaml.parse fs.readFileSync("#{__dirname}/stats/#{commodity}/#{commodity}#{month}.yml").toString()
		else
			commodity:commodity
			month:month
			records: {}
		
		if not json
			throw new Error commodity + month + "\n" + JSON.stringify(x)
			
		json.records[date.toString('yyyyMMdd')] = x
		fs.writeFileSync "#{__dirname}/stats/#{commodity}/#{commodity}#{month}.yml", yaml.stringify(json, 4)

download = (date, market, cb)->
	market date, (err, data, url)->
		try
			write date, data
		catch e
			console.error url
			throw e
		cb?()


#return download Date.parseExact('20100624', 'yyyyMMdd'), sh, ->

download_between = (start, end, cb)->
	lst = []
	date = start
	while date.compareTo(end) <= 0
		lst.push date
		date = date.clone().add(day:1)
	return if lst.length <= 0
	
	done = _.after 2, cb 
	flow.serialize _.map(lst, (date)-> (cb)-> download date, sh, cb), done
	flow.serialize _.map(lst, (date)-> (cb)-> download date, dl, cb), done
	flow.serialize _.map(lst, (date)-> (cb)-> download date, zz, cb), done
	
file = __dirname + '/stats/.date'
if fs.existsSync file
	start = fs.readFileSync(file).toString()
	start = Date.parseExact(start, 'yyyyMMdd')
else
	start = Date.parseExact('20130126', 'yyyyMMdd')

download_between start, end=Date.today(), ->
	fs.writeFileSync file, end.toString('yyyyMMdd')
	console.log 'done'