package com.infernalsuite.isdownloadapi.database.repository;

import com.infernalsuite.isdownloadapi.database.model.Artifact;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtifactCollection extends MongoRepository<Artifact, ObjectId> {
    List<Artifact> findAllByProjectAndVersionAndBuild(final ObjectId project, final ObjectId version, final ObjectId build);
    Optional<Artifact> findByProjectAndVersionAndBuildAndName(final ObjectId project, final ObjectId version, final ObjectId build, final String name);
}
