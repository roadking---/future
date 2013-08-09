EventProxy = require('eventproxy').EventProxy

exports.group = (funcs, cb)->
	cb?() if not funcs or not funcs.length
	
	proxy = new EventProxy
	
	args = (''+i for fn, i in funcs)
	args.push -> cb?.apply cb?.callee, arguments
		
	proxy.assign.apply proxy, args
	
	for fn, i in funcs
		do (fn, i)->
			i_cb = ->
				arg = if arguments.length > 1 then arguments else arguments[0]
				proxy.trigger ''+i, arg
			fn i_cb