package org.tinygame.herostory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.tinygame.herostory.cmdhandler.CmdHandlerFactory;
import org.tinygame.herostory.mq.MqProducer;
import org.tinygame.herostory.util.RedisUtil;

/**
 * 服务器入口类
 *
 * 用单线程来计算，用多线程来IO
 */
@SpringBootApplication
public class ServerMain {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(ServerMain.class);
    /**
     * 服务器端口号
     */
    static private final int SERVER_PORT = 12345;

    /**
     * 服务端启动主函数
     *
     * @param args
     */
    public static void main(String[] args) {
        // 设置log4j属性文件
        PropertyConfigurator.configure(ServerMain.class.getClassLoader().getResourceAsStream("log4j.properties"));

        // 初始化命令处理器工厂
        CmdHandlerFactory.init();
        // 初始化消息识别器
        GameMsgRecognizer.init();
        // 初始化MySQL会话工厂
        MySqlSessionFactory.init();
        // 初始化Redis
        RedisUtil.init();
        // 初始化消息队列
        MqProducer.init();

        // 拉客的线程池
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 干活的线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        // 初始化Netty服务器
        ServerBootstrap b = new ServerBootstrap();
        // 设置线程池
        b.group(bossGroup, workerGroup);
        // 服务器信道的处理方式
        b.channel(NioServerSocketChannel.class);
        b.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        // 前三个都是固定的写法
                        // Http服务器编解码器
                        new HttpServerCodec(),
                        // 内容长度限制
                        new HttpObjectAggregator(65535),
                        // WebSocket协议处理器，在这里处理握手、ping、pong等消息
                        new WebSocketServerProtocolHandler("/websocket"),
                        // 添加自定义的消息解码器
                        new GameMsgDecoder(),
                        // 添加自定义的消息编码器
                        new GameMsgEncoder(),
                        // 添加自定义消息处理器
                        new GameMsgHandler()
                        );
            }
        });
        b.option(ChannelOption.SO_BACKLOG, 128);
        // 一直KeepAlive，保持连接不要断开
        b.childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            // 绑定端口号12345
            // 注意：实际项目中会使用argvArray中的参数来指定端口号
            ChannelFuture f = b.bind(SERVER_PORT).sync();

            // 判断服务器是否启动成功
            if (f.isSuccess()) {
                LOGGER.info("游戏服务器启动成功");
            }
            // 等待服务器信道关闭
            // 也就是不要立即退出应用程序，让应用程序可以一直提供服务
            f.channel().closeFuture().sync();
        }catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }finally {
            // 关闭服务器(优雅地结束两个线程池)
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}