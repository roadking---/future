package analysis

class Frequency extends org.apache.commons.math.stat.Frequency {
	static int amplify = 100
	void addValue(double v){
		super.addValue new Integer((int)(amplify*v)).intValue()
	}
	void addValue(BigDecimal v){
		addValue v.doubleValue()
	}
	void addValue(int v){
		super.addValue(amplify * v)
	}
	void addValue(Integer v){
		super.addValue amplify * v.doubleValue()
	}
	
	@Override
	public double getCumPct(int v) {
		return super.getCumPct(amplify * v);
	}
	public double getCumPct(double v) {
		super.getCumPct new Integer((int)(amplify*v)).intValue()
	}
	public double getCumPct(BigDecimal v) {
		getCumPct v.doubleValue()
	}
	public double getCumPct(Integer v) {
		getCumPct v.doubleValue()
	}
	
	@Override
	public long getCount(int v) {
		return super.getCount(amplify * v);
	}
	public long getCount(double v) {
		super.getCount new Integer((int)(amplify*v)).intValue()
	}
	public long getCount(BigDecimal v) {
		getCount v.doubleValue()
	}
	public long getCount(Integer v) {
		getCount v.intValue()
	}
	
	@Override
	public double getPct(int v) {
		// TODO Auto-generated method stub
		return super.getPct(amplify * v);
	}
	public double getPct(double v) {
		super.getPct new Integer((int)(amplify*v)).intValue()
	}
	public double getPct(BigDecimal v) {
		getPct v.doubleValue()
	}
	public long getPct(Integer v) {
		getPct v.intValue()
	}
}
