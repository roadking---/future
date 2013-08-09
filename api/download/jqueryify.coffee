_ = require 'underscore'
jsdom = require 'jsdom'
iconv = require('iconv-lite')
http = require 'http'
BufferHelper = require('bufferhelper')
url_node = require 'url'

GBK_HOSTS = [
	'baike.baidu.com'
]

parse = exports.parse = (html, cb)->
	jsdom.env 
		html: html
		scripts: ['//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'] 
		done: (err, window)->
			return cb err if err
			
			$ = window.$
			cb undefined, $, html, -> window?.close()

			#window.close() gc

exports.parse_local = (html, cb)->
	jsdom.env 
		html: html
		scripts: ['./jquery.min.js'] 
		done: (err, window)->
			return cb err if err
			
			$ = window.$
			cb undefined, $, html, -> window?.close()

			#window.close() gc
	

###
exports.fetch = ->
	if arguments.length is 2
		[url, cb] = arguments
	else
		[url, encode, cb] = arguments
	
	opt = url_node.parse(url)
	opt.agent = false
	req = http.get opt, (res)->
		bufferHelper = new BufferHelper()
		res.on 'data', (chunk)-> bufferHelper.concat chunk
		res.on 'end', ->
			encode = 'GBK' if _.find GBK_HOSTS, (x)-> x is opt.host
			if not encode
				console.log m = res.headers['content-type']?.match /charset=(.+)\b/i
				encode = m[1] if m
			html = if encode
				iconv.decode bufferHelper.toBuffer(), encode
			else
				bufferHelper.toString()
			
			parse html, cb
	req.on 'error', cb
###
