package testJUnit;

import org.junit.Test;

import nodeFactory.primarySetters.BundleMap;

public class DictionaryTest {

	@Test
	public void dictTest()
	{
		BundleMap<String,String> dict = new BundleMap<String,String>(String.class,String.class);
		
		System.out.println(dict.getXml());
		
		dict.put("good morning", "goede morgen");
		
		System.out.println(dict.getXml());
		
		BundleMap<String,Integer> numbers = new BundleMap<String,Integer>(
				String.class, Integer.class, 
				"text", "numeric", 
				"numberLibrary", "number");
		
		numbers.put("six", 6);
		numbers.put("four", 4);
		
		System.out.println(numbers.getXml());
		
		numbers.muteSpecification = true;
		System.out.println(numbers.getXml());
		
	}
}
