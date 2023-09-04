package api.transsaction.presentation;

import api.transsaction.application.TranssactionService;
import api.transsaction.domain.Transsaction;
import api.transsaction.presentation.mapper.TranssactionMapper;
import api.transsaction.presentation.model.TranssactionModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/transsaction")
public class TranssactionController
{
    @Autowired(required = true)
    private TranssactionService transsactionService;
    @Autowired
    private TranssactionMapper transsactionMapper;

    @Operation(summary = "Listar todos los monederos Transsaction registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los monederos Transsaction registrados",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Transsaction.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findAll")
    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackGetAllTranssaction")
    @TimeLimiter(name = "transsactionTimeLimiter")
    @Timed(description = "transsactionGetAll")
    public Flux<TranssactionModel> getAll() {
        log.info("getAll executed");
        return transsactionService.findAll()
                .map(transsaction -> transsactionMapper.entityToModel(transsaction));
    }


    @Operation(summary = "Listar todos los monederos Transsaction por Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los monederos transsaction por Id",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Transsaction.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findById/{id}")
    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackFindById")
    @TimeLimiter(name = "transsactionTimeLimiter")
    @Timed(description = "transsactionsGetById")
    public Mono<ResponseEntity<TranssactionModel>> findById(@PathVariable String id){
        return transsactionService.findById(id)
                .map(transsaction -> transsactionMapper.entityToModel(transsaction))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los registros por DNI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los registros por DNI",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Transsaction.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findByIdentityDni/{identityDni}")
    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackFindByIdentityDni")
    @TimeLimiter(name = "transsactionTimeLimiter")
    public Mono<ResponseEntity<TranssactionModel>> findByIdentityDni(@PathVariable String identityDni){
        log.info("findByIdentityDni executed {}", identityDni);
        Mono<Transsaction> response = transsactionService.findByIdentityDni(identityDni);
        return response
                .map(transsaction -> transsactionMapper.entityToModel(transsaction))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Registro de los Monederos Transsaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se registro el monedero de manera exitosa",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Transsaction.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @PostMapping
    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackCreateTranssaction")
    @TimeLimiter(name = "transsactionTimeLimiter")
    public Mono<ResponseEntity<TranssactionModel>> create(@Valid @RequestBody TranssactionModel request){
        log.info("create executed {}", request);
        return transsactionService.create(transsactionMapper.modelToEntity(request))
                .map(transsaction -> transsactionMapper.entityToModel(transsaction))
                .flatMap(c -> Mono.just(ResponseEntity.created(URI.create(String.format("http://%s:%s/%s/%s", "register", "9081", "transsaction", c.getId())))
                        .body(c)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar el monedero Transsaction por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se actualizará el registro por el ID",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Transsaction.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @PutMapping("/{id}")
    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackUpdateTranssaction")
    @TimeLimiter(name = "transsactionTimeLimiter")
    public Mono<ResponseEntity<TranssactionModel>> updateById(@PathVariable String id, @Valid @RequestBody TranssactionModel request){
        log.info("updateById executed {}:{}", id, request);
        return transsactionService.update(id, transsactionMapper.modelToEntity(request))
                .map(transsaction -> transsactionMapper.entityToModel(transsaction))
                .flatMap(c -> Mono.just(ResponseEntity.created(URI.create(String.format("http://%s:%s/%s/%s", "register", "9081", "transsaction", c.getId())))
                        .body(c)))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Eliminar Monedero Transsaction por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se elimino el registro por ID",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Transsaction.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @DeleteMapping("/{id}")
    @CircuitBreaker(name = "transsactionCircuit", fallbackMethod = "fallbackDeleteTranssaction")
    @TimeLimiter(name = "transsactionTimeLimiter")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable String id){
        log.info("deleteById executed {}", id);
        return transsactionService.delete(id)
                .map( r -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
