package com.jt.manage.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jt.manage.pojo.Item;
import com.jt.manage.pojo.ItemDesc;
import com.jt.manage.service.ItemService;

@Controller
//根据url确认拦截路径
//"http://manage.jt.com/web/item/findItemById"
@RequestMapping("/web/item")
public class WebItemController {
	
	@Autowired
	private ItemService itemService;
	
	@RequestMapping("/findItemById")
	@ResponseBody
	public Item findItemById(Long itemId){
		
		return itemService.finditemById(itemId);
	}
	
	
	///findItemDescById/"+itemId
	@RequestMapping("/findItemDescById/{itemId}")
	@ResponseBody
	//参数注意：前台定义的map的k值"itemId"，那么后台接收的名字也必须为itemId 
	public ItemDesc findItemDescById(@PathVariable Long itemId){
		
		return itemService.findItemDesc(itemId);
	}

}
