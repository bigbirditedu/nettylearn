package com.bigbird.netty.httpdemo4;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


@ChannelHandler.Sharable
public class MyNettyHttpServer extends ChannelDuplexHandler implements InitClose, StartStop {
    static Logger log = LoggerFactory.getLogger(MyNettyHttpServer.class);

    int port = 8888;
    String host = "*";
    int idleSeconds = 30;
    int maxConnections = 100000;
    int workerThreads = 0;
    int backlog = 128;
    boolean nativeNetty = false;

    int maxInitialLineLength = 4096;
    int maxHeaderSize = 8192;
    int maxChunkSize = 8192;
    int maxContentLength = 2 * 1024 * 1024;

    NamedThreadFactory bossThreadFactory = new NamedThreadFactory("netty_webserver_boss");
    NamedThreadFactory workThreadFactory = new NamedThreadFactory("netty_webserver_worker");

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    Channel serverChannel;

    ConcurrentHashMap<String, Channel> conns = new ConcurrentHashMap<String, Channel>();

    AtomicBoolean stopFlag = new AtomicBoolean();

    ServerBootstrap serverBootstrap;

    public MyNettyHttpServer() {
    }

    public MyNettyHttpServer(int port) {
        this.port = port;
    }

    @Override
    public void init() {
        int processors = Runtime.getRuntime().availableProcessors();
        if (workerThreads == 0) {
            workerThreads = processors;
        }

        String osName = System.getProperty("os.name");
        if (nativeNetty && osName != null && osName.toLowerCase().indexOf("linux") >= 0) {
            nativeNetty = true;
        } else {
            nativeNetty = false;
        }

        if (nativeNetty) {
            bossGroup = new EpollEventLoopGroup(1, bossThreadFactory);
            workerGroup = new EpollEventLoopGroup(workerThreads, workThreadFactory);
        } else {
            bossGroup = new NioEventLoopGroup(1, bossThreadFactory);
            workerGroup = new NioEventLoopGroup(workerThreads, workThreadFactory);
        }

        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(nativeNetty ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("timeout", new IdleStateHandler(0, 0, idleSeconds));
                        pipeline.addLast("codec", new HttpServerCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize));
                        pipeline.addLast("decompressor", new HttpContentDecompressor());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(maxContentLength));
                        pipeline.addLast("compressor", new HttpContentCompressor());
                        pipeline.addLast("handler", MyNettyHttpServer.this);
                    }
                });
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, backlog);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    @Override
    public void start() {
        InetSocketAddress addr;
        if (host == null || "*".equals(host)) {
            addr = new InetSocketAddress(port); // "0.0.0.0"
        } else {
            addr = new InetSocketAddress(host, port);
        }
        try {
            serverChannel = serverBootstrap.bind(addr).syncUninterruptibly().channel();
            log.info("netty http server started on host(" + addr.getHostString() + ") port(" + port + ")");
        } catch (Exception e) {
            log.error("netty http server bind exception, port=" + port);
            System.exit(-1);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (stopFlag.get()) {
            ctx.close();
            return;
        }

        String connId = getConnId(ctx);
        if (conns.size() >= maxConnections) {
            ctx.close();
            log.error("connection started, connId={}, but not allowed,server max connections exceeded.", connId);
            return;
        }

        log.info("connection started, connId={}", connId);
        conns.put(connId, ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String connId = getConnId(ctx);
        conns.remove(connId);
        log.info("connection ended, connId={}", connId);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.ALL_IDLE) {
                String connId = getConnId(ctx);
                ctx.close();
                log.error("connection timeout, ctx closed,connId={}", connId);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String connId = getConnId(ctx);
        log.error("connection exception, connId=" + connId + ",msg=" + cause.toString(), cause);
        ctx.close();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        try {
            //由于使用了HttpObjectAggregator聚合处理器,这里的msg类型必然是FullHttpRequest
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;

            if (!fullHttpRequest.decoderResult().isSuccess()) {
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, RetCodes.DECODE_REQ_ERROR);
                return;
            }

            if (fullHttpRequest.method() != HttpMethod.GET && fullHttpRequest.method() != HttpMethod.POST
                    && fullHttpRequest.method() != HttpMethod.PUT && fullHttpRequest.method() != HttpMethod.DELETE
                    && fullHttpRequest.method() != HttpMethod.HEAD && fullHttpRequest.method() != HttpMethod.OPTIONS) {
                sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, RetCodes.HTTP_METHOD_NOT_ALLOWED);
                return;
            }

            if (fullHttpRequest.method() == HttpMethod.OPTIONS) {
                String origin = fullHttpRequest.headers().get(HttpHeaderNames.ORIGIN);
                String requestMethod = fullHttpRequest.headers().get(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD);
                if (origin == null || requestMethod == null) {
                    sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, RetCodes.HTTP_METHOD_NOT_ALLOWED);
                    return;
                }
            }

            //为了方便地处理http请求响应，封装了WebReq、WebRes对象
            DefaultWebReq req = convertReq(fullHttpRequest);
            DefaultWebRes res = process(req);
            ctx.channel().writeAndFlush(res);
        } finally {
            //如果不在pipeline中传递ChannelHandler了，则此处手动释放
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理Http请求，通常是调用服务端某个业务接口，并返回响应
     * 这里只是简单演示使用反射方式调用服务端接口方法，并返回json数据，未做复杂的接口路由
     *
     * @param req
     * @return
     */
    private DefaultWebRes process(DefaultWebReq req) {
        String path = req.getPath();
        int i = path.lastIndexOf("/");
        String methodName = path.substring(i + 1);
        String reqContent = req.getContent();
        DefaultWebRes webRes = null;
        try {
            webRes = new DefaultWebRes(req, 200);
            webRes.setContentType("application/json");
            Method method = OrderService.class.getDeclaredMethod(methodName, QueryOrderInfoReq.class);
            method.setAccessible(true);
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> parameterType = parameterTypes[0];
            Object param = JSON.parseObject(reqContent, parameterType);
            Object resContent = method.invoke(new OrderService(), param);
            webRes.setContent(JSON.toJSONString(resContent));
        } catch (NoSuchMethodException noSuchMethodException) {
            String s = String.format(WebConstants.ContentFormat, -627, RetCodes.retCodeText(-627));
            webRes.setContent(s);
        } catch (Exception e) {
            log.error("http call exception", e);
            webRes = new DefaultWebRes(req, 500);
        }
        return webRes;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //这里可以自定义数据处理逻辑，如果已经是FullHttpMessage类型，比如sendError()，则直接透传到pipeline中的出站处理器中
        if (msg instanceof DefaultWebRes) {
            DefaultWebRes data = (DefaultWebRes) msg;
            writeHttpResponse(ctx, promise, data);
        } else {
            super.write(ctx, msg, promise);
        }
    }

    private void writeHttpResponse(ChannelHandlerContext ctx, ChannelPromise promise, DefaultWebRes data) {
        DefaultFullHttpResponse res = null;
        if (data.getContent() != null && !data.getContent().isEmpty()) {
            int size = ByteBufUtil.utf8MaxBytes(data.getContent());
            ByteBuf bb = ctx.alloc().buffer(size);
            bb.writeCharSequence(data.getContent(), Charset.forName(data.getCharSet()));
            int len = bb.readableBytes();
            if (data.isHeadMethod()) {
                res = new DefaultFullHttpResponse(data.getVersion(), HttpResponseStatus.valueOf(data.getHttpCode()));
                ReferenceCountUtil.release(bb);
            } else {
                res = new DefaultFullHttpResponse(data.getVersion(), HttpResponseStatus.valueOf(data.getHttpCode()), bb);
            }
            res.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, len);
        } else {
            res = new DefaultFullHttpResponse(data.getVersion(), HttpResponseStatus.valueOf(data.getHttpCode()));
            res.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 0);  // 302 不返回长度会导致客户端卡住
        }
        res.headers().set(HttpHeaderNames.SERVER, WebConstants.Server);
        if (stopFlag.get()) res.headers().set(WebConstants.ShutdownFlag, "1");

        if (data.isKeepAlive()) {
            res.headers().set(HttpHeaderNames.CONNECTION, "keep-alive");
        }

        if (data.getHeaders() != null) {
            for (String key : data.getHeaders().names()) {
                res.headers().set(key, data.getHeaders().get(key));
            }
        }

        if (data.getCookies() != null) {
            for (io.netty.handler.codec.http.cookie.Cookie c : data.getCookies()) {
                String s = ServerCookieEncoder.STRICT.encode(c);
                res.headers().add(HttpHeaderNames.SET_COOKIE, s);
            }
        }

        ChannelFuture future = ctx.writeAndFlush(res, promise);
        if (!data.isKeepAlive()) {
            //如果没有KeepAlive则在数据传输完成后关闭通道
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    DefaultWebReq convertReq(FullHttpRequest fullHttpRequest) {
        DefaultWebReq req = new DefaultWebReq();
        req.setVersion(fullHttpRequest.protocolVersion());
        req.setMethod(fullHttpRequest.method());
        req.setKeepAlive(HttpUtil.isKeepAlive(fullHttpRequest));

        String uri = fullHttpRequest.uri();
        int p1 = findPathEndIndex(uri);
        String path = p1 >= 0 ? uri.substring(0, p1) : uri;
        path = WebUtils.decodeUrl(path);
        int p2 = uri.indexOf('?');
        String queryString = p2 >= 0 ? uri.substring(p2 + 1) : "";
        req.setPath(path);
        req.setQueryString(queryString);
        req.setHeaders(fullHttpRequest.headers());

        ByteBuf bb = fullHttpRequest.content();
        if (bb != null) {
            String cs = req.getCharSet();
            String content = bb.toString(Charset.forName(cs));
            req.setContent(content);
        }

        return req;
    }

    @Override
    public void stop() {
        stopFlag.set(true);
    }

    @Override
    public void close() {
        if (workerGroup != null) {
            log.info("stopping netty http server");
            bossGroup.shutdownGracefully();
            bossGroup = null;

            ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            allChannels.add(serverChannel);
            for (Channel ch : conns.values()) {
                allChannels.add(ch);
            }
            ChannelGroupFuture future = allChannels.close();
            future.awaitUninterruptibly();

            workerGroup.shutdownGracefully();
            workerGroup = null;

            log.info("netty http server stopped");
        }
    }


    Channel getChannel(String connId) {
        return conns.get(connId);
    }

    String getConnId(ChannelHandlerContext ctx) {
        Channel ch = ctx.channel();
        return parseIpPort(ch.remoteAddress().toString()) + ":" + ch.id().asShortText();
    }

    public String getAddr(String connId) {
        int p = connId.lastIndexOf(":");
        return connId.substring(0, p);
    }

    String parseIp(String connId) {
        int p = connId.lastIndexOf(":");
        if (p < 0) return connId;
        int p2 = connId.lastIndexOf(":", p - 1);
        if (p2 < 0) return connId;
        return connId.substring(0, p2);
    }

    String parseIpPort(String s) {
        int p = s.indexOf("/");

        if (p >= 0)
            return s.substring(p + 1);
        else
            return s;
    }

    int findPathEndIndex(String uri) {
        int len = uri.length();
        for (int i = 0; i < len; i++) {
            char c = uri.charAt(i);
            if (c == '?' || c == '#') {
                return i;
            }
        }
        return -1;
    }

    void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, int retCode) {
        ByteBuf bb = ctx.alloc().buffer(32);
        String s = String.format(WebConstants.ContentFormat, retCode, RetCodes.retCodeText(retCode));
        bb.writeCharSequence(s, CharsetUtil.UTF_8);
        int len = bb.readableBytes();
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, bb);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, WebConstants.MIMETYPE_JSON);
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, len);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
