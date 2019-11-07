package io.spring.servlet;

import io.demo.Demo;
import io.spring.annotation.Autowried;
import io.spring.annotation.Controller;
import io.spring.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jason
 * @version 1.0
 * @ClassName DispatchServlet
 * @Date 2019-11-03 01:15
 */
public class DispatchServlet extends HttpServlet {

	/**
	 * 配置文件
	 */
	private Properties contextConfig = new Properties();

	/**
	 * IOC容器
	 */
	private Map<String, Object> beanMap = new ConcurrentHashMap<String, Object>();

	/**
	 * 保存所有Class名称
	 */
	private List<String> classNames = new ArrayList<String>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("--调用doPost--");
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		// 开始初始化的进程
		// 定位
		doLoadConfig(config.getInitParameter("contextConfigLocation"));

		// 加载
		doScanner(contextConfig.getProperty("scanPackage"));

		// 注册
		doRegistry();

		// 依赖注入
		//Spring 中通过调用getBean方法才触发依赖注入的
		doAutoWired();

		Demo demo = (Demo) beanMap.get("demo");
		demo.query("xzj");

		// 如果是mvc 多设计一个东西HandlerMapping();


		// 将RequestMapping中配置的url和一个Method关联上
		// 以便于从浏览器获得用户输入的url以后，能够找到具体执行的方法通过反射去调用
		initHandlerMapping();

	}

	private void initHandlerMapping() {
	}

	private void doAutoWired() {
		if (beanMap.isEmpty()) {
			return;
		}
		for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
			// 反射
			// getFields()只能获取public的字段，包括父类的。
			// 而getDeclaredFields()只能获取自己声明的各种字段，包括public，protected，private。
			// 而我写的Characters类中的属性是在继承父类的，父类中是protected的，所以获取不到，这个弄了我半天！最后只要把父类的protected属性全改成public的就ok
			Field[] fields =  entry.getValue().getClass().getDeclaredFields();

			for (Field field : fields){
				if (!field.isAnnotationPresent(Autowried.class)){
					continue;
				}
				Autowried autowried = field.getAnnotation(Autowried.class);
				String beanName = autowried.value().trim();
				if ("".equals(beanName)){
					beanName = field.getType().getName();
				}
				field.setAccessible(true);
				try {
					field.set(entry.getValue(),beanMap.get(beanName));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * 注册bean，通过反射去实例化类（类在classNames）中
	 */
	private void doRegistry() {
		if (classNames.isEmpty()) {
			return;
		}

		try {
			for (String className : classNames) {
				// 反射出一个class
				Class<?> clazz = Class.forName(className);

				// Spring中用了多个子方法来处理的
				// parseArray，parseMap
				if (clazz.isAnnotationPresent(Controller.class)) {
					// 获取类的名称，首字母小写
					String beanName = lowerFirstCase(clazz.getSimpleName());
					// 在Spring中在这个阶段不会直接put instance的，这里是put的BeanDefinition
					beanMap.put(beanName, clazz.newInstance());
				} else if (clazz.isAnnotationPresent(Service.class)) {
					Service service = clazz.getAnnotation(Service.class);

					// 默认用类首字母小写注入
					//如果自己定义了beanName，优先用自己定义的
					//如果是一个接口，使用接口类型去自动注入
					String beanName = service.value();
					if ("".equals(beanName.trim())) {
						beanName = lowerFirstCase(clazz.getSimpleName());
					}

					Object instance = clazz.newInstance();
					beanMap.put(beanName, instance);

					Class<?>[] interfaces = clazz.getInterfaces();
					for (Class<?> i : interfaces) {
						beanMap.put(i.getName(), instance);
					}
				} else {
					continue;
				}


			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加载类扫描路径下的全部类文件
	 *
	 * @param packgeName
	 */
	private void doScanner(String packgeName) {
		// io.demo 替换成 /io/demo 文件路径
		URL url = this.getClass().getClassLoader().getResource("" + packgeName.replaceAll("\\.", "/"));
		File classDir = new File(url.getFile());
		for (File file : classDir.listFiles()) {
			if (file.isDirectory()) {
				doScanner(packgeName + "." + file);
			} else {
				classNames.add(packgeName + "." + file.getName().replaceAll(".class", ""));
			}
		}
	}

	/**
	 * 定位配置文件，加载配置文件
	 *
	 * @param location
	 */
	private void doLoadConfig(String location) {
		// 在Spring中是通过Reader去查找和定位
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(location.replace("classpath:", ""));
		try {
			// 配置配置文件 在webApp.xml中
			contextConfig.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 方法：
	 * 注入到集合beanNames的key值（beanName）为首字母小写
	 */
	private String lowerFirstCase(String str) {
		char[] chars = str.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}

}
