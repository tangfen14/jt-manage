package com.jt.manage.mapper;

import com.jt.common.mapper.SysMapper;
import com.jt.manage.pojo.User;

//必须实现SysMapper接口，泛型接口，加上泛型<User>
public interface UserMapper extends SysMapper<User>{

}
