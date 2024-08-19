package info.skyblond.rabbitmq.rpc.demo

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Nullability


class RpcSymbolProcessor(
    environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    private val logger = environment.logger
    private val codeGenerator = environment.codeGenerator
    private val options = environment.options

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(RpcService::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .forEach { clazz ->
                val file = codeGenerator.createNewFile(
                    dependencies = Dependencies(
                        aggregating = false,
                        sources = arrayOf(clazz.containingFile!!)
                    ),
                    packageName = clazz.packageName.asString(),
                    fileName = clazz.simpleName.asString() + "Stub"
                )

                val pkg = clazz.packageName.asString()
                val className = clazz.simpleName.asString()
                val exchange = clazz.getAnnotationsByType(RpcService::class).first().exchange

                file.write("""
                    |package {{package}}
                    |
                    |class {{className}}Stub(
                    |    sender: reactor.rabbitmq.Sender
                    |): {{className}} {
                    |
                    |
                """.trimMargin()
                    .replace("{{package}}", pkg)
                    .replace("{{className}}", className)
                    .toByteArray())

                clazz.getAllFunctions()
                    .filter { f -> f.isAnnotationPresent(RpcMethod::class) }
                    .forEach { function ->
                        val functionName = function.simpleName.asString()
                        val routingKey = function.getAnnotationsByType(RpcMethod::class)
                            .first().routingKey.ifBlank { functionName }
                        require(function.parameters.size == 1) {"More than 1 parameter for function $functionName"}
                        val parameter = function.parameters.first()
                        val parameterType = parameter.type.resolve()
                        require(parameterType.nullability == Nullability.NOT_NULL) { "Parameter must not be null"}

                        file.write("    private val ${functionName}Client = sender.rpcClient(\"${exchange}\", \"${routingKey}\")\n\n".toByteArray())

                        file.write("""
                            |    fun ${functionName}Async(${parameter.name!!.asString()}: ${parameterType.declaration.qualifiedName!!.asString()}): reactor.core.publisher.Mono<${function.returnType!!.resolve().declaration.qualifiedName!!.asString()}> {
                            |        return ${functionName}Client.rpc(
                            |            reactor.core.publisher.Mono.just(
                            |                reactor.rabbitmq.RpcClient.RpcRequest(serialize(${parameter.name!!.asString()}))))
                            |            .map { deserialize<${function.returnType!!.resolve().declaration.qualifiedName!!.asString()}>(it.body.inputStream()) }
                            |    }
                            |    
                            |
                        """.trimMargin().toByteArray())

                        file.write("""
                            |    override fun ${functionName}(${parameter.name!!.asString()}: ${parameterType.declaration.qualifiedName!!.asString()}): ${function.returnType!!.resolve().declaration.qualifiedName!!.asString()} {
                            |        val latch = java.util.concurrent.CountDownLatch(1)
                            |        val result = java.util.concurrent.atomic.AtomicReference<${function.returnType!!.resolve().declaration.qualifiedName!!.asString()}>()
                            |        ${functionName}Async(${parameter.name!!.asString()})
                            |            .subscribe {
                            |                result.set(it)
                            |                latch.countDown()
                            |            }
                            |        latch.await()
                            |        return result.get()
                            |    }
                            |
                            |
                        """.trimMargin().toByteArray())
                    }

                file.write("}".toByteArray())
            }
        return emptyList()
    }
}

class RpcSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RpcSymbolProcessor(environment)
    }

}