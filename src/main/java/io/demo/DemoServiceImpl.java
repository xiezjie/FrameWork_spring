package io.demo;

import io.spring.annotation.Service;

/**
 * @ClassName DemoServiceImpl
 * @author Jason
 * @Date  2019-11-06 23:08
 * @version 1.0  
 */
@Service
public class DemoServiceImpl implements DemoService {
	@Override
	public String query(String name) {
		System.out.println("My name is "+name);
		return "My name is "+name;
	}
}
