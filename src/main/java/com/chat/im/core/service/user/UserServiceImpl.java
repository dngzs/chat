package com.chat.im.core.service.user;

import com.chat.im.core.dao.CtUserMapper;
import com.chat.im.core.entity.CtUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private CtUserMapper ctUserMapper;

    public List<CtUser> getAll(){
        return  ctUserMapper.selectAll();
    }

}
