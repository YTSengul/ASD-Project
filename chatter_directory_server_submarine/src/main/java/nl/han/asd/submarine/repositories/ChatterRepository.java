package nl.han.asd.submarine.repositories;

import nl.han.asd.submarine.models.Chatter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatterRepository extends MongoRepository<Chatter, String> {

    boolean existsByAliasOrUsername(String alias, String username);

    Chatter findDistinctByUsername(String username);

    Chatter findIpAddressByAlias(String alias);

}
