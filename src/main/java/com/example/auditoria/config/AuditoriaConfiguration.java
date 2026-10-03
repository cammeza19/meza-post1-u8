package com.example.auditoria.config;

import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.impl.CerrarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHallazgoService;
import com.example.auditoria.usecase.impl.IniciarRemediacionService;
import com.example.auditoria.usecase.impl.ReabrirHallazgoService;
import com.example.auditoria.usecase.impl.RegistrarHallazgoService;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wiring explicito del circulo Frameworks & Drivers: los casos de uso son clases Java puras
 * (sin anotaciones de Spring) y aqui se ensamblan con sus puertos. Cada caso de uso se envuelve
 * en una transaccion para que cada operacion sobre el agregado sea atomica.
 */
@Configuration
public class AuditoriaConfiguration {

    private final TransaccionalUseCaseFactory transaccional;

    public AuditoriaConfiguration(PlatformTransactionManager transactionManager) {
        this.transaccional = new TransaccionalUseCaseFactory(transactionManager);
    }

    @Bean
    public RegistrarHallazgoUseCase registrarHallazgoUseCase(HallazgoRepositoryPort repo) {
        return transaccional.envolver(RegistrarHallazgoUseCase.class, new RegistrarHallazgoService(repo));
    }

    @Bean
    public IniciarRemediacionUseCase iniciarRemediacionUseCase(HallazgoRepositoryPort repo) {
        return transaccional.envolver(IniciarRemediacionUseCase.class,
                new IniciarRemediacionService(repo));
    }

    @Bean
    public CerrarHallazgoUseCase cerrarHallazgoUseCase(HallazgoRepositoryPort repo) {
        return transaccional.envolver(CerrarHallazgoUseCase.class, new CerrarHallazgoService(repo));
    }

    @Bean
    public ReabrirHallazgoUseCase reabrirHallazgoUseCase(HallazgoRepositoryPort repo) {
        return transaccional.envolver(ReabrirHallazgoUseCase.class, new ReabrirHallazgoService(repo));
    }

    @Bean
    public ConsultarHallazgoUseCase consultarHallazgoUseCase(HallazgoRepositoryPort repo) {
        return transaccional.envolverSoloLectura(ConsultarHallazgoUseCase.class, new ConsultarHallazgoService(repo));
    }
}
