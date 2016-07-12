package nodeFactory;

public class ModelAttribute {
	
	/**
	 * Associated Xml tag of attribute
	 */
	public String tag;
	
	/**
	 * String value of attribute
	 */
	public String value;
	
	/**
	 * Allowed options for attribute value, null if any String is allowed
	 */
	public String[] options;
	
	/**
	 * Attribute can be edited during model construction
	 */
	public boolean editable;
	
	/**
	 * General constructor
	 * @param tag
	 * @param value
	 * @param options
	 * @param editable
	 */
	public ModelAttribute(String tag, String value, String[] options, 
			boolean editable)
	{
		this.tag = tag;
		this.setValue(value);
		this.options = options;
		this.editable = editable;
	}
	
	/**
	 * String value of attribute's Xml line
	 * @return
	 */
	public String getXML()
	{
		return " " + tag + "=\"" + getValue() + "\"";
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
