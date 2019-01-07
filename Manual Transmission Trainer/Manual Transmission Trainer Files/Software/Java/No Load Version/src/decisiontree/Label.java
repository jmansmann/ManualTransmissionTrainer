package decisiontree;

public class Label {
	
	private String _strvalue;
	private double _value;
	
	public Label(String strvalue, double value) {
		_strvalue = strvalue;
		_value = value;
	}
	
	public Label() {	// start as not a leaf node and modify after...
		_strvalue = "Not a leaf node";
		_value = -1;
	}
	
	public String getStrValue() {
		return _strvalue;
	}
	
	public double getValue() {
		return _value;
	}
}
