package com.jt.manage.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jt.common.vo.EasyUITree;
import com.jt.manage.pojo.ItemCat;
import com.jt.manage.service.ItemCatService;

@Controller
@RequestMapping("/item/cat")
public class ItemCatController {
	@Autowired
	private ItemCatService itemCatService;
	
	/*三种请求风格
	 * get			/item/cat/all?page=2&rows=10
	 * post			写在表单中<input type="hidden" name="page" value="2">
	 * 主流请求:RESTFul		/itemcat/all/2/10
	 */
	@RequestMapping("/all/{page}/{rows}")//RESTFul风格的请求,参数中必须加@PathVariable注解才能接收
	@ResponseBody	//返回值是一个Json字符串     
	public List<ItemCat> findAll(@PathVariable Integer page,@PathVariable  Integer rows){
		List<ItemCat> itemCatList = itemCatService.findAll(page, rows);
		return itemCatList;
	}

	/**问题:  用这个注解@ResponseBody 为什么会乱码????
	 *  这个注解对回传的数据有要求:
	 *  1.如果回传数据是一个对象使用@ResponseBody 返回时默认以utf-8编码
	 *  2.如果回传字符串,则默认以iso-8859-1编码
	 * 
	 * @param itemId
	 * @param response
	 * @throws IOException
	 */
	//实现商品分类目录的回显,  produces属性可以解决乱码问题
	@RequestMapping(value="/queryItemName",produces="text/html;charset=utf-8")
	@ResponseBody
	public String findItemCatNameById(Long itemId,HttpServletResponse response) throws IOException{
		
		//String name = itemCatService.findNameById(itemId);
		//response.setContentType("text/html;charset=utf-8");
		
		//response.getWriter().write(name);
		return itemCatService.findNameById(itemId);
	}
	
	
	//http://localhost:8091/item/cat/list
	/**
	 *  业务分析:当点击一级目录的时候,应该获取一级目录的id,去后台查询其下的二级子目录id,
	 *	以此类推,当点击二级目录的时候,应该获取二级目录id,传到后台获取三级子目录id
	 * 
	 * 参数选择思路:
	 * 查easyUIAPI手册得:
	 * 树控件读取URL时,子节点的加载依赖于父节点的状态。当展开一个封闭的节点(指close状态)，如果节点没有加载子节点,
	 * 它将会把节点id的值作为http请求参数并命名为'id'，通过URL发送到服务器上面检索子节点。
	 * 因此这个Controller方法的参数值就为这个'id',而获取到这个id,需要加@RequestParam注解指定名字

	 * @RequestParam(value="id",defaultValue="0",required=true)解析:
	 * 	id表示接收参数的名称
	 *  defaultValue 默认值(刚开始点选择类目按钮,弹出树初始化时,此时并没有点击树其中标签的动作,此时就没有id传输,因此需要设置一个初始化默认值,为0;)
	 *  required=true 该参数必须传递,否则SpringMVC校验报错.(此处这句话不要加,因为刚开始初始化的时候,根本就没有这个参数,使用默认值0即可)
	 *  
	 *  返回值的选择思路:参考返回的JSON的结构,它的最外层是一个数组的格式,而内层再封装的时候封装了"id"/"text"/"state"的esayUITree; 
	 *  而由于我们要要展现的是一个多级的目录,目录内部中的每一级元素都是一个esayUITree,
	 *  因此要想有多段的树形结构的展现那么就应该有多个easyUITree,因此用List<EasyUITree>
	 */
	@RequestMapping("/list")
	@ResponseBody
	public List<EasyUITree> findItemCat(@RequestParam(value="id",defaultValue="0")Long parentId){
		
		//1.查询一级商品分类目录(先按一级目录测试)
		//Long parentId = 0L;
		//return itemCatService.findItemCatByParentId(parentId);
		//上面注释的为原先的查询方法,下面的加入redis缓存后的方法
		return itemCatService.findCacheByParentId(parentId);
	}
	
	
}
