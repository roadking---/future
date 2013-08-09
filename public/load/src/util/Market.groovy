package util

import price.FuturePrice;

class Market extends Holds {

	static main(args) {
		new Market()
	}

	Market(){
		if(holds.size() > 0){
			def fp = new FuturePrice()
			fp.concern(holds.keySet())
		}
	}
}
