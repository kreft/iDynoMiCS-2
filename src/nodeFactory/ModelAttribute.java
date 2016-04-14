package nodeFactory;

public class ModelAttribute {
	public String tag;
	public String value;
	public String[] options;
	public boolean editable;
	
	public ModelAttribute(String tag, String value, String[] options, boolean editable)
	{
		this.tag = tag;
		this.value = value;
		this.options = options;
		this.editable = editable;
	}
	
	public String getXML()
	{
		return " " + tag + "=\"" + value + "\"";
	}
}
