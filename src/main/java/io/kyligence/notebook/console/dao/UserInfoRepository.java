package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {

    @Query(value = "select * from user_info where name = ?1", nativeQuery = true)
    List<UserInfo> findByName(String name);
}
