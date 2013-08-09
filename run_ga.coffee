Run_GA = require('./api/analysis/ga').Run_GA
repr2param = require('./api/analysis/ga').repr2param
history = require './api/price/history'
experiment = require './api/analysis/experiment'

#history.prepare -> console.log experiment.run 'M', repr2param([43, 0.01, 12, 0.04, 5, 7, -0.05, 10, -0.02, 4])
new Run_GA().run()