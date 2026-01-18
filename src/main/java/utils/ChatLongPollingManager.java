package utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Gerenciador de conexões Long Polling para o chat
 * Mantém uma lista de threads aguardando por novas mensagens em cada sala
 */
public class ChatLongPollingManager {
    
    // Mapa de salaId -> Lista de latches aguardando
    private static final Map<Integer, List<CountDownLatch>> waitingRequests = new ConcurrentHashMap<>();
    
    /**
     * Registra uma thread para aguardar por novas mensagens em uma sala
     * @param salaId ID da sala
     * @param latch CountDownLatch que será liberado quando houver nova mensagem
     */
    public static void aguardarMensagens(Integer salaId, CountDownLatch latch) {
        waitingRequests.computeIfAbsent(salaId, k -> new CopyOnWriteArrayList<>()).add(latch);
    }
    
    /**
     * Remove um latch da lista de espera
     * @param salaId ID da sala
     * @param latch CountDownLatch a ser removido
     */
    public static void removerAguardando(Integer salaId, CountDownLatch latch) {
        List<CountDownLatch> latches = waitingRequests.get(salaId);
        if (latches != null) {
            latches.remove(latch);
            // Remove a entrada do mapa se a lista ficar vazia
            if (latches.isEmpty()) {
                waitingRequests.remove(salaId);
            }
        }
    }
    
    /**
     * Notifica todas as threads aguardando por mensagens em uma sala
     * Chamado quando uma nova mensagem é enviada
     * @param salaId ID da sala que recebeu nova mensagem
     */
    public static void notificarNovaMensagem(Integer salaId) {
        List<CountDownLatch> latches = waitingRequests.get(salaId);
        if (latches != null && !latches.isEmpty()) {
            // Libera todos os latches aguardando
            for (CountDownLatch latch : latches) {
                latch.countDown();
            }
            // Limpa a lista após notificar
            latches.clear();
        }
    }
    
    /**
     * Aguarda por novas mensagens com timeout
     * @param salaId ID da sala
     * @param timeout Tempo máximo de espera
     * @param unit Unidade de tempo
     * @return true se foi notificado antes do timeout, false se expirou
     */
    public static boolean aguardarComTimeout(Integer salaId, long timeout, TimeUnit unit) {
        CountDownLatch latch = new CountDownLatch(1);
        aguardarMensagens(salaId, latch);
        
        try {
            // Aguarda até ser notificado ou timeout
            boolean notificado = latch.await(timeout, unit);
            return notificado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            // Remove o latch da lista de espera se ainda estiver lá
            removerAguardando(salaId, latch);
        }
    }
    
    /**
     * Retorna o número de conexões aguardando em uma sala (para debug/monitoramento)
     * @param salaId ID da sala
     * @return Número de conexões aguardando
     */
    public static int contarAguardando(Integer salaId) {
        List<CountDownLatch> latches = waitingRequests.get(salaId);
        return latches != null ? latches.size() : 0;
    }
    
    /**
     * Limpa todas as conexões aguardando (útil para testes ou shutdown)
     */
    public static void limparTodos() {
        for (List<CountDownLatch> latches : waitingRequests.values()) {
            for (CountDownLatch latch : latches) {
                latch.countDown();
            }
            latches.clear();
        }
        waitingRequests.clear();
    }
}

