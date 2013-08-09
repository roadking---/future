package analysis.trend

class Decision {
	static final def MIN_HOLDS = 50
	
	def date, price, trend, algorithm, result, direction, seq, holds
	def revert, revert_direction
	def in_experiment = true
	
	def getDecision(){
		//only invest on first n days of the trend
		direction = trend.guess(date)
		def max_seq = direction == 'UP' ? Trend.params[trend.history.commodity].first_n_days_up : Trend.params[trend.history.commodity].first_n_days_down
		
		seq = trend.getTrendSeq(date)
		
//		if(trend.history.contract == 'WS201007' && '2010-06-03' == date.format('yyyy-MM-dd')){
//			println trend.getRevertDate(date)
//		}
		
		def d = holds >= MIN_HOLDS && direction != 'COMMON' && seq <= max_seq && !trend.revert(direction, date)
		if(d && in_experiment) {
			//make the decision
			result = algorithm(trend, date)
			revert = trend.getRevertDate(date)
			revert_direction = revert == null ? 'COMMON' : trend.guess(revert)
		}
		
		d
	}

	@Override
	public String toString() {
		def d = Date.isInstance(date) ? date.format('yyyy-MM-dd') : date.toString()
		def r = Date.isInstance(revert) ? revert.format('yyyy-MM-dd') : revert.toString()
		"${d}\t${trend.history.contract}\t${direction}\t${result}\t${seq}\t${r}\t${revert_direction}"
	}
}
