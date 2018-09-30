package com.jt.manage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jt.manage.pojo.User;
import com.jt.manage.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	//1.注入UserService
	@Autowired
	private UserService userService;
	
	//2.调用，直接转向jsp文件，查询所有
	@RequestMapping("/queryAll")
	public String queryAll(Model model){
		List<User> userList = userService.queryAll();
		//传递数据给jsp文件
		model.addAttribute("userList", userList);
		
		//prefix+logicName+suffix /WEB-INF/views/+userList+.jsp
		return "userList";	//转向userList.jsp
	}
}
