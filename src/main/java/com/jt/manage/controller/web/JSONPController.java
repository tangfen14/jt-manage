package com.jt.manage.controller.web;

import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.manage.pojo.Item;

@Controller
public class JSONPController {
	
	/**
	 * 关于JSON写法
	 * @param callback
	 * @return
	 * @throws JsonProcessingException 
	 */
	
	//返回值data数据必须是JSON
	//http://manage.jt.com/web/testJSONP?callback=asdfasdfad
	//@RequestMapping("/web/testJSONP")
	//@ResponseBody
	/*public String  testJSONP(String callback){
		
		return callback + "(" + "{\"id\":\"100\",\"name\":\"tom\"}" +  ")";
	}*/
	
	
	//@RequestMapping("/web/testJSONP")
//	@ResponseBody
	/*public String  testJSONP(String callback) throws JsonProcessingException{
		
		Item item = new Item();
		item.setId(1000L);
		item.setTitle("卖姑娘的小火柴");
		//将对象转为json
		ObjectMapper objectMapper = new ObjectMapper();
		String  json = objectMapper.writeValueAsString(item);
		//回传一个对象
		return callback + "(" + json +  ")";
	}*/
	
	//利用Spring提供的JSONP的解决方案,实现回传一个Item对象
	@RequestMapping("/web/testJSONP")
	@ResponseBody
	//MappingJacksonValue是spring专门为实现跨域JONP时准备的API
	public MappingJacksonValue testJacksonValue(String callback){
		//新建一个对象
		Item item = new Item();
		item.setId(1000L);
		item.setTitle("卖姑娘的小火柴");
		//构造方法中的参数就是需要回传的数据
		MappingJacksonValue jacksonValue = 
				new MappingJacksonValue(item);
			//指定JSONP的回调函数的名称
		jacksonValue.setJsonpFunction(callback);
		//返回经过api处理过的item对象;
		return jacksonValue;
	}
	
	
	
}
