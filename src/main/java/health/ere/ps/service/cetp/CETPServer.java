package health.ere.ps.service.cetp;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import org.wildfly.common.net.Inet;

import health.ere.ps.config.AppConfig;
import health.ere.ps.service.cetp.codec.CETPDecoder;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.gematik.PharmacyService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class CETPServer {
    public static final int PORT = 8585;
    private static Logger log = Logger.getLogger(CETPServer.class.getName());

    EventLoopGroup bossGroup; // (1)
    EventLoopGroup workerGroup;

    @Inject
    PharmacyService pharmacyService;

    @Inject
    SecretsManagerService secretsManagerService;

    @Inject
    AppConfig appConfig;

    void onStart(@Observes StartupEvent ev) {               
        log.info("Running CETP Server on port "+PORT);
        run();

    }

    void onShutdown(@Observes ShutdownEvent ev) {               
        log.info("Shutdown CETP Server on port "+PORT);
        if(workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if(bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

    }

    public void run() {
        bossGroup = new NioEventLoopGroup(); // (1)
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // (3)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                    try {

                        SslContext sslContext = SslContextBuilder
                            .forServer(secretsManagerService.getKeyManagerFactory())
                            .clientAuth(ClientAuth.NONE)
                            .build();

                        ch.pipeline()
                            .addLast("ssl", sslContext.newHandler(ch.alloc()))
                            .addLast(new CETPDecoder())
                            .addLast(new CETPServerHandler(pharmacyService, appConfig.getCardLinkServer().orElse("wss://cardlink.service-health.de:8444/websocket/80276003650110006580-20230112")));
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Failed to create SSL context", e);
                    }
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
    
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(PORT).sync(); // (7)
    
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture(); //.sync();
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "CETP Server interrupted", e);
        }
    }
}
