package com.infernalsuite.isdownloadapi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
class MongoConfiguration {
    @Bean
    MappingMongoConverter mappingMongoConverter(
            final MongoDatabaseFactory mongoDatabaseFactory,
            final MongoMappingContext context,
            final MongoCustomConversions mongoCustomConversions
    ) {
        final DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
        final MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
        mappingConverter.setCustomConversions(mongoCustomConversions);
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null)); // to remove _class
        return mappingConverter;
    }
}
