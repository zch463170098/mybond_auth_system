package com.xidian.mybond.dao;

import com.xidian.mybond.bean.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by Zch On 2019/3/17 22:31
 **/
@Mapper
public interface UserDao {

    /**
     * 用户数据新增
     */
    @Insert("insert into t_user(user_name,password,email,phone) values (#{userName},#{password},#{email},#{phone})")
    void addUser(User user);

    /**
     * 用户数据修改
     */
    @Update("update t_user set password=#{password},email=#{email},phone=#{phone} where user_name=#{userName}")
    void updateUser(User user);

    /**
     * 用户数据删除
     */
    @Delete("delete from t_user where user_name=#{userName}")
    void deleteUser(String userName);

    /**
     * 根据用户名称查询用户信息
     *
     */
    @Select("SELECT user_name,password,email,phone FROM t_user where user_name=#{userName}")
    @Results({
            @Result(property = "userName",  column = "user_name")
    })
    User findUserByName(String userName);

    /**
     * 查询所有
     */
    @Select("SELECT user_name,password,email,phone FROM t_user")
    @Results({
            @Result(property = "userName",  column = "user_name")
    })
    List<User> findAll();
}