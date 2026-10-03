package com.example.auditoria.config;

import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHistorialUseCase;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.ObtenerDashboardAuditoriaUseCase;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.impl.CerrarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHistorialService;
import com.example.auditoria.usecase.impl.IniciarRemediacionService;
import com.example.auditoria.usecase.impl.ObtenerDashboardAuditoriaService;
import com.example.auditoria.usecase.impl.ReabrirHallazgoService;
import com.example.auditoria.usecase.impl.RegistrarHallazgoService;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wiring explicito del circulo Frameworks & Drivers: los casos de uso son clases Java puras
 * (sin anotaciones de Spring) y aqui se ensamblan con sus puertos. Cada caso de uso se envuelve
 * en una transaccion para que el cambio de estado y su registro en la bitacora sean atomicos.
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
    public IniciarRemediacionUseCase iniciarRemediacionUseCase(HallazgoRepositoryPort repo,
                                                               HistorialAuditoriaPort historial) {
        return transaccional.envolver(IniciarRemediacionUseCase.class,
                new IniciarRemediacionService(repo, historial));
    }

    @Bean
    public CerrarHallazgoUseCase cerrarHallazgoUseCase(HallazgoRepositoryPort repo,
                                                       HistorialAuditoriaPort historial) {
        return transaccional.envolver(CerrarHallazgoUseCase.class, new CerrarHallazgoService(repo, historial));
    }

    @Bean
    public ReabrirHallazgoUseCase reabrirHallazgoUseCase(HallazgoRepositoryPort repo,
                                                         HistorialAuditoriaPort historial) {
        return transaccional.envolver(ReabrirHallazgoUseCase.class, new ReabrirHallazgoService(repo, historial));
    }

    @Bean
    public ConsultarHallazgoUseCase consultarHallazgoUseCase(HallazgoRepositoryPort repo) {
        return transaccional.envolverSoloLectura(ConsultarHallazgoUseCase.class, new ConsultarHallazgoService(repo));
    }

    @Bean
    public ObtenerDashboardAuditoriaUseCase obtenerDashboardAuditoriaUseCase(HallazgoRepositoryPort repo) {
        return transaccional.envolverSoloLectura(ObtenerDashboardAuditoriaUseCase.class,
                new ObtenerDashboardAuditoriaService(repo));
    }

    @Bean
    public ConsultarHistorialUseCase consultarHistorialUseCase(HallazgoRepositoryPort repo,
                                                               HistorialAuditoriaPort historial) {
        return transaccional.envolverSoloLectura(ConsultarHistorialUseCase.class,
                new ConsultarHistorialService(repo, historial));
    }
}
