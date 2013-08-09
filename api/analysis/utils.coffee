exports.unique = (array) ->
	tmp = []
	for item in array
		tmp.push item if item not in tmp
	tmp

#------------------------------------------------
###
assert = require('assert')
array = [3, 2, 4, 3, 2, 4]
assert.equal 3, exports.unique(array).length
###
#------------------------------------------------

exports.find = (array, num) ->
	return i for n, i in array when n is num
	-1
#------------------------------------------------
###
assert = require('assert')
array = [3, 2, 4]
assert.equal 0, exports.find(array, 3)
assert.equal 1, exports.find(array, 2)
assert.equal 2, exports.find(array, 4)
###
#------------------------------------------------

exports.rank = (array) ->
	nums = exports.unique array
	nums.sort()
	(1 + exports.find nums, item for item in array)

#------------------------------------------------
###
assert = require('assert')
array = [3, 2, 4, 3, 2, 4]
r = exports.rank array
assert.equal 2, r[0]
assert.equal 1, r[1]
assert.equal 3, r[2]
###
#------------------------------------------------

exports.union = (array1, array2) ->
	a = (item for item in array1)
	a.push item for item in array2
	a
#------------------------------------------------
###
assert = require('assert')
a1 = [2, 1]
a2 = [3, 4]
r = exports.union a1, a2
assert.equal 4, r.length
###
#------------------------------------------------

exports.randomInt = (num) ->
	Math.floor Math.random() * (num - .1)

exports.max = (array) ->
	max = array[0]
	for n in array
		max = n if n > max
	return max

#------------------------------------------------
###
assert = require('assert')
assert.equal 4, exports.max [2, 3, 4]
###
#------------------------------------------------

class exports.Map
	constructor: ->
		@keys = []
		@hash = {}
	
	keySet: -> @keys
	
	put: (key, value) ->
		@hash[key] = value
		@keys.push key if exports.find(@keys, key) < 0
	
	get: (key) -> @hash[key]
	has: (key) -> @hash[key]?
	size: -> @keys.length
###
assert = require('assert')
map = new exports.Map
assert.equal 0, map.size()
assert.equal false, map.has('key')
map.put 'key', 'value'
assert.equal true, map.has('key')
assert.equal 'value', map.get('key')
assert.equal 1, map.size()
#console.log map.keySet()
###
