package info.skyblond.rabbitmq.rpc.demo

@RpcService(exchange = "info.skyblond.rabbitmq.rpc.demo")
interface DemoRpc {

    @RpcMethod
    fun foo(req: DemoFooRequest): Int

    @RpcMethod
    fun bar(i: Int): String
}

//class DemoRpcStub2(
//    sender: reactor.rabbitmq.Sender
//): DemoRpc {
//
//    private val fooClient = sender.rpcClient("exchange", "key")
//
//    override fun foo(req: DemoFooRequest): Int {
//        val latch = java.util.concurrent.CountDownLatch(1)
//        val result = java.util.concurrent.atomic.AtomicReference<Int>()
//        fooAsync(req)
//            .subscribe {
//                result.set(it)
//                latch.countDown()
//            }
//        latch.await()
//        return result.get()
//    }
//
//    fun fooAsync(req: DemoFooRequest): reactor.core.publisher.Mono<Int> {
//        return fooClient.rpc(
//            reactor.core.publisher.Mono.just(
//                reactor.rabbitmq.RpcClient.RpcRequest(serialize(req))))
//            .map { deserialize<Int>(it.body.inputStream()) }
//    }
//}