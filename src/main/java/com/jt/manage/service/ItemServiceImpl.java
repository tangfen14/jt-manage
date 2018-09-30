package com.jt.manage.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.common.vo.EasyUIResult;
import com.jt.manage.mapper.ItemDescMapper;
import com.jt.manage.mapper.ItemMapper;
import com.jt.manage.pojo.Item;
import com.jt.manage.pojo.ItemDesc;

@Service
public class ItemServiceImpl implements ItemService {
	
	@Autowired
	private ItemMapper itemMapper;
	
	@Autowired
	private ItemDescMapper itemDescMapper;

	@Override
	public List<Item> findItemAll() {
		
		return itemMapper.findItemAll();
	}

	@Override
	public EasyUIResult findItemByPage(Integer page, Integer rows) {
		
		/**
		 * 通用Mapper 查询操作时  如果传入的数据不为null,则会充当where条件
		 * select count(*) from tb_item
		 * select * from tb_item limit 0,20
s		   select * from tb_item limit 20,20
s          select * from tb_item limit 40,20
		 */
		int total = itemMapper.selectCount(null);
		
		int start = (page - 1) * rows;
		
		List<Item> itemList = itemMapper.findItemByPage(start,rows);
		
		EasyUIResult result = new EasyUIResult(total, itemList);
		
		return result;
	}
	
	
	//保存数据,要实现入库两张表(商品表和描述表)
	@Override
	public void saveItem(Item item,String desc) {
		//需要补齐数据
		item.setStatus(1); //表示商品上架
		item.setCreated(new Date());
		item.setUpdated(item.getCreated());
		itemMapper.insert(item);
		
		//思考:此处在多线程的情况下,会不会拿到不正确的id值?
		//答:不会,我们有时候在表中会看到类似100,101,103,105这样的自增主键,中间的102和104不见的
		//原因是因为在添加数据的时候可能出错了,事务回滚了,而数据库是不会再重新分配已经分配过的自增值的,因此回到
		//这个问题,我们获得的线程内的最大id值,不可能是别的商品的id值
		
		ItemDesc itemDesc = new ItemDesc();
		/*System.out.println(item.getId());
		描述表中的id值从商品表中取出,两表1对1的关系;
		这里要关注下内部的操作,按理论说此时的id是没有数据的,
		因为Item对象中的id是主键自增的,那么它只会在真正的在数据库中提交了才有值;
		因此我们这里应该需要获取当前线程内Id的最大值才对,SELECT LAST_INSERT_ID(),
		而测试发现这里我们自己没有操作但是依然可以拿到id值,是因为由于我们设定了这个id是主键自增的,
		底层Mybatis在itemMapper.insert(item);这句新增后,帮我们执行了上行的查询语句,
		并回传给了我们现在在使用的item对象
		
	注:SELECT LAST_INSERT_ID();   这行代码是不能单独操作的,它要配合比如新增等操作一起执行,
	才能查询到结果,可以获取当前线程内Id的最大值
		*/
		itemDesc.setItemId(item.getId());
		
		
		itemDesc.setItemDesc(desc);
		itemDesc.setCreated(item.getCreated());
		itemDesc.setUpdated(item.getCreated());
		itemDescMapper.insert(itemDesc);
		
	}
	
	@Override
	public void updateItem(Item item,String desc) {
		//为数据赋值
		item.setUpdated(new Date());
		//表示动态更新操作. 只更新不为null的数据
		itemMapper.updateByPrimaryKeySelective(item);
		
		
		//商品描述信息更新
		ItemDesc itemDesc = new ItemDesc();
		itemDesc.setItemId(item.getId());
		itemDesc.setItemDesc(desc);
		itemDesc.setUpdated(item.getUpdated());
		itemDescMapper.updateByPrimaryKeySelective(itemDesc);
	}
	
	
	@Override
	public void deleteItems(Long[] ids) {
		//根据主键删除,商品表和描述表的两组对应的数据
		itemMapper.deleteByIDS(ids);
		itemDescMapper.deleteByIDS(ids);
		/*注意:这里必须将删除两表的动作放在一个业务层方法中完成,切记不能分两个业务层方法来些;
		因为事务是切业务层,删除数据涉及到出错后的事务回滚,如果分拆了,那就是两个分开的事务了,就不能同步回滚了*/ 
	}
	
	@Override
	public void updateStatus(int status, Long[] ids) {
		//update tb_item set status = #{status},updated = now() where id in (1,2,3,4,5)
		
		itemMapper.updateStatus(status,ids);
		
	}
	
	//根据itemId查询商品详情信息
	@Override
	public ItemDesc findItemDesc(Long itemId) {
		
		return itemDescMapper.selectByPrimaryKey(itemId);
	}

	@Override
	public Item finditemById(Long itemId) {
		//根据主键查询
		return itemMapper.selectByPrimaryKey(itemId);
	}


}
