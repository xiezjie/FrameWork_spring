package io.demo;

import io.spring.annotation.Autowried;
import io.spring.annotation.Controller;

/**
 * @ClassName Demo
 * @author Jason
 * @Date  2019-11-04 23:58
 * @version 1.0  
 */
@Controller
public class Demo {
	@Autowried
	public DemoService demoService;

	public void query(String name){
		demoService.query(name);
	}
}
