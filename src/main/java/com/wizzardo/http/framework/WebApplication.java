package com.wizzardo.http.framework;

import com.wizzardo.http.HttpConnection;
import com.wizzardo.http.HttpServer;
import com.wizzardo.http.Worker;
import com.wizzardo.http.framework.di.DependencyFactory;
import com.wizzardo.http.framework.template.LocalResourcesTools;
import com.wizzardo.http.request.Request;
import com.wizzardo.http.response.Response;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wizzardo on 28.04.15.
 */
public class WebApplication<T extends HttpConnection> extends HttpServer<T> {

    public WebApplication(int port) {
        super(port);
    }

    public WebApplication(String host, int port) {
        super(host, port);
    }

    public WebApplication(String host, int port, String context) {
        super(host, port, context);
    }

    public WebApplication(String host, int port, int workersCount) {
        super(host, port, workersCount);
    }

    public WebApplication(String host, int port, String context, int workersCount) {
        super(host, port, context, workersCount);
    }

    protected void init() {
        List<Class> classes = new LocalResourcesTools().getClasses();
        DependencyFactory.get().setClasses(classes);
    }

    @Override
    protected Worker<T> createWorker(BlockingQueue<T> queue, String name) {
        return new WebWorker<T>(queue, name) {
            @Override
            protected void process(T connection) {
                processConnection(connection);
            }
        };
    }

    @Override
    protected Response handle(Request request, Response response) throws IOException {
        ((WebWorker) Thread.currentThread()).setRequestHolder(new RequestHolder(request, response));
        return super.handle(request, response);
    }
}
