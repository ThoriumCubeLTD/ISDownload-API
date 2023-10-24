package com.infernalsuite.isdownloadapi.database.repository;

import com.infernalsuite.isdownloadapi.database.model.Version;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersionCollection extends MongoRepository<Version, ObjectId> {
    List<Version> findAllByProject(final ObjectId project);

    List<Version> findAllByProjectAndGroup(final ObjectId project, final ObjectId group);

    Optional<Version> findByProjectAndName(final ObjectId project, final String name);
}