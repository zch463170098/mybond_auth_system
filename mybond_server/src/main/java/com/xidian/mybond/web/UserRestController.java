package com.xidian.mybond.web;


import com.google.gson.Gson;
import com.xidian.mybond.bean.OperationResult;
import com.xidian.mybond.bean.User;
import com.xidian.mybond.bean.UserLogin;
import com.xidian.mybond.service.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Zch On 2019/3/17 23:32
 **/
@RestController
@RequestMapping(value = "/mybond/user")
public class UserRestController {
    @Autowired
    private UserService userService;
    private Gson gson = new Gson();

    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    public OperationResult addUser(@RequestBody String registerData) {
        System.out.println("开始新增..."+registerData);
        User user = gson.fromJson(registerData,User.class);
        OperationResult operationResult = new OperationResult();
        if(userService.addUser(user)){
            operationResult.setStatus(true);
            operationResult.setOperation("register");
            operationResult.setMessage("注册成功");
            return operationResult;
        }else{
            operationResult.setStatus(false);
            operationResult.setOperation("register");
            operationResult.setMessage("注册失败，用户名已存在");
            return operationResult;
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public OperationResult login(@RequestBody String loginData) {
        System.out.println("用户登录，"+loginData);
        UserLogin userLogin = gson.fromJson(loginData,UserLogin.class);
        String name = userLogin.getUserName();
        String password = userLogin.getPassword();
        User myUser = userService.findUserByName(name);
        OperationResult operationResult = new OperationResult();
        if(myUser.getPassword().equals(password)){
            operationResult.setStatus(true);
            operationResult.setOperation("login");
            operationResult.setMessage("登录成功");
            return operationResult;
        }else{
            operationResult.setStatus(false);
            operationResult.setOperation("login");
            operationResult.setMessage("登录失败，用户密码错误");
            return operationResult;
        }
    }


    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    public OperationResult updateUser(@RequestBody String updateUserData) {
        System.out.println("开始更新..."+updateUserData);
        User user = gson.fromJson(updateUserData,User.class);
        OperationResult operationResult = new OperationResult();
        if(userService.updateUser(user)){
            operationResult.setStatus(true);
            operationResult.setOperation("updateUser");
            operationResult.setMessage("个人修改成功");
            return operationResult;
        }else{
            operationResult.setStatus(false);
            operationResult.setOperation("updateUser");
            operationResult.setMessage("个人信息修改失败，用户不存在");
            return operationResult;
        }
    }

    @RequestMapping(value = "/deleteUser", method = RequestMethod.POST)
    public boolean delete(@RequestBody String deleteUserName) {
        System.out.println("开始删除..."+deleteUserName);
        String userName = deleteUserName;
        return userService.deleteUser(userName);
    }

    @RequestMapping(value = "/findUser", method = RequestMethod.POST)
    public User findByUserName(@RequestBody String findUserName) {
        System.out.println("开始查询..."+findUserName);
        String userName = findUserName;
        return userService.findUserByName(userName);
    }


    @RequestMapping(value = "/userAll", method = RequestMethod.GET)
    public List<User> findAll() {
        System.out.println("开始查询所有数据...");
        return userService.findAll();
    }
}
