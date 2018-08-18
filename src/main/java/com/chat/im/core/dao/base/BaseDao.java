package com.chat.im.core.dao.base;

import com.chat.im.core.entity.CtUser;

import java.util.List;

public interface BaseDao<T> {

    int deleteByPrimaryKey(Long id);

    int insert(CtUser record);

    CtUser selectByPrimaryKey(Long id);

    List<CtUser> selectAll();

    int updateByPrimaryKey(T record);
}
