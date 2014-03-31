package com.wizzardo.httpserver;

import com.wizzardo.epoll.EpollServer;
import com.wizzardo.epoll.readable.ReadableBytes;
import com.wizzardo.httpserver.request.Header;
import com.wizzardo.httpserver.request.RequestHeaders;
import com.wizzardo.httpserver.response.Response;
import simplehttpserver.concurrent.NonBlockingQueue;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author: moxa
 * Date: 11/5/13
 */
public class HttpServer extends EpollServer<HttpConnection> {

    private NonBlockingQueue<Runnable> queue = new NonBlockingQueue<Runnable>();

    public HttpServer() {
        for (int i = 0; i < 16; i++) {
            new Worker(queue, "worker_" + i);
        }
    }

    @Override
    protected HttpConnection createConnection(int fd, int ip, int port) {
        return new HttpConnection(fd, ip, port);
    }

    @Override
    public void readyToRead(final HttpConnection connection) {

        queue.add(new Runnable() {
            @Override
            public void run() {
                ByteBuffer b;
                try {

                    while ((b = read(connection, connection.getBufferSize())).limit() > 0) {
                        if (!connection.check(b))
                            return;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    connection.reset("exception while reading");
                    close(connection);
                    return;
                }

                connection.reset("read headers");

                try {
                    Response response = handleRequest(connection);

                    connection.writeData = response.toReadableByteArray();
                } catch (Throwable t) {
                    t.printStackTrace();
                    //TODO render error page
                }

                readyToWrite(connection);
            }
        });

    }

    public Response handleRequest(HttpConnection connection) {
        return new Response()
                .appendHeader("Connection", "Keep-Alive")
                .appendHeader("Content-Type", "text/html;charset=UTF-8")
                .setBody("ololo".getBytes());
    }

    @Override
    public void readyToWrite(final HttpConnection connection) {
        try {
            ReadableBytes data = connection.writeData;
            RequestHeaders headers = connection.headers;
            if (data == null || data.isComplete()) {
                stopWriting(connection);
                readyToRead(connection);
            } else {
                while (!data.isComplete() && write(connection, data) > 0) {
                }
                if (!data.isComplete()) {
                    startWriting(connection);
                    return;
                }
                if (Header.VALUE_CONNECTION_CLOSE.equalsIgnoreCase(headers.get(Header.KEY_CONNECTION))) {
                    close(connection);
                    connection.reset("close. not keep-alive");
                }
            }
        } catch (IOException e) {
            close(connection);
            onCloseConnection(connection);
            e.printStackTrace();
        }
    }


    @Override
    public void onOpenConnection(HttpConnection httpConnection) {
//        System.out.println("new connection " + httpConnection);
    }

    @Override
    public void onCloseConnection(HttpConnection httpConnection) {
//        System.out.println("close " + httpConnection);
    }

    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.bind(8084, 1000);
        server.start();
    }
}