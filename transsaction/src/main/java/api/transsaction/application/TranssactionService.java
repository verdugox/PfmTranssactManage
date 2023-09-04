package api.transsaction.application;

import api.transsaction.config.CircuitResilienceListener;
import api.transsaction.domain.Transsaction;
import api.transsaction.domain.TranssactionRepository;
import api.transsaction.presentation.mapper.TranssactionMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
public class TranssactionService
{
    @Autowired
    private TranssactionRepository transsactionRepository;
    @Autowired
    private CircuitResilienceListener circuitResilienceListener;
    @Autowired
    private TimeLimiterRegistry timeLimiterRegistry;
    @Autowired
    private TranssactionMapper transsactionMapper;

    @Autowired
    private ReactiveHashOperations<String, String, Transsaction> hashOperations;

    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackGetAllTranssaction")
    @TimeLimiter(name = "transsactionTimeLimiter")
    public Flux<Transsaction> findAll(){
        log.debug("findAll executed");

        // Intenta obtener todos los monederos transsaction desde el caché de Redis
        Flux<Transsaction> cachedTranssaction = hashOperations.values("TranssactionRedis")
                .flatMap(transsaction -> Mono.justOrEmpty((Transsaction) transsaction));

        // Si hay datos en la caché de Redis, retornarlos
        return cachedTranssaction.switchIfEmpty(transsactionRepository.findAll()
                .flatMap(transsaction -> {
                    // Almacena cada monedero transsaction en la caché de Redis
                    return hashOperations.put("TranssactionRedis", transsaction.getId(), transsaction)
                            .thenReturn(transsaction);
                }));

    }

    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackFindById")
    @TimeLimiter(name = "transsactionTimeLimiter")
    public Mono<Transsaction> findById(String transsactionId)
    {
        log.debug("findById executed {}" , transsactionId);
        return  hashOperations.get("TranssactionRedis",transsactionId)
                .switchIfEmpty(transsactionRepository.findById(transsactionId)
                        .flatMap(transsaction -> hashOperations.put("TranssactionRedis",transsaction.getId(),transsaction)
                                .thenReturn(transsaction)));
    }

    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackGetAllItems")
    @TimeLimiter(name = "transsactionTimeLimiter")
    public Mono<Transsaction> findByIdentityDni(String identityDni){
        log.debug("findByIdentityDni executed {}" , identityDni);
        return transsactionRepository.findByIdentityDni(identityDni);
    }

    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackFindByIdentityDni")
    @TimeLimiter(name = "transsactionTimeLimiter")
    public Mono<Transsaction> create(Transsaction transsaction){
        log.debug("create executed {}",transsaction);
        transsaction.setDateRegister(LocalDate.now());
        return transsactionRepository.save(transsaction);
    }

    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackUpdateTranssaction")
    @TimeLimiter(name = "transsactionTimeLimiter")
    public Mono<Transsaction> update(String transsactionId, Transsaction transsaction){
        log.debug("update executed {}:{}", transsactionId, transsaction);
        return transsactionRepository.findById(transsactionId)
                .flatMap(dbTranssaction -> {
                    transsaction.setDateRegister(dbTranssaction.getDateRegister());
                    transsactionMapper.update(dbTranssaction, transsaction);
                    return transsactionRepository.save(dbTranssaction);
                });
    }

    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackDeleteTranssaction")
    @TimeLimiter(name = "transsactionTimeLimiter")
    public Mono<Transsaction>delete(String transsactionId){
        log.debug("delete executed {}",transsactionId);
        return transsactionRepository.findById(transsactionId)
                .flatMap(existingTranssaction -> transsactionRepository.delete(existingTranssaction)
                        .then(Mono.just(existingTranssaction)));
    }
}
