package com.cms.infrastructure.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

//@PropertySource(value = "classpath:application-${env}.properties", encoding = "UTF-8")
public class ConfigProperties {
	private static final Properties properties = new Properties();

	/**
	 * Lấy giá trị cấu hình theo key từ file properties.
	 *
	 * @param key tên cấu hình cần lấy
	 * @param fileName tên file properties
	 * @return giá trị cấu hình hoặc key nếu không tìm thấy
	 */
	public static String getConfigProperties(String key, String fileName) {
		return getConfigProperties(key, key, fileName);
	}

	/**
	 * Lấy giá trị cấu hình theo key từ file properties với giá trị mặc định.
	 *
	 * @param key tên cấu hình cần lấy
	 * @param dfValue giá trị mặc định nếu không tìm thấy
	 * @param fileName tên file properties
	 * @return giá trị cấu hình hoặc giá trị mặc định
	 */
	public static String getConfigProperties(String key, String dfValue, String fileName) {
		String value = dfValue;
		try {
			// properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
			// if(properties.containsKey(key)){
			// value = properties.getProperty(key, dfValue);
			// }

			InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
			properties.load(new InputStreamReader(input, "UTF-8"));
			if (properties.containsKey(key)) {
				value = properties.getProperty(key, dfValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
}
