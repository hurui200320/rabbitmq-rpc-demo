package info.skyblond.rabbitmq.rpc.demo

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RpcService(
    val exchange: String,
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class RpcMethod(
    val routingKey: String = ""
)