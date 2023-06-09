package com.czc.sys.service.impl;

import ch.qos.logback.core.boolex.Matcher;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.czc.sys.entity.User;
import com.czc.sys.mapper.UserMapper;
import com.czc.sys.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author czc
 * @since 2023-06-08
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

//    @Override
//    public Map<String, Object> login(User user) {
//        // 根据用户名查询
//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(User::getUsername,user.getUsername());
//        User loginUser = this.baseMapper.selectOne(wrapper);
//        // 结果不为空，并且密码和传入密码匹配，则生成token，并将用户信息存入redis
//        if(loginUser != null && passwordEncoder.matches(user.getPassword(),loginUser.getPassword())){
//            // 暂时用UUID, 终极方案是jwt
//            String key = "user:" + UUID.randomUUID();
//            // 存入redis
//            loginUser.setPassword(null);
//            redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);
//
//            // 返回数据
//            Map<String, Object> data = new HashMap<>();
//            data.put("token",key);
//            return data;
//        }
//        return null;
//    }

    @Override
    public Map<String, Object> login(User user) {
        // 根据用户名和密码查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,user.getUsername());
        wrapper.eq(User::getPassword,user.getPassword());
        User loginUser = this.baseMapper.selectOne(wrapper);
        // 结果不为空，则生成token，并将用户信息存入redis
        if(loginUser != null){
            // 暂时用UUID, 终极方案是jwt
            String key = "user:" + UUID.randomUUID();

            // 存入redis
            loginUser.setPassword(null);
            redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);

            // 返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token",key);
            return data;
        }
        return null;
    }



    @Override
    public Map<String, Object> getUserInfo(String token) {
        // 从redis查询token
        Object obj = redisTemplate.opsForValue().get(token);
        // 反序列化
        User user = JSON.parseObject(JSON.toJSONString(obj), User.class);
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", user.getUsername());
            data.put("avatar", user.getAvatar());
            List<String> roleList = this.getBaseMapper().getRoleNamesByUserId(user.getId());
            data.put("roles", roleList);
            return data;
        }
        return null;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(token);
    }


}
