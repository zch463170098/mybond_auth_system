package com.xidian.mybond.service.impl;


import com.xidian.mybond.bean.User;
import com.xidian.mybond.dao.UserDao;
import com.xidian.mybond.service.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zch On 2019/3/17 23:00
 **/
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;


    @Override
    public boolean addUser(User user) {
        boolean flag=false;
        try{
            userDao.addUser(user);
            flag=true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public boolean updateUser(User user) {
        boolean flag=false;
        try{
            userDao.updateUser(user);
            flag=true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public boolean deleteUser(String userName) {
        boolean flag=false;
        try{
            userDao.deleteUser(userName);
            flag=true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public User findUserByName(String userName) {
        return userDao.findUserByName(userName);
    }


    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }
}

