package com.infernalsuite.isdownloadapi.database.repository;

import com.infernalsuite.isdownloadapi.database.model.Latest;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface LatestCollection extends MongoRepository<Latest, ObjectId> {
    Optional<Latest> findByProject(final ObjectId project);
    Optional<Latest> findByProjectAndVersion(final ObjectId project, final ObjectId version);
    @Transactional
    default void updateLatest(@Nullable ObjectId oldLatest, @NonNull Latest newLatest) {
        if (oldLatest != null) this.deleteById(oldLatest);
        this.save(newLatest);
    }
}
