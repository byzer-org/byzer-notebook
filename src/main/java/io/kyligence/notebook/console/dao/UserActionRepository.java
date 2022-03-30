package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.UserAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Integer> {

    @Query(value = "select * from user_action where `user` = ?1", nativeQuery = true)
    List<UserAction> findByUser(String user);
}
