package com.infernalsuite.isdownloadapi.database.repository;

import com.infernalsuite.isdownloadapi.database.model.Build;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BuildCollection extends MongoRepository<Build, ObjectId> {
    List<Build> findAllByProjectAndVersion(final ObjectId project, final ObjectId version);

    List<Build> findAllByProjectAndVersionIn(final ObjectId project, final Collection<ObjectId> version);

    Optional<Build> findByProjectAndVersionAndNumber(final ObjectId project, final ObjectId version, final int number);
}
