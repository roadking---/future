package load

class Load{
	static main(args){
		def dir = new File('FutureDB')
		if(!dir.exists()) dir.mkdirs()
		
		def lastUpdated = [zz:'20100101', sh:'20080101', dl:'20080101']
		//def lastUpdated = [zz:'2011-04-10', sh:'20110410', dl:'20110410']
		
		def lastUpdatedFile = new File(dir.getAbsolutePath() + '\\.lastUpdated')
		if(lastUpdatedFile.exists()){
			def lines = lastUpdatedFile.readLines()
			lines.each{
				def tmp = it.tokenize()
				lastUpdated.put(tmp[0],tmp[1])
			}
		}
		
		
		def now = new Date() - 1
		
		def download = [
			{
				if(0 < now - Date.parse('yyyyMMdd', lastUpdated.sh)){
					new Shanghai(dir.getAbsolutePath(), lastUpdated.sh, now.format('yyyyMMdd'))
					lastUpdated.sh = now.format('yyyyMMdd')
				}
			},{
//				if(0 < now - Date.parse('yyyy-MM-dd', lastUpdated.zz)){
//					new Zhengzhou(dir.getAbsolutePath(), lastUpdated.zz, now.format('yyyy-MM-dd'))
//					lastUpdated.zz = now.format('yyyy-MM-dd')
//				}
				if(0 < now - Date.parse('yyyyMMdd', lastUpdated.zz)){
					new Zhengzhou2(dir.getAbsolutePath(), lastUpdated.zz, now.format('yyyyMMdd'))
					lastUpdated.zz = now.format('yyyyMMdd')
				}
			},{
				if(0 < now - Date.parse('yyyyMMdd', lastUpdated.dl)){
					new Dalian(dir.getAbsolutePath(), lastUpdated.dl, now.format('yyyyMMdd'))
					lastUpdated.dl = now.format('yyyyMMdd')
				}
			}]
		download = download.collect{new Thread(){void run(){it()}}}
		download*.start()
		download*.join()
		
		lastUpdatedFile.text = ''
		lastUpdated.each{key, value -> lastUpdatedFile << "${key} ${value}\n"}
	}
}