package com.infernalsuite.isdownloadapi.database.repository;

import com.infernalsuite.isdownloadapi.database.model.VersionFamily;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersionFamilyCollection extends MongoRepository<VersionFamily, ObjectId> {
    List<VersionFamily> findAllByProject(final ObjectId project);

    Optional<VersionFamily> findByProjectAndName(final ObjectId project, final String name);
}
