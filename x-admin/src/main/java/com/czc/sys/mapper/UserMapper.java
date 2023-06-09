package com.czc.sys.mapper;

import com.czc.sys.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author czc
 * @since 2023-06-08
 */
public interface UserMapper extends BaseMapper<User> {

    List<String> getRoleNamesByUserId(Integer id);
}
