package org.jetlinks.zlmedia.runner.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.lang3.ArrayUtils;
import org.jetlinks.zlmedia.ZLMediaOperations;
import org.jetlinks.zlmedia.restful.RestfulZLMediaOperations;
import org.jetlinks.zlmedia.restful.ZLMediaConfigs;
import org.jetlinks.zlmedia.runner.ZLMediaRuntime;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于 {@link ProcessBuilder} fork 运行zlmedia进程.
 *
 * @author zhouhao
 * @since 2.2
 */
@Slf4j
public class ProcessZLMediaRuntime implements ZLMediaRuntime {

    private final String processFile;
    private final String[] args;
    private Process process;

    private final Sinks.Many<String> output = Sinks
        .many()
        .unicast()
        .onBackpressureBuffer();

    private final Disposable.Composite disposable = Disposables.composite();

    private final Sinks.One<Void> startAwait = Sinks.one();

    private int restartCount;

    private final ZLMediaOperations operations;
    private final Map<String, String> configs = new ConcurrentHashMap<>();

    public ProcessZLMediaRuntime(String processFile) {
        this(processFile, new ZLMediaConfigs());
    }

    public ProcessZLMediaRuntime(String processFile, ZLMediaConfigs configs) {
        this(processFile,
             WebClient.builder(),
             new ObjectMapper(),
             configs);
    }

    @SneakyThrows
    private static void storeInit(Map<String, String> conf, File path) {
        Configurations configs = new Configurations();
        INIConfiguration ini = configs.ini(path);
        conf.forEach(ini::setProperty);
        FileWriter fileWriter = new FileWriter(path);
        ini.write(fileWriter);
        fileWriter.close();
        ini.clear();
    }

    public ProcessZLMediaRuntime(String processFile,
                                 WebClient.Builder builder,
                                 ObjectMapper mapper,
                                 ZLMediaConfigs configs) {
        this.processFile = processFile;
        this.configs.putAll(configs.createConfigs());
        this.args = configs.getCommandArgs();
        String secure = configs.getSecret();
        this.operations = new RestfulZLMediaOperations(
            builder
                .clone()
                .baseUrl("http://127.0.0.1:" + configs.getPorts().getHttp())
                .filter((request, exchange) -> exchange.exchange(
                    ClientRequest
                        .from(request)
                        .url(UriComponentsBuilder
                                 .fromUri(request.url())
                                 .queryParam("secret", secure)
                                 .build()
                                 .toUri())
                        .build()
                ))
                .build(),
            configs,
            mapper);
    }

    @Override
    @SneakyThrows
    public Mono<Void> start() {
        return Mono
            //启动
            .fromRunnable(this::start0)
            .subscribeOn(Schedulers.boundedElastic())
            //等待
            .then(startAwait.asMono());
    }


    protected long getPid() {
        try {
            if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                return f.getLong(process);
            }
            return -1;
        } catch (Throwable e) {
            return -1;
        }
    }

    @SneakyThrows
    protected synchronized void start0() {
        File file = new File(processFile);
        if (isDisposed() || process != null) {
            return;
        }
        storeInit(this.configs, new File(new File(processFile).getParent(), "config.ini"));

        Path pidFile = Paths.get(processFile + ".pid");
        if (pidFile.toFile().exists()) {
            try {
                String pid = new String(Files.readAllBytes(pidFile));
                log.warn("zlmedia process already exists, kill it:{}", pid);
                Runtime
                    .getRuntime()
                    .exec(new String[]{"kill", pid})
                    .waitFor();
            } catch (Throwable e) {
                log.warn("kill zlmedia process error", e);
            }
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(file.getAbsolutePath());
        if (ArrayUtils.isNotEmpty(this.args)) {
            cmd.addAll(Arrays.asList(this.args));
        }
        process = new ProcessBuilder()
            .command(cmd)
            .directory(file.getParentFile())
            .redirectErrorStream(true)
            .inheritIO()
            .start();
        long pid = getPid();

        if (pid > 0) {
            Files.write(pidFile,
                        String.valueOf(pid).getBytes(),
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE);
            pidFile
                .toFile()
                .deleteOnExit();

            disposable.add(() -> {
                boolean ignore = pidFile.toFile().delete();
            });
        }

        //监听进程退出
        disposable
            .add(
                Mono
                    .<DataBuffer>fromCallable(() -> {
                        try {
                            processExit(process.waitFor());
                        } catch (InterruptedException ignore) {
                            processExit(-1);
                        }
                        return null;
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe()
            );

        //定时检查是否启动成功
        disposable.add(
            Flux.interval(Duration.ofSeconds(2), Duration.ofSeconds(1))
                .onBackpressureDrop()
                .concatMap(ignore -> operations
                    .opsForState()
                    .isAlive())
                .filter(Boolean::booleanValue)
                .take(1)
                .subscribe(ignore -> {
                    restartCount = 0;
                    startAwait.tryEmitEmpty();
                })
        );
        if (isDisposed()) {
            process.destroy();
        }
    }

    private void handleOutput(String line) {
        output.tryEmitNext(line);
    }

    protected void processExit(int code) {
        if (disposable.isDisposed()) {
            return;
        }
        //启动中...
        if (startAwait.currentSubscriberCount() > 0) {
            startAwait.tryEmitError(new ZLMediaProcessException(code, "ZLMediaKit start failed,code:" + code));
        } else {
            log.warn("ZLMediaKit exit with code:{}", code);
        }
        if (restartCount > 10) {
            log.error("ZLMediaKit exit with code:{},restart count > 10,stop restart", code);
            return;
        }
        process = null;
        restartCount++;
        Schedulers
            .boundedElastic()
            .schedule(() -> {
                if (disposable.isDisposed()) {
                    return;
                }
                start0();
            }, 2, TimeUnit.SECONDS);
        //  disposable.dispose();
    }


    @Override
    public Flux<String> output() {
        return output.asFlux();
    }

    @Override
    public ZLMediaOperations getOperations() {
        return operations;
    }


    @Override
    public void dispose() {
        disposable.dispose();
        if (null != process) {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException ignore) {

            }
        }
    }
}
