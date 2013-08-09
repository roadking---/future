express = require('express')
routes = require('./routes')
http = require('http')
path = require('path')

app = express()

app.configure ->
  app.set('port', process.env.PORT || 3001)
  app.set('views', __dirname + '/views')
  app.set('view engine', 'jade')
  app.use(express.favicon())
  app.use(express.logger('dev'))
  app.use(express.bodyParser({ keepExtensions: true, uploadDir: 'uploads' }))
  app.use(express.methodOverride())
  app.use(express.cookieParser('your secret here'))
  app.use(express.session())
  app.use(app.router)
  app.use(require('stylus').middleware(__dirname + '/public'))
  app.use(express.static(path.join(__dirname, 'public')))

app.configure 'development', ->
  app.use express.errorHandler()

app.locals
	_: require 'underscore'

app.get '/', routes.index
app.get '/load', routes.load
app.get '/current', routes.current
app.get '/ga', routes.ga.index
app.get '/ga/population/:commodity', routes.ga.population
app.get '/ga/best/:commodity', routes.ga.best
app.get '/advice', routes.trades.advice
app.get '/ga/clean', routes.ga.clean
app.get '/history', routes.trades.history
app.get '/contract/:contract', routes.contract
app.get /(buy|sell)\/(.+?)\/(\d+)/, routes.trades.deal
app.get '/holds', routes.trades.holds

http.createServer(app).listen app.get('port'), ->
  console.log("Express server listening on port " + app.get('port'))
