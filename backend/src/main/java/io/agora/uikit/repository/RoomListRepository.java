package io.agora.uikit.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import io.agora.uikit.bean.entity.RoomListEntity;

@Repository
public interface RoomListRepository extends MongoRepository<RoomListEntity, String> {
    Page<RoomListEntity> findByCreateTimeLessThan(Long createTime, Pageable pageAble);

    List<RoomListEntity> findByCreateTimeLessThan(Long createTime);

    @Query("{_id : ?0}")
    void updatById(String id, Update update);
}
