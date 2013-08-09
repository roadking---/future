_ = require 'underscore'

exports.add = (module)->
	_.chain(module).functions().each (fn)->
		module[fn + '_bak'] = module[fn]
		
		module[fn] = -> 
			module._profile ?= {}
			module._profile[fn] ?= {}
			module._profile[fn].count ?= 0
			module._profile[fn].time ?= 0
			time = new Date().getTime()
			
			rlt = module[fn + '_bak'].apply null, arguments
	
			module._profile[fn].count++
			module._profile[fn].time += new Date().getTime() - time
			rlt

exports.print = (obj)->	
	_.chain(obj).pairs().sortBy((x)->-x[1].time/x[1].count).each (x)->
		console.log [x[0], x[1].count, x[1].time, Math.round x[1].time/x[1].count].join "\t"
