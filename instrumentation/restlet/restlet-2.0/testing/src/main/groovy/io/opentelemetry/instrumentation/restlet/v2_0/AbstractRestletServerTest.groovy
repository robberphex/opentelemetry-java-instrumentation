/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.restlet.v2_0


import io.opentelemetry.instrumentation.test.base.HttpServerTest
import io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint
import org.restlet.Component
import org.restlet.Context
import org.restlet.Request
import org.restlet.Response
import org.restlet.Restlet
import org.restlet.Server
import org.restlet.data.Form
import org.restlet.data.MediaType
import org.restlet.data.Protocol
import org.restlet.data.Status
import org.restlet.routing.Redirector
import org.restlet.routing.Router
import org.restlet.routing.Template
import org.restlet.routing.VirtualHost

import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.CAPTURE_HEADERS
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.ERROR
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.EXCEPTION
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.INDEXED_CHILD
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.NOT_FOUND
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.PATH_PARAM
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.QUERY_PARAM
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.REDIRECT
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.SUCCESS

abstract class AbstractRestletServerTest extends HttpServerTest<Server> {

  Component component
  VirtualHost host

  @Override
  Server startServer(int port) {

    component = new Component()
    host = component.getDefaultHost()
    def server = setupServer(component)
    setupRouting()

    component.start()

    return server
  }

  @Override
  void stopServer(Server server) {
    component.stop()
  }

  def attachAndWrap(path, restlet) {
    host.attach(path, wrapRestlet(restlet, path))
  }

  Server setupServer(Component component) {
    return component.getServers().add(Protocol.HTTP, port)
  }

  void setupRouting() {

    def defaultRouter = wrapRestlet(new Router(host.getContext()), "/*")
    host.attach("/", defaultRouter).setMatchingMode(Template.MODE_STARTS_WITH)

    attachAndWrap(SUCCESS.path, new Restlet() {
      @Override
      void handle(Request request, Response response) {
        controller(SUCCESS) {
          response.setEntity(SUCCESS.body, MediaType.TEXT_PLAIN)
          response.setStatus(Status.valueOf(SUCCESS.status), SUCCESS.body)
        }
      }
    })

    attachAndWrap(REDIRECT.path, new Redirector(Context.getCurrent(), REDIRECT.body, Redirector.MODE_CLIENT_FOUND) {
      @Override
      void handle(Request request, Response response) {
        super.handle(request, response)
        controller(REDIRECT) {
        }
      }
    })

    attachAndWrap(ERROR.path, new Restlet() {
      @Override
      void handle(Request request, Response response) {
        controller(ERROR) {
          response.setStatus(Status.valueOf(ERROR.getStatus()), ERROR.getBody())
        }
      }
    })

    attachAndWrap(EXCEPTION.path, new Restlet() {
      @Override
      void handle(Request request, Response response) {
        controller(EXCEPTION) {
          throw new Exception(EXCEPTION.getBody())
        }
      }
    })

    attachAndWrap(QUERY_PARAM.path, new Restlet() {
      @Override
      void handle(Request request, Response response) {
        controller(QUERY_PARAM) {
          response.setEntity(QUERY_PARAM.getBody(), MediaType.TEXT_PLAIN)
          response.setStatus(Status.valueOf(QUERY_PARAM.getStatus()), QUERY_PARAM.getBody())
        }
      }
    })

    attachAndWrap("/path/{id}/param", new Restlet() {
      @Override
      void handle(Request request, Response response) {
        controller(PATH_PARAM) {
          response.setEntity(PATH_PARAM.getBody(), MediaType.TEXT_PLAIN)
          response.setStatus(Status.valueOf(PATH_PARAM.getStatus()), PATH_PARAM.getBody())
        }
      }
    })

    attachAndWrap("/captureHeaders", new Restlet() {
      @Override
      void handle(Request request, Response response) {
        controller(CAPTURE_HEADERS) {

          def requestHeaders = request.getAttributes().get("org.restlet.http.headers")
          def responseHeaders

          try {
            def headerClass = Class.forName("org.restlet.data.Header")
            def seriesClass = Class.forName("org.restlet.util.Series")
            //to avoid constructor error (Series is abstract in 2.0.x)
            responseHeaders = response.getAttributes().computeIfAbsent("org.restlet.http.headers", { seriesClass.newInstance(headerClass) })

          } catch (ClassNotFoundException | NoClassDefFoundError e) {

            responseHeaders = response.getAttributes().computeIfAbsent("org.restlet.http.headers", { new Form() })
          }

          responseHeaders.add("X-Test-Response", requestHeaders.getValues("X-Test-Request"))

          response.setEntity(CAPTURE_HEADERS.getBody(), MediaType.TEXT_PLAIN)
          response.setStatus(Status.valueOf(CAPTURE_HEADERS.getStatus()), CAPTURE_HEADERS.getBody())
        }
      }
    })

    attachAndWrap(INDEXED_CHILD.path, new Restlet() {
      @Override
      void handle(Request request, Response response) {
        controller(INDEXED_CHILD) {
          INDEXED_CHILD.collectSpanAttributes { request.getOriginalRef().getQueryAsForm().getFirst(it).getValue() }
          response.setStatus(Status.valueOf(INDEXED_CHILD.status))
        }
      }
    })

  }

  @Override
  boolean testPathParam() {
    true
  }

  @Override
  boolean testErrorBody() {
    false
  }

  @Override
  String expectedHttpRoute(ServerEndpoint endpoint) {
    switch (endpoint) {
      case PATH_PARAM:
        return getContextPath() + "/path/{id}/param"
      case NOT_FOUND:
        return getContextPath() + "/*"
      default:
        return super.expectedHttpRoute(endpoint)
    }
  }

  Restlet wrapRestlet(Restlet restlet, String path) {
    return restlet
  }
}