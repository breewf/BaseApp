package com.hy.basic.vehicle.protocol.dbc

import com.hy.basic.vehicle.protocol.can.CanByteOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** DBC 解析回归测试：字段、扩展帧、写入解码和坏行。 */
class DbcParserTest {

    /** 测试用 DBC 文本 */
    private val sample = """
        VERSION ""
        NS_ :
        BU_: ECU

        BO_ 256 Powertrain: 8 ECU
         SG_ Speed : 0|16@1+ (0.1,0) [0|250] "km/h" ECU
         SG_ Gear : 16|4@1+ (1,0) [0|15] "" ECU
         SG_ Torque : 39|12@0- (0.5,0) [-500|500] "Nm" ECU

        BO_ 2147483904 ExtFrame: 8 ECU
         SG_ Temp : 24|8@1- (1,-40) [-40|150] "C" ECU

        VAL_ 256 Gear 0 "P" 1 "R" 2 "N" 3 "D" ;
        CM_ SG_ 256 Speed "vehicle speed";
        CM_ BO_ 256 "powertrain";
        BA_ "GenMsgCycleTime" BO_ 256 20;
        BA_ "GenSigStartValue" SG_ 256 Speed 0;
    """.trimIndent()

    /** 解析报文、信号、注释和枚举 */
    @Test
    fun `解析报文和信号字段`() {
        val db = DbcParser.parse(sample)
        assertEquals(emptyList<String>(), db.issues)

        val message = db.message(256)
        assertTrue(message != null)
        assertEquals("Powertrain", message!!.name)
        assertEquals(8, message.dlc)
        assertEquals(20, message.cycleTimeMs)
        assertEquals("powertrain", message.comment)

        val speed = message.signal("Speed")!!
        assertEquals(CanByteOrder.INTEL, speed.spec.byteOrder)
        assertEquals(0.1, speed.spec.factor, 0.0001)
        assertEquals("km/h", speed.spec.unit)
        assertEquals("vehicle speed", speed.comment)

        val gear = message.signal("Gear")!!
        assertEquals("D", gear.spec.valueLabels[3L])

        val torque = message.signal("Torque")!!
        assertEquals(CanByteOrder.MOTOROLA, torque.spec.byteOrder)
        assertTrue(torque.spec.signed)
    }

    /** 扩展帧 ID 拆分 */
    @Test
    fun `扩展帧标志位会拆出来`() {
        val db = DbcParser.parse(sample)

        val extended = db.message(256, extended = true)
        assertTrue(extended != null)
        assertEquals("ExtFrame", extended!!.name)
        assertTrue(extended.extended)
    }

    /** 写入后按 DBC 解回 */
    @Test
    fun `信号写入后能按 DBC 解回`() {
        val db = DbcParser.parse(sample)
        val message = db.message(256)!!

        var frame = message.frame(ByteArray(8))
        frame = frame.write(message.signal("Speed")!!.spec, 60.5)
        frame = frame.write(message.signal("Gear")!!.spec, 3.0)
        frame = frame.write(message.signal("Torque")!!.spec, -120.0)

        val values = message.decode(frame).associateBy { it.name }
        assertEquals(60.5, values["Speed"]!!.physical, 0.0001)
        assertEquals("D", values["Gear"]!!.label)
        assertEquals(-120.0, values["Torque"]!!.physical, 0.0001)
    }

    /** 坏行记录 issue，不中断解析 */
    @Test
    fun `坏行只记录问题不中断解析`() {
        val db = DbcParser.parse("BO_ not_a_number Bad: 8 ECU")

        assertTrue(db.messages.isEmpty())
        assertEquals(1, db.issues.size)
    }
}
