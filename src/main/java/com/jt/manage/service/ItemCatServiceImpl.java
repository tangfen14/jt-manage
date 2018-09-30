package com.jt.manage.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jt.common.mapper.SysMapperProvider;
import com.jt.common.service.BaseService;
import com.jt.common.service.RedisService;
import com.jt.common.vo.EasyUITree;
import com.jt.common.vo.ItemCatData;
import com.jt.common.vo.ItemCatResult;
import com.jt.manage.mapper.ItemCatMapper;
import com.jt.manage.pojo.ItemCat;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

@Service
public class ItemCatServiceImpl extends BaseService<ItemCat> implements ItemCatService{
	//关联ItemCatMapper
	@Autowired
	private ItemCatMapper itemCatMapper;
	
	//注入jedis对象
	//private Jedis jedis;
	//使用工具包中自写的装饰类
	@Autowired
	//private RedisService redisService;
	private JedisCluster jedisCluster;//使用工具类中自定义的工厂获取的对象,可操作redies集群
	
	//工具类对象,json为我们提供的专门用来转对象的API
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public List<ItemCat> findAll(Integer page, Integer rows){
		//return itemCatMapper.findAll();
		//分页支持，startPage方法是静态
		//内部就调用拦截器，startPage相当于事务开启begin，开启分页操作;
		//那么这个操作什么时候生效呢?答:此行后的第一条(且仅此一条)执行的查询的SQL语句生效(内部拦截器只处理一次,处理完第二条sql语句再进来就不管了 )
		PageHelper.startPage(page, rows); 	
		//第一条查询SQL被拦截，SQL语句拼接 limit page, rows
		List<ItemCat> itemCatList = itemCatMapper.findAll();
		//比如下行测试第二条sql,通过debug可以看到上条获得是分页的个数,而第二条就是全部的数据了
		List<ItemCat> itemCatList1 = itemCatMapper.findAll();
		
		
		//上行的返回值itemCatList不能直接返回，必须放在PageInfo对象中
		//这里和线程安全有关！直接返回方式它会产生线程安全问题(比如高并发中,我跟你同时查询,如果此时没有线程安全保障,会产生混乱,我的查询还没完返回,你的查询就进来了,结果有可能我拿到了你查询的结果)
		//怎么解决？在pageHepler中利用ThreadLocal，把当前对象和当前线程绑定，每个用户独立线程，比加锁好多了(加锁的话一个人没执行完另一个就得等着)
		PageInfo<ItemCat> pageInfo = new PageInfo<ItemCat>(itemCatList);
		
		return pageInfo.getList();
		
	}

	@Override
	public String findNameById(Long itemId) {
		//利用通用mapper获取对象的结果
		return itemCatMapper.selectByPrimaryKey(itemId).getName();
	}

	/**
	 * 目标:实现商品新增弹窗的树结构
	 * 实现步骤:
	 * 1.根据条件查询需要的结果 where parent_id = ?
	 * 2.需要将ItemCat集合(商品表pojo)转化为List<EasyUITree>
	 * 3.通过循环遍历的方式实现List赋值.
	 */
	@Override
	public List<EasyUITree> findItemCatByParentId(Long parentId) {
		ItemCat itemCat = new ItemCat();
		itemCat.setParentId(parentId);
		//1.通过通用mapper,根据父类来查询需要的结果
		// 		通用Mapper的规则, 查询操作时  如果传入的数据不为null,则会充当where条件;
		//此处将仅有parentId不为空的itemCat传入做参数,相当于将parentId当做where条件进行查询,那么所得的结果就是我们想要的List<ItemCat>
		/*	通用mapper接口中的方法如下:
		 *     @SelectProvider(type = SysMapperProvider.class, method = "dynamicSQL")
    			List<T> select(T record);
		 */
		List<ItemCat> itemCatList = 
						itemCatMapper.select(itemCat);
		//2.创建返回集合对象
		List<EasyUITree> treeList = new ArrayList<EasyUITree>();
		
		//3.将集合进行转化
			//遍历List<ItemCat>集合,将其中的ItemCat对象的数据一一赋值给EasyUITree对象,再放入List<EasyUITree>中返回最终结果
		for (ItemCat itemCatTemp : itemCatList) {
			EasyUITree easyUITree = new EasyUITree();
			easyUITree.setId(itemCatTemp.getId());
			easyUITree.setText(itemCatTemp.getName());
			//easyUITree中的state状态信息有两个:  "open"/"closed"
			//此状态信息要参考ItemCat对象中的Boolean isParent属性,
			//如果是父级则暂时先关闭,用户需要时在展开(参考京东,当鼠标移上去时,子菜单才会展开,否则页面太满)
			String state = 
	itemCatTemp.getIsParent() ? "closed" : "open";
			easyUITree.setState(state);
			//放入List<EasyUITree>中
			treeList.add(easyUITree);
		}
		return treeList;
	}
	
	//加入redis后的新方法
	@Override
	public List<EasyUITree> findCacheByParentId(Long parentId) {
		
		String key = "ITEM_CAT_"+parentId;
		String result = jedisCluster.get(key);
		List<EasyUITree> easyUITreeList = null;
		try {
			//判断数据是否为null
				//org.springframework.util.StringUtils,spring中的工具类
				//此方法内会先判断是否==null,再判断是否为空串
			if(StringUtils.isEmpty(result)){
				//如果缓存内为空,那么查询数据库
				easyUITreeList = 
						findItemCatByParentId(parentId);
				//用工具类将数据转化为JSON串
				String jsonData = objectMapper.writeValueAsString(easyUITreeList);
				//将数据保存到缓存中
				jedisCluster.set(key, jsonData);
				return easyUITreeList;
			}else{
				//表示缓存数据不为null,那么要将result中的json串转换为java对象;
				EasyUITree[] easyUITrees = 
				objectMapper.readValue(result,EasyUITree[].class);
				//上面只能得到一个数组,我们需要将数组转换为一个list来返回
				easyUITreeList = Arrays.asList(easyUITrees);
				return easyUITreeList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return null;
	}
	
	
	/**
	 * 思考:
	 * 	1.首先获取1级商品分类信息
	 * 	2.根据1级商品分类菜单查询2级商品分类信息
	 *  3.根据2级商品分类信息查询3级商品分类信息
	 *  根据上述的描述信息,数据库查询的效率太低了.(一个用户就需要循环查询20*20次)
	 *  
	 *  改进:
	 *  1.能否采用合理的数据结构,让我们的查询只查询一次.
	 *  	实现商品分类的划分.
	 *    定义一个   Map<parentId,List<ItemCat>> ,key为当前父集,v为旗下的子集;
	 *    那么查询一次数据库,就为这个map进行赋值,此map中有值以后就可以在此map中灵活的获取子集信息;
	 */
	@Override
	public ItemCatResult findItemCatAll() {
		
		//1.实现商品分类划分
		Map<Long,List<ItemCat>> map = 
				new HashMap<Long,List<ItemCat>>();
		
		//2.获取全部的商品(状态值为1)分类信息
		ItemCat itemCatDB = new ItemCat();
		itemCatDB.setStatus(1);//1表示状态正常,2表示删除
			//利用通用mapper查询
		List<ItemCat> itemCats = 
				itemCatMapper.select(itemCatDB);
		
		//3.实现商品数据的封装
		for (ItemCat itemCatTemp : itemCats) {
			//判断map中是否有此父类ID的信息,能进此判断,就表示Map中已经包含了该商品分类的父级 Id
			if(map.containsKey(itemCatTemp.getParentId())){
			//get得到的就是子集的List集合,add表示将刚用通用mapper查询到的数据信息放入该map的v值list集合中封装;
				map.get(itemCatTemp.getParentId()).add(itemCatTemp);
			}else{
				//else表示map中没有此父类id信息,第一次进来,则新建kv,存入map
				List<ItemCat> itemCatTempList = 
						new ArrayList<ItemCat>();
				itemCatTempList.add(itemCatTemp);
				map.put(itemCatTemp.getParentId(), itemCatTempList);
			}
		}
		
		ItemCatResult result = new ItemCatResult();
		
		//4.准备一级商品分类的List集合信息
		List<ItemCatData> itemCatDataList1 = 
								new ArrayList<ItemCatData>();
		
		//5.为一级商品分类赋值
			//map.get得到的是一个list集合,其中元素为1级的数据信息itemCat1(如家用电器)
		for (ItemCat itemCat1 : map.get(0L)) {
			ItemCatData itemCatData1 = new ItemCatData();
			//将itemCat1的信息赋值给data所需要的u,n
			itemCatData1.setUrl("/products/"+itemCat1.getId()+".html");
			itemCatData1.setName("<a href='"+ itemCatData1.getUrl()+"'>"+ itemCat1.getName() +"</a>");
			
			//实现2级商品分类信息
			List<ItemCatData> itemCatDataList2 = 
					new ArrayList<ItemCatData>();
			
			//循环遍历2级商品分类信息
				//通过1级的id从map中获取旗下2级的数据信息的list集合,并且遍历list中的数据(如生活电器)
			for (ItemCat itemCat2 : map.get(itemCat1.getId())) {
				ItemCatData itemCatData2 = new ItemCatData();
				itemCatData2.setUrl("/products/"+itemCat2.getId());
				itemCatData2.setName(itemCat2.getName());
				
				//实现三级商品分类信息维护
				List<String> itemCatDataList3 = new ArrayList<String>();
				
				for (ItemCat itemCat3 : map.get(itemCat2.getId())) {
					//将遍历的数据添加到3级商品list中
					itemCatDataList3.
				add("/products/"+ itemCat3.getId()+"|" + itemCat3.getName());
				}
				//将3级商品信息赋值给2级所需的i
				itemCatData2.setItems(itemCatDataList3);
				//将遍历的数据添加到2级商品list中
				itemCatDataList2.add(itemCatData2);
			}
			//将2级商品信息赋值给1级所需的i
			itemCatData1.setItems(itemCatDataList2);
			//将遍历的数据添加到1级商品list中
			itemCatDataList1.add(itemCatData1);
			//页面不够显示了,因此设定第14次就跳出循环
			if(itemCatDataList1.size() > 13){
				break;
			}
		}
		result.setItemCats(itemCatDataList1);
		return result;
	}
	
	/**
	 * 1.先查询缓存数据
	 * 	如果数据不为null: 需要将缓存中的数据转化为java对象
	 *  如果数据为null:  需要查询数据库,之后将数据保存到缓存中.
	 */
	@Override
	public ItemCatResult findItemCatCache() {
		String key = "ITEM_CAT_ALL";
		String jsonData = jedisCluster.get(key);
		ItemCatResult itemCatResult = null;
		try {
			if(StringUtils.isEmpty(jsonData)){
				itemCatResult = findItemCatAll();
				//将数据保存到缓存中
				String itemCatJSON = 
				objectMapper.writeValueAsString(itemCatResult);
				jedisCluster.set(key, itemCatJSON);
				//System.out.println("商品分类列表第一次查询");
			}else{
				//表示缓存中有数据
				itemCatResult
				= objectMapper.readValue(jsonData,ItemCatResult.class);
				//System.out.println("商品分类列表缓存操作");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemCatResult;
	}
	


}
