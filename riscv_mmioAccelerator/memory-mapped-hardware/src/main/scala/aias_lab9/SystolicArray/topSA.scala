package aias_lab9.SystolicArray

import chisel3._
import chisel3.util._

import aias_lab9.AXILite._

/** topSA module
  *
  * includes one Memory_Mapped module and one SA module
  *
  * pure wiring between these two modules and I/O interface
  */
class topSA(addr_width: Int, data_width: Int, reg_width: Int) extends Module {
  val io = IO(new Bundle {
    // slave interface for connecting to AXI bus
    val slave = new AXILiteSlaveIF(addr_width, data_width)
  })

  // module declaration
  val sa = Module(new SA(4, 4, addr_width, data_width, reg_width))
  val mm = Module(new Memory_Mapped(0x8000, addr_width, data_width, reg_width))

  // module wiring
  io.slave <> mm.io.slave
  mm.io.mmio <> sa.io.mmio

  // for internal buffer (local mem) >>>>>
  mm.io.raddr <> sa.io.raddr
  mm.io.rdata <> sa.io.rdata
  mm.io.waddr <> sa.io.waddr
  mm.io.wdata <> sa.io.wdata
  mm.io.wstrb <> sa.io.wstrb
  mm.io.wen <> sa.io.wen
}
