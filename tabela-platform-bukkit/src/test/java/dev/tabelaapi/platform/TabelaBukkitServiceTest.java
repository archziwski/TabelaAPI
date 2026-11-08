package dev.tabelaapi.platform;

import dev.tabelaapi.api.TabelaAction;
import dev.tabelaapi.api.TabelaCancelReason;
import dev.tabelaapi.api.TabelaOpenException;
import dev.tabelaapi.api.TabelaPrompt;
import dev.tabelaapi.api.TabelaSession;
import dev.tabelaapi.api.TabelaValidators;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TabelaBukkitServiceTest {
    private static final TabelaSettings SETTINGS =
        new TabelaSettings(Duration.ofSeconds(30), 64, 10, Duration.ZERO, true);

    private final FakeTransport transport = new FakeTransport();
    private final FakeScheduler scheduler = new FakeScheduler();
    private final Plugin plugin = plugin();

    private TabelaBukkitService service(TabelaSettings settings) {
        return new TabelaBukkitService(plugin, transport, settings, scheduler);
    }

    @Test
    void valid_input_completes_and_closes_the_session() {
        TabelaBukkitService service = service(SETTINGS);
        Player player = player();
        List<String> received = new ArrayList<>();

        service.open(player, TabelaPrompt.builder()
            .lines("Ali", "", "", "")
            .onComplete(result -> {
                received.addAll(result.lines());
                return TabelaAction.close();
            })
            .build());
        transport.submit("Ali", "", "", "");

        assertThat(received).containsExactly("Ali", "", "", "");
        assertThat(service.activeSessions()).isZero();
        assertThat(transport.closes).isOne();
    }

    @Test
    void validation_failure_reopens_the_editor_and_rearms_the_timeout() {
        TabelaBukkitService service = service(SETTINGS);
        Player player = player();
        AtomicInteger cancels = new AtomicInteger();

        service.open(player, TabelaPrompt.builder()
            .lines("", "", "", "")
            .validator(0, TabelaValidators.notBlank())
            .onCancel(result -> {
                cancels.incrementAndGet();
                return TabelaAction.close();
            })
            .build());
        long firstRound = transport.opens;
        transport.submit("   ", "", "", "");

        assertThat(transport.opens).isEqualTo(firstRound + 1);
        assertThat(scheduler.pending()).isEqualTo(2);

        scheduler.fireNext();
        assertThat(cancels).hasValue(0);
        assertThat(service.activeSessions()).isOne();

        scheduler.fireNext();
        assertThat(cancels).hasValue(1);
        assertThat(service.activeSessions()).isZero();
    }

    @Test
    void validation_failure_cancels_when_reopening_is_disabled() {
        TabelaBukkitService service = service(new TabelaSettings(
            Duration.ofSeconds(30), 64, 10, Duration.ZERO, false));
        Player player = player();
        AtomicInteger cancels = new AtomicInteger();

        service.open(player, TabelaPrompt.builder()
            .validator(TabelaValidators.notBlank())
            .onCancel(result -> {
                cancels.incrementAndGet();
                return TabelaAction.close();
            })
            .build());
        transport.submit("", "", "", "");

        assertThat(cancels).hasValue(1);
        assertThat(service.activeSessions()).isZero();
        assertThat(transport.opens).isOne();
    }

    @Test
    void timeout_cancels_the_session() {
        TabelaBukkitService service = service(SETTINGS);
        Player player = player();
        AtomicInteger cancels = new AtomicInteger();

        service.open(player, TabelaPrompt.builder()
            .timeout(Duration.ofSeconds(5))
            .onCancel(result -> {
                cancels.incrementAndGet();
                return TabelaAction.close();
            })
            .build());

        assertThat(scheduler.pending()).isEqualTo(1);
        assertThat(scheduler.nextDelay()).isEqualTo(Duration.ofSeconds(5));

        scheduler.fireNext();

        assertThat(cancels).hasValue(1);
        assertThat(service.activeSessions()).isZero();
    }

    @Test
    void opening_again_replaces_the_previous_session() {
        TabelaBukkitService service = service(SETTINGS);
        Player player = player();
        AtomicInteger cancels = new AtomicInteger();

        service.open(player, TabelaPrompt.builder()
            .onCancel(result -> {
                cancels.incrementAndGet();
                return TabelaAction.close();
            })
            .build());
        TabelaSession second = service.open(player, TabelaPrompt.builder().build());

        assertThat(cancels).hasValue(1);
        assertThat(service.activeSessions()).isOne();
        assertThat(second.playerId()).isEqualTo(player.getUniqueId());
    }

    @Test
    void rate_limit_blocks_reopening() {
        TabelaBukkitService service = service(new TabelaSettings(
            Duration.ofSeconds(30), 64, 10, Duration.ofSeconds(5), true));
        Player player = player();

        service.open(player, TabelaPrompt.builder().build());

        assertThatThrownBy(() -> service.open(player, TabelaPrompt.builder().build()))
            .isInstanceOf(TabelaOpenException.class);
        assertThat(service.activeSessions()).isOne();
    }

    @Test
    void max_active_sessions_limits_other_players() {
        TabelaBukkitService service = service(new TabelaSettings(
            Duration.ofSeconds(30), 64, 1, Duration.ZERO, true));

        service.open(player(), TabelaPrompt.builder().build());

        assertThatThrownBy(() -> service.open(player(), TabelaPrompt.builder().build()))
            .isInstanceOf(TabelaOpenException.class);
        assertThat(service.activeSessions()).isOne();
    }

    @Test
    void null_packet_lines_become_empty_strings() {
        TabelaBukkitService service = service(SETTINGS);
        Player player = player();
        List<String> received = new ArrayList<>();

        service.open(player, TabelaPrompt.builder()
            .onComplete(result -> {
                received.addAll(result.lines());
                return TabelaAction.close();
            })
            .build());
        transport.submit((String[]) null);

        assertThat(received).containsExactly("", "", "", "");
    }

    @Test
    void reopen_action_keeps_the_prompt_validator() {
        TabelaBukkitService service = service(SETTINGS);
        Player player = player();

        service.open(player, TabelaPrompt.builder()
            .validator(TabelaValidators.notBlank())
            .onComplete(result -> TabelaAction.reopen("yeni"))
            .build());
        transport.submit("Ali", "", "", "");

        assertThat(transport.opens).isEqualTo(2);
        assertThat(transport.lastPrompt.lines()).containsExactly("yeni", "", "", "");
        assertThat(transport.lastPrompt.validationFor(0)).isPresent();

        transport.submit("", "", "", "");

        assertThat(transport.opens).isEqualTo(3);
        assertThat(service.activeSessions()).isOne();
    }

    @Test
    void shutdown_cancels_open_sessions() {
        TabelaBukkitService service = service(SETTINGS);
        Player player = player();
        AtomicInteger cancels = new AtomicInteger();

        service.open(player, TabelaPrompt.builder()
            .onCancel(result -> {
                cancels.incrementAndGet();
                return TabelaAction.close();
            })
            .build());
        service.shutdown();

        assertThat(cancels).hasValue(1);
        assertThat(service.activeSessions()).isZero();
    }

    @Test
    void cancel_after_timeout_does_not_fire_a_second_callback() {
        TabelaBukkitService service = service(SETTINGS);
        Player player = player();
        AtomicInteger cancels = new AtomicInteger();

        service.open(player, TabelaPrompt.builder()
            .onCancel(result -> {
                cancels.incrementAndGet();
                return TabelaAction.close();
            })
            .build());
        scheduler.fireNext();
        service.cancel(player, TabelaCancelReason.DISCONNECTED);

        assertThat(cancels).hasValue(1);
    }

    private static Player player() { return player(UUID.randomUUID()); }

    private static Player player(UUID id) {
        return (Player) Proxy.newProxyInstance(TabelaBukkitServiceTest.class.getClassLoader(),
            new Class<?>[]{Player.class}, (proxy, method, args) -> switch (method.getName()) {
                case "getUniqueId" -> id;
                case "isOnline" -> true;
                case "getName" -> "TestPlayer";
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "FakePlayer[" + id + "]";
                default -> defaultValue(method.getReturnType());
            });
    }

    private static Plugin plugin() {
        return (Plugin) Proxy.newProxyInstance(TabelaBukkitServiceTest.class.getClassLoader(),
            new Class<?>[]{Plugin.class}, (proxy, method, args) -> switch (method.getName()) {
                case "isEnabled" -> true;
                case "getName" -> "TabelaAPI";
                case "getServer" -> server();
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "FakePlugin";
                default -> defaultValue(method.getReturnType());
            });
    }

    private static Server server() {
        return (Server) Proxy.newProxyInstance(TabelaBukkitServiceTest.class.getClassLoader(),
            new Class<?>[]{Server.class}, (proxy, method, args) -> defaultValue(method.getReturnType()));
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0d;
        if (type == float.class) return 0.0f;
        if (type == void.class) return null;
        return 0;
    }

    private static final class FakeScheduler implements TabelaScheduler {
        private final List<Duration> delays = new ArrayList<>();
        private final List<Runnable> tasks = new ArrayList<>();

        @Override public void run(Plugin plugin, Player player, Runnable task) { task.run(); }

        @Override public void runLater(Plugin plugin, Player player, Duration delay, Runnable task) {
            delays.add(delay);
            tasks.add(task);
        }

        int pending() { return tasks.size(); }

        Duration nextDelay() { return delays.get(0); }

        void fireNext() {
            Runnable task = tasks.remove(0);
            delays.remove(0);
            task.run();
        }
    }

    private static final class FakeTransport implements SignTransport {
        private Consumer<String[]> pending;
        private int opens;
        private int closes;
        private TabelaPrompt lastPrompt;

        @Override public void open(Player player, TabelaPrompt prompt, Consumer<String[]> result) {
            opens++;
            lastPrompt = prompt;
            pending = result;
        }

        @Override public void close(Player player) {
            closes++;
            pending = null;
        }

        void submit(String... lines) {
            Consumer<String[]> consumer = pending;
            if (consumer == null) throw new IllegalStateException("No open editor");
            pending = null;
            consumer.accept(lines);
        }
    }
}
