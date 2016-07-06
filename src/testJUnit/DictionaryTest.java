package testJUnit;

import org.junit.Test;

import nodeFactory.primarySetters.Bundle;

public class DictionaryTest {

	@Test
	public void dictTest()
	{
		Bundle<String,String> dict = new Bundle<String,String>(String.class,String.class);
		
		System.out.println(dict.getXml());
		
		dict.put("good morning", "goede morgen");
		
		System.out.println(dict.getXml());
		
		Bundle<String,Integer> numbers = new Bundle<String,Integer>(
				String.class, Integer.class, 
				"text", "numeric", 
				"numberLibrary", "number");
		
		numbers.put("six", 6);
		numbers.put("four", 4);
		
		System.out.println(numbers.getXml());
		
		numbers.muteClassDef = true;
		numbers.muteAttributeDef = true;
		System.out.println(numbers.getXml());
		
	}
}
