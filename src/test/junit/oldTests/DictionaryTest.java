package test.junit.oldTests;

import org.junit.Test;

import instantiable.object.InstantiableMap;

public class DictionaryTest {

	@Test
	public void dictTest()
	{
		InstantiableMap<String,String> dict = new InstantiableMap<String,String>(String.class,String.class);
		
		System.out.println(dict.getXml());
		
		dict.put("good morning", "goede morgen");
		
		System.out.println(dict.getXml());
		
		InstantiableMap<String,Integer> numbers = new InstantiableMap<String,Integer>(
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
