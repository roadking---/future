package util

class ModifyParams {
	static main(args){
		
		
		def file = 'PARAMS.map'
		def params = new File(file).newObjectInputStream()?.readObject()
		
		println params.SR
		println params.SR.values()
		return
		params.keySet().each{
			params[it].first_n_days_up = params[it].first_n_days
			params[it].first_n_days_down = params[it].first_n_days
			params[it].remove('first_n_days')
		}
		
		def f = new File(file)
		f.delete()
		f.newObjectOutputStream() << params
	}
}
