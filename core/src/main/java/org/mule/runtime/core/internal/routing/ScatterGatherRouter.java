/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.core.internal.routing;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.core.api.config.i18n.CoreMessages.noEndpointsForRouter;
import static org.mule.runtime.core.api.routing.ForkJoinStrategy.RoutingPair.of;
import static org.mule.runtime.core.api.rx.Exceptions.propagateWrappingFatal;
import static org.mule.runtime.core.internal.routing.FirstSuccessfulRoutingStrategy.validateMessageIsNotConsumable;
import static reactor.core.publisher.Flux.fromIterable;

import java.util.List;
import java.util.function.Consumer;

import org.mule.runtime.core.api.InternalEvent;
import org.mule.runtime.core.api.processor.MessageProcessorChain;
import org.mule.runtime.core.api.routing.ForkJoinStrategy;
import org.mule.runtime.core.api.routing.ForkJoinStrategyFactory;
import org.mule.runtime.core.api.routing.RoutePathNotFoundException;
import org.mule.runtime.core.internal.routing.forkjoin.CollectMapForkJoinStrategyFactory;

import org.reactivestreams.Publisher;

/**
 * <p>
 * The <code>Scatter-Gather</code> router will broadcast copies of the current message to every route in parallel subject to any
 * limitation in concurrency that has been configured
 * <p>
 * For advanced use cases, a custom {@link ForkJoinStrategyFactory} can be applied to customize the logic used to aggregate the
 * route responses back into one single Event.
 * <p>
 * <b>EIP Reference:</b> <a href="http://www.eaipatterns.com/BroadcastAggregate.html"<a/>
 * </p>
 *
 * @since 3.5.0
 */
public class ScatterGatherRouter extends AbstractForkJoinRouter {

  private List<MessageProcessorChain> routes = emptyList();

  @Override
  protected Consumer<InternalEvent> onEvent() {
    return event -> {
      validateMessageIsNotConsumable(event.getMessage());
      if (isEmpty(routes)) {
        propagateWrappingFatal(new RoutePathNotFoundException(noEndpointsForRouter(), null));
      }
    };
  }

  @Override
  protected Publisher<ForkJoinStrategy.RoutingPair> getRoutingPairs(InternalEvent event) {
    return fromIterable(routes).map(route -> of(event, route));
  }

  @Override
  protected List<MessageProcessorChain> getOwnedObjects() {
    return routes;
  }

  public void setRoutes(List<MessageProcessorChain> routes) {
    checkArgument(routes.size() > 1, "At least 2 routes are required for ScatterGather");
    this.routes = routes;
  }

  @Override
  protected boolean isDelayErrors() {
    return true;
  }

  @Override
  protected int getDefaultMaxConcurrency() {
    return routes.size();
  }

  @Override
  protected ForkJoinStrategyFactory getDefaultForkJoinStrategyFactory() {
    return new CollectMapForkJoinStrategyFactory();
  }
}