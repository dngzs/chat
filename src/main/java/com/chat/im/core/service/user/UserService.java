package com.chat.im.core.service.user;

import com.chat.im.core.entity.CtUser;

import java.util.List;

/**
 * 用户管理接口
 */
public interface UserService {

    List<CtUser> getAll();
}
