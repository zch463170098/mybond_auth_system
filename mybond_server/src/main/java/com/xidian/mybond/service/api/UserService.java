package com.xidian.mybond.service.api;


import com.xidian.mybond.bean.User;

import java.util.List;

/**
 * Created by Zch On 2019/3/17 22:58
 **/
public interface UserService {

    /**
     * 新增用户
     * @param user
     * @return
     */
    boolean addUser(User user);

    /**
     * 修改用户
     * @param user
     * @return
     */
    boolean updateUser(User user);


    /**
     * 删除用户
     * @param userName
     * @return
     */
    boolean deleteUser(String userName);

    /**
     * 根据用户名字查询用户信息
     * @param userName
     */
    User findUserByName(String userName);



    /**
     * 查询所有
     * @return
     */
    List<User> findAll();
}