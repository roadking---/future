_ = require 'underscore'

serialize = exports.serialize = (funcs, cb)->
	cb ?= ->
	current = 0
	rlts = []
	funcs[0] next = ->
		rlts.push arguments
		if ++current < funcs.length
			funcs[current] next, rlts
		else
			cb.apply cb.callee, rlts
	

###
serialize = exports.serialize = (funcs, cb)->
	lst = [0..funcs.length]
	rlts = []
	
	lst[lst.length-1] = cb
	for i in [funcs.length-1..0]
		do (i)->
			lst[i] = -> 
				funcs[i] (-> 
					rlts.push if arguments.length is 1 then arguments[0] else arguments
					lst[i+1]?.apply lst[i+1]?.callee, rlts
					), rlts
	lst[0]()
###	

group = exports.group = (funcs, cb)->
	cb ?= ->
	cb = _.after funcs.length, cb
	_.each funcs, (f)-> f cb
