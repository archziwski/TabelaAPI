package dev.tabelaapi.velocity;

import com.velocitypowered.api.proxy.Player;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Proxy-side contract. Rendering always happens on the backend server. */
public interface TabelaVelocityBridge {
    CompletableFuture<BridgeResult> request(Player player, String backend, String requestId);
    record BridgeResult(UUID playerId, String requestId, boolean accepted, String message) { }
}
