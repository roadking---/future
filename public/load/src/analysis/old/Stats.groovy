package analysis.old

class Stats {
	def data
	
	def getAvg(){
		if(data.count{it!=null} == 0) return null
		data.sum{it==null?0:it} / data.count{it!=null}
	}
	def getStddev(){
		if(data.count{it!=null} == 0) return null
		
		def avg = new Stats(data:data).avg
		def num = data.grep{it!=null}.collect{it-avg}.sum { it*it }
		num /= data.count{it!=null}
		Math.sqrt(num)
	}
}
