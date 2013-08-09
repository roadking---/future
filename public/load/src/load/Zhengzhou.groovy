package load

class Zhengzhou {
	
	def historyFile = 'http://www.czce.com.cn/portal/exchange/datahistory.txt'
	def dir, startDate, endDate
	Zhengzhou(){}
	Zhengzhou(dir, startDate, endDate){
		this.dir = dir
		this.startDate = startDate
		this.endDate = endDate
		
		println "ZZ: do the query"
		def historyPage = "http://www.czce.com.cn/cms/cmsface/czce/exchange/datahistory.jsp?startDate=${startDate}&endDate=${endDate}&searchflag=1&commodity="
		new URL(historyPage).getText()
		
		println "ZZ: download the file"
		def file = new File(historyFile.tokenize("/")[-1])
		def out = new BufferedOutputStream(new FileOutputStream(file))
    out << new URL(historyFile).openStream()
    out.close()
    
    //parse the file
    def pattern = ~/^\d+\-\d+\-\d+\|.+/
    file.eachLine{
    	if(pattern.matcher(it).matches()){
    		def fields = it.replaceAll(',', '').replaceAll('\\s+', '').tokenize('|')
    		
    		def data = [	date:fields[0],		contract:fields[1],		lastSettle:fields[2],
    								open:fields[3],		high:fields[4],				low:fields[5],
    								close:fields[6],	settlement:fields[7],	diff:fields[9],
    								amount:fields[10], holds:fields[11],]
				if(data.date != startDate){
	    		def m = data.contract =~ /(\D+)(\d+)/
	    		if(m.matches()){
	    			data.commodity = m[0][1]
	    			data.contract = m[0][1] + makeupMonthStr(m[0][2])
	    		}
	    		write(data)
    		}
    	}
    }
    file.delete()
	}
	
	def write(data){
		def parent = new File("${dir}\\${data.commodity}")
		if(!parent.exists()){parent.mkdirs()}
		
		def file = new File("${dir}\\${data.commodity}\\${data.contract}.txt")
		file << "${data.date}\t${data.lastSettle}\t${data.open}\t${data.high}\t${data.low}\t${data.close}\t${data.settlement}\t${data.diff}\t${data.amount}\t${data.holds}\n"
		file = null
	}
	
	def makeupMonthStr(month){
		if(month.length() == 3){
			def y2 = month[0]
			return y2=='8'|| y2=='9' ? "200${month}" : "201${month}"
		}
		if(month.length() == 4) return '20' + month
		month
	}
	
	static main(args){
		new Zhengzhou(null, '2011-01-01', '2011-01-05')
	}
}