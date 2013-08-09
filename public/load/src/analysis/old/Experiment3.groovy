package analysis.old

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import price.HistoryLoader;

class Experiment3 {

	def start, end
	def DAYS = 5, THREAD_POOL = 4
	
	def run(String contract){
		def everyday = []
		
		def date = Date.parse('yyyy-MM-dd', start)
		def endDate = Date.parse('yyyy-MM-dd', end)
		def h = new HistoryLoader(contract)
		while(date <= endDate){
			def price = h.getPrice(date)
			if(price != null && price > 0){
				def trend = new TraditionalTrend().guess(h, date)
				everyday << [date:date, price:price, 
					direction:(trend.startsWith('UP') ? 'UP' : (trend.startsWith('DOWN') ? 'DOWN' : 'COMMON'))]
			}
			date = date.next()
		}
		h.close()
		
		def trendset = [], currentDirection = ''
		everyday.each{
			if(it.direction != currentDirection){
				def name = it.direction
				if(name == 'COMMON' && currentDirection != '')
					name = "${currentDirection}2${name}"
				trendset << [name:name, list:[it]]
			} else trendset[-1].list << it
			currentDirection = it.direction
		}
		
		def rlt = [:]
		trendset.each{
			if(null == rlt[it.name]) rlt[it.name] = [list:[], frequency:[:], days:[:], win:[:]]
			rlt[it.name].list << it.list
		}
		rlt.remove('COMMON')
		
		rlt.each{key, item ->
			def lst = item.list.collect{ eachlist ->
				def prices = eachlist.collect{it.price}
				prices.collect { new Double(100*it/prices[0]).round(1)}
			}
			item.list = lst.collect {
				def win = 0
				if(key == 'DOWN' || key == 'UP2COMMON') win = it[0] - it[-1]
				else win = it[-1] - it[0]
				win = win.round(1)
				[prices:it, win:win] 
			}
		}
		
		rlt = calcFrequency(rlt)
		rlt = calcDays(rlt)
		rlt = calcWin(rlt)
	}
	
	def calcWin(rlt){
		rlt.each{key, item ->
			def winlst = item.list.collect{it -> it.win}
			def stats = new Stats(data:winlst)
			item.win.avg = stats.avg?.round(1)
			item.win.stddev = stats.stddev?.round(1)
		}
		rlt
	}
	
	def calcFrequency(rlt){
		rlt.each{key, item ->
			item.list.each{
				if(item.frequency[it.prices.size()] == null) item.frequency[it.prices.size()] = 1
				else item.frequency[it.prices.size()] = 1 + item.frequency[it.prices.size()]
			}
		}
		rlt
	}
	def calcDays(rlt){
		rlt.each{key, item ->
			for(day in 0 .. DAYS-1){
				def prices = item.list.collect{it.prices[day]}
				def stats = new Stats(data:prices)
				item.days[day] = [
					count:prices.count{it!=null},
					avg:stats.avg?.round(1),
					stddev:stats.stddev?.round(3)
					]
			}
		}
		rlt
	}
	
	class RunTask implements Callable{
		def contract, start, end
		public RunTask(contract, start, end) {
			super();
			this.contract = contract;
			this.start = start;
			this.end = end;
		}
		def call(){
			def e = new Experiment3(start:start, end:end)
			def rlt = e.run(contract)
			rlt
		}
	}
	def run(List<String> contracts){
		def pool = Executors.newFixedThreadPool(THREAD_POOL)
		def lst = contracts.collect{
			def rlt = pool.submit(new RunTask(it, start, end))
			rlt.get()
			}
		pool.shutdown()
		
		def rlt = [:]
		lst.each{
			it.each{key, value ->
				if(rlt[key] == null) rlt[key] = [list:[], frequency:[:], days:[:], win:[:]]
				rlt[key].list.addAll(value.list)
			}
		}
		rlt = calcFrequency(rlt)
		rlt = calcDays(rlt)
		rlt = calcWin(rlt)
	}
	
	def show(rlt){
		rlt.each {key, value -> 
			println key
			println 'days ' + value.days
			println 'freq ' + value.frequency
			println 'win  ' + value.win
			println ''
			}
	}
	def csv(String file, rlt){
		def f = new File(file)
		return csv(f, rlt)
	}
	def csv(File f, rlt){
		if(f.exists()) f.delete()
		rlt.each {key, value ->
			f.append key + "\n"
			f.append "Days\n"
			f.append "days,count,avg,stddev\n"
			value.days.each{k,v->
				f.append "${k},${v.count},${v.avg},${v.stddev}\n"
			}
			f.append "Frequency\n"
			f.append "days,freq\n"
			value.frequency.keySet().sort().each{
				f.append "${it},${value.frequency[it]}\n"
			}
			f.append "Win\n"
			f.append "avg,${value.win.avg}\n"
			f.append "stddev,${value.win.stddev}\n"
			value.list.each{f.append "${it.win}\n"}
			
			f.append "\n"
		}
	}
	static main(args) {
		def e = new Experiment3(start:'2008-1-5', end:'2011-7-31') 
		def rlt = e.run('SR200905')
		//def rlt = e.run(['SR200905', 'SR200907'])
		
//		def rlt = e.run(HistoryLoader.loadContractList('SR'))
//		e.show(rlt)
//		e.csv('SR.csv', rlt)
		
//		def commodities = 'ER CF RO SR TA WT A AL C Y M J P V L AU FU RU CU ZN PB RB'.split().toList()
//		commodities.each{
//			def f = new File("${it}.csv")
//			if(!f.exists()){
//				println it
//				def rlt = e.run(HistoryLoader.loadContractList(it))
//				e.show(rlt)
//				e.csv(f, rlt)
//			}
//		}
	}

}
