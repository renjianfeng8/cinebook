package com.example.springboot.mapper;

import com.example.springboot.common.BaseMapper;
import com.example.springboot.entity.Admin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminMapper extends BaseMapper<Admin> {

    Admin selectByUsername(String username);

    /** 删除前锁全表管理员行：count-then-delete 期间不允许并发增删，避免最后两个管理员被同时删掉 */
    List<Integer> selectIdsForUpdate();

    void updatePassword(Admin admin);

    /** 吊销点：改密时写入最新令牌的 iat，早于它的令牌一律拒绝 */
    int updateRevokedBefore(@Param("id") Integer id, @Param("epochSecond") long epochSecond);

    /** 该账号的吊销点（epoch 秒）；从未吊销时为 null */
    Long selectRevokedBefore(Integer id);

}
