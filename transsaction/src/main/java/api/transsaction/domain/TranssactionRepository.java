package api.transsaction.domain;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;


public interface TranssactionRepository extends ReactiveMongoRepository<Transsaction,String>
{
    Mono<Transsaction> findByIdentityDni(String identityDni);
}
