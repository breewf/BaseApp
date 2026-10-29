package com.hy.basic.vehicle.protocol.uds

import com.hy.basic.vehicle.protocol.toHexString
import java.util.Locale
import kotlinx.coroutines.CancellationException

/**
 * desc: UDS(ISO 14229)请求/响应封装。
 *
 * 只做服务层:发 SID + 数据、收完整响应、拆肯定/否定/超时。
 * ISO-TP 分包重组、CAN 发送、0x78 响应挂起重试由 [UdsTransport] 实现方处理。
 *
 * 注意:
 * - 0x78(ResponsePending)表示 ECU 还在处理,传输层要继续等,不要当失败;
 * - 安全访问的 seed/key 算法各车厂不同,通过 [UdsClient.securityAccess] 回调传入;
 * - 这是协议层,不依赖 CarService / VHAL,台架和单测里可以换假传输。
 */

/** UDS 常用服务号 */
object UdsService {
    const val DIAGNOSTIC_SESSION_CONTROL = 0x10
    const val ECU_RESET = 0x11
    const val READ_DATA_BY_IDENTIFIER = 0x22
    const val SECURITY_ACCESS = 0x27
    const val WRITE_DATA_BY_IDENTIFIER = 0x2E
    const val READ_DTC_INFORMATION = 0x19
    const val ROUTINE_CONTROL = 0x31
    const val TESTER_PRESENT = 0x3E
}

/** 常见否定响应码 */
object UdsNrc {
    const val SERVICE_NOT_SUPPORTED = 0x11
    const val SUB_FUNCTION_NOT_SUPPORTED = 0x12
    const val INCORRECT_MESSAGE_LENGTH = 0x13
    const val CONDITIONS_NOT_CORRECT = 0x22
    const val REQUEST_SEQUENCE_ERROR = 0x24
    const val REQUEST_OUT_OF_RANGE = 0x31
    const val SECURITY_ACCESS_DENIED = 0x33
    const val INVALID_KEY = 0x35
    const val RESPONSE_PENDING = 0x78
}

/** DTC 状态位 */
object UdsDtcStatus {
    const val TEST_FAILED = 0x01
    const val TEST_FAILED_THIS_CYCLE = 0x02
    const val PENDING = 0x04
    const val CONFIRMED = 0x08
    const val WARNING_INDICATOR = 0x80
}

/**
 * UDS 传输层。
 *
 * 实现方负责把请求发出去、收完整响应(含 ISO-TP 多帧),失败/超时返回 null。
 */
interface UdsTransport {

    /** 发送一包 UDS 请求,返回完整响应;超时或通道错误返回 null */
    suspend fun request(payload: ByteArray, timeoutMs: Long): ByteArray?
}

/** UDS 调用结果 */
sealed class UdsResult {

    /** 肯定响应,[response] 含 0x40+SID */
    data class Success(val response: ByteArray) : UdsResult() {
        override fun equals(other: Any?): Boolean =
            other is Success && response.contentEquals(other.response)

        override fun hashCode(): Int = response.contentHashCode()
    }

    /** 否定响应 */
    data class Negative(val serviceId: Int, val nrc: Int) : UdsResult()

    /** 超时 */
    data class Timeout(val serviceId: Int) : UdsResult()

    /** 传输层异常或响应格式不对 */
    data class TransportError(val serviceId: Int, val message: String?) : UdsResult()

    /** 是否肯定响应 */
    val isSuccess: Boolean get() = this is Success
}

/**
 * UDS 客户端。
 *
 * 默认超时 1 秒,长例程(刷写、自检)调用时单独传更大的 timeout。
 */
class UdsClient(
    private val transport: UdsTransport,
    private val defaultTimeoutMs: Long = 1_000L,
) {

    /** 通用请求:自动拼 SID,拆否定响应和 SID 不匹配 */
    suspend fun request(
        serviceId: Int,
        data: ByteArray = ByteArray(0),
        timeoutMs: Long = defaultTimeoutMs,
    ): UdsResult {
        val payload = ByteArray(data.size + 1) { index ->
            if (index == 0) serviceId.toByte() else data[index - 1]
        }

        val response = try {
            transport.request(payload, timeoutMs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return UdsResult.TransportError(serviceId, e.message)
        } ?: return UdsResult.Timeout(serviceId)

        if (response.size >= 3 && (response[0].toInt() and 0xFF) == 0x7F) {
            return UdsResult.Negative(
                serviceId = response[1].toInt() and 0xFF,
                nrc = response[2].toInt() and 0xFF,
            )
        }

        val expectedSid = (serviceId + 0x40) and 0xFF
        if (response.isEmpty() || (response[0].toInt() and 0xFF) != expectedSid) {
            return UdsResult.TransportError(
                serviceId = serviceId,
                message = "响应 SID 不匹配: ${response.toHexString()}",
            )
        }
        return UdsResult.Success(response)
    }

    /** 10 服务:切换诊断会话(01 默认 / 02 编程 / 03 扩展) */
    suspend fun sessionControl(
        sessionType: Int,
        timeoutMs: Long = defaultTimeoutMs,
    ): UdsResult = request(
        serviceId = UdsService.DIAGNOSTIC_SESSION_CONTROL,
        data = byteArrayOf(sessionType.toByte()),
        timeoutMs = timeoutMs,
    )

    /** 22 服务:按 DID 读数据 */
    suspend fun readDataByIdentifier(
        did: Int,
        timeoutMs: Long = defaultTimeoutMs,
    ): UdsResult = request(
        serviceId = UdsService.READ_DATA_BY_IDENTIFIER,
        data = byteArrayOf((did ushr 8).toByte(), did.toByte()),
        timeoutMs = timeoutMs,
    )

    /** 2E 服务:按 DID 写数据 */
    suspend fun writeDataByIdentifier(
        did: Int,
        data: ByteArray,
        timeoutMs: Long = defaultTimeoutMs,
    ): UdsResult = request(
        serviceId = UdsService.WRITE_DATA_BY_IDENTIFIER,
        data = byteArrayOf((did ushr 8).toByte(), did.toByte()) + data,
        timeoutMs = timeoutMs,
    )

    /** 3E 服务:保持会话。0x80 表示不需要肯定响应 */
    suspend fun testerPresent(
        timeoutMs: Long = defaultTimeoutMs,
    ): UdsResult = request(
        serviceId = UdsService.TESTER_PRESENT,
        data = byteArrayOf(0x80.toByte()),
        timeoutMs = timeoutMs,
    )

    /** 19 02 服务:按状态掩码读 DTC,结果用 [parseDtcByStatusMask] 解析 */
    suspend fun readDtcByStatusMask(
        statusMask: Int = 0xFF,
        timeoutMs: Long = defaultTimeoutMs,
    ): UdsResult = request(
        serviceId = UdsService.READ_DTC_INFORMATION,
        data = byteArrayOf(0x02, statusMask.toByte()),
        timeoutMs = timeoutMs,
    )

    /**
     * 27 服务:安全访问。
     *
     * [level] 是请求种子的子功能,通常是奇数;拿到 seed 后交给 [keyProvider] 算 key,
     * 再自动发 level+1 的送 key 请求。keyProvider 放最后,方便用尾随 lambda 传算法。
     */
    suspend fun securityAccess(
        level: Int,
        timeoutMs: Long = defaultTimeoutMs,
        keyProvider: suspend (ByteArray) -> ByteArray,
    ): UdsResult {
        require(level in 1..0x7F && level % 2 == 1) {
            "安全访问的 seed 子功能应为奇数 1..0x7F: $level"
        }

        val seedResult = request(
            serviceId = UdsService.SECURITY_ACCESS,
            data = byteArrayOf(level.toByte()),
            timeoutMs = timeoutMs,
        )
        if (seedResult !is UdsResult.Success) return seedResult
        if (seedResult.response.size < 2) {
            return UdsResult.TransportError(
                serviceId = UdsService.SECURITY_ACCESS,
                message = "seed 响应过短: ${seedResult.response.toHexString()}",
            )
        }

        val seed = seedResult.response.copyOfRange(2, seedResult.response.size)
        val key = keyProvider(seed)
        return request(
            serviceId = UdsService.SECURITY_ACCESS,
            data = byteArrayOf((level + 1).toByte()) + key,
            timeoutMs = timeoutMs,
        )
    }
}

/** 一条 DTC */
data class UdsDtc(
    /** 3 字节 DTC 码 */
    val code: Int,
    /** 状态字节 */
    val status: Int,
) {

    /** 转成 P/C/B/U 格式,例如 P0301 */
    fun codeText(): String {
        val first = (code ushr 16) and 0xFF
        val letter = when ((first ushr 6) and 0x03) {
            0 -> 'P'
            1 -> 'C'
            2 -> 'B'
            else -> 'U'
        }
        val firstDigit = (first ushr 4) and 0x03
        val rest = ((first and 0x0F) shl 8) or ((code ushr 8) and 0xFF)
        return String.format(Locale.US, "%c%d%03X", letter, firstDigit, rest)
    }
}

/**
 * 解析 19 02 的肯定响应。
 *
 * 响应格式:[0x59, 0x02, 可用性掩码, DTC1(3) 状态1, DTC2(3) 状态2, ...]
 */
fun parseDtcByStatusMask(response: ByteArray): List<UdsDtc> {
    if (response.size < 3 || (response[0].toInt() and 0xFF) != 0x59) return emptyList()

    val result = mutableListOf<UdsDtc>()
    var index = 3
    while (index + 4 <= response.size) {
        val code = ((response[index].toInt() and 0xFF) shl 16) or
            ((response[index + 1].toInt() and 0xFF) shl 8) or
            (response[index + 2].toInt() and 0xFF)
        val status = response[index + 3].toInt() and 0xFF
        result += UdsDtc(code, status)
        index += 4
    }
    return result
}
