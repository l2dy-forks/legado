package io.legado.app.help

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 全局线程池，用于执行需要独立线程的后台任务。
 * 使用多线程池替代单线程池，提升并发性能。
 */
val globalExecutor: ExecutorService by lazy {
    Executors.newFixedThreadPool(
        (Runtime.getRuntime().availableProcessors()).coerceAtLeast(2)
    )
}
