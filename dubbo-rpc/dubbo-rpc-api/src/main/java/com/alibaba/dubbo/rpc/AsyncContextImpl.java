/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncContextImpl implements AsyncContext {
    private static final Logger logger = LoggerFactory.getLogger(AsyncContextImpl.class);

    private final AtomicBoolean started = new AtomicBoolean(false);

    private CompletableFuture<Object> future;

    public AsyncContextImpl() {
    }

    public AsyncContextImpl(CompletableFuture<Object> future) {
        this.future = future;
    }

    @Override
    public void addListener(Runnable run) {

    }

    @Override
    public void write(Object value) {
        if (stop()) {
            if (value instanceof Throwable) {
                // TODO check exception type like ExceptionFilter do.
                Throwable bizExe = (Throwable) value;
                future.complete(new RpcResult(bizExe));
            }
            future.complete(new RpcResult(value));
        } else {
            throw new IllegalStateException("The async response has probably been wrote back by another thread, or the asyncContext has been closed.");
        }
    }

    @Override
    public boolean isAsyncStarted() {
        return started.get();
    }

    @Override
    public boolean stop() {
        if (started.compareAndSet(true, false)) {
//            future.cancel(true);
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        this.started.set(true);
    }
}
