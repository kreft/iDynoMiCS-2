package test.junit;

import org.junit.Test;

import instantiatable.object.InstantiatableMap;

public class DictionaryTest {

	@Test
	public void dictTest()
	{
		InstantiatableMap<String,String> dict = new InstantiatableMap<String,String>(String.class,String.class);
		
		System.out.println(dict.getXml());
		
		dict.put("good morning", "goede morgen");
		
		System.out.println(dict.getXml());
		
		InstantiatableMap<String,Integer> numbers = new InstantiatableMap<String,Integer>(
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
