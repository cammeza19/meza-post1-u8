package com.example.auditoria.config;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Envuelve un caso de uso en un proxy que ejecuta cada invocacion dentro de una transaccion.
 * Permite que el circulo Use Cases tenga limites transaccionales sin importar org.springframework.
 */
class TransaccionalUseCaseFactory {

    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    TransaccionalUseCaseFactory(PlatformTransactionManager transactionManager) {
        this.escritura = new TransactionTemplate(transactionManager);
        this.lectura = new TransactionTemplate(transactionManager);
        this.lectura.setReadOnly(true);
    }

    <T> T envolver(Class<T> contrato, T implementacion) {
        return crearProxy(contrato, implementacion, escritura);
    }

    <T> T envolverSoloLectura(Class<T> contrato, T implementacion) {
        return crearProxy(contrato, implementacion, lectura);
    }

    private static <T> T crearProxy(Class<T> contrato, T implementacion, TransactionTemplate tx) {
        Object proxy = Proxy.newProxyInstance(contrato.getClassLoader(), new Class<?>[]{contrato},
                (instancia, metodo, args) -> {
                    if (metodo.getDeclaringClass() == Object.class) {
                        return metodo.invoke(implementacion, args);
                    }
                    return tx.execute(estado -> invocar(implementacion, metodo, args));
                });
        return contrato.cast(proxy);
    }

    private static Object invocar(Object destino, Method metodo, Object[] args) {
        try {
            return metodo.invoke(destino, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
