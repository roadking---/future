http = require 'http'
url = require 'url'
events = require 'events'

class WebPageLoader extends events.EventEmitter
	load: (myUrl)->
		options = url.parse myUrl
		
		http.get(options, (res)=>
			buffers = []
			size = 0
			
			res.on 'data', (chunk) ->
				buffers.push chunk
				size += chunk.length
			
			res.on 'end', () =>
				buffer = new Buffer(size)
				pos = 0
				for b in buffers
					b.copy buffer, pos
					pos += b.length
				
				@emit 'loaded', buffer
		).on 'error', (e) ->
			console.log "Got error: #{e.message}"


class exports.HexunPrice extends events.EventEmitter
	constructor: ->
		@loader = new WebPageLoader()
		@loader.on 'loaded', (buffer) =>
			prices = @parse buffer
			@emit 'loaded', prices
	
	load: ->
		@loader.load "http://quote.futures.hexun.com/EmbPrice.aspx"
	
	parse: (buffer) ->
		lines = buffer.toString().split "\n"
		str = "<tr align='center' onmouseover='colorLine(this)' onmouseout='discolorLine(this)' style='cursor: hand'>"
		prices = {contracts:[]}
		for l in lines when l[0 ... str.length] is str
			l = l[str.length ... l.length]
			array = @parseLine l
			pack =
				contract:	array[0],
				current:	Number(array[2]),
				diff:			Number(array[3]),
				buy_price:	Number(array[4]),
				buy_amount:	Number(array[5]),
				sell_price:	Number(array[6]),
				sell_amount:	Number(array[7]),
				deal_amount:	Number(array[8]),
				open:			Number(array[9]),
				last_settlement:	Number(array[10]),
				high:			Number(array[11]),
				low:			Number(array[12]),
				hold:			Number(array[13]),
				hold_diff:			Number(array[14]),
			
			prices.contracts.push pack.contract
			prices[pack.contract] = pack
		prices

	parseLine: (l) ->
		piece = l[l.indexOf('<') .. l.indexOf('>')]
		l = l[piece.length ... l.length]
		
		contract = /'(\w+)'/.exec(piece)[1]
		contract = contract.toUpperCase()
		if /(\D+)(\d+)/.test(contract)
			rlt = /(\D+)(\d+)/.exec(contract)
			contract = "#{rlt[1]}20#{rlt[2]}"
		
		array = [contract]
		piece = null
		while not(piece?) or l.length > piece.length
			l = l[piece.length ... l.length] if piece?
			if l.indexOf('<') > 0
				field = l[0 ... l.indexOf('<')]
				array.push field
				l = l[field.length ... l.length]
			piece = l[l.indexOf('<') .. l.indexOf('>')]
		array

#----------------------------------------------------------------------------
###
hp = new exports.HexunPrice
hp.on 'loaded', (prices) =>
	console.log prices.TA201205
hp.load()
###
#----------------------------------------------------------------------------