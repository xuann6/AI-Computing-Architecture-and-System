package aias_lab8.Single_Cycle.Controller

import chisel3._
import chisel3.util._


object opcode_map {
    val LOAD      = "b0000011".U
    val STORE     = "b0100011".U
    val BRANCH    = "b1100011".U
    val JALR      = "b1100111".U
    val JAL       = "b1101111".U
    val OP_IMM    = "b0010011".U
    val OP        = "b0110011".U
    val AUIPC     = "b0010111".U
    val LUI       = "b0110111".U
    val HCF       = "b0001011".U

    // Edit for Vector Extension
    val VADD_VV   = "b1010111".U
}

object ALU_op{
  val ADD  = 0.U
  val SLL  = 1.U
  val SLT  = 2.U
  val SLTU = 3.U
  val XOR  = 4.U
  val SRL  = 5.U
  val OR   = 6.U
  val AND  = 7.U
  val SUB  = 8.U
  val SRA  = 13.U
}

//**************************************************
//**                                              **
//**                                              **
//**      Modification of Vector Extension        **
//**                                              **
//**                                              **
//**************************************************
object vector_ALU_op{
    val VADD_VV = 0.U
}

object condition{
  val EQ = "b000".U
  val NE = "b001".U
  val LT = "b100".U
  val GE = "b101".U
  val LTU = "b110".U
  val GEU = "b111".U
}

import opcode_map._,condition._,ALU_op._,vector_ALU_op._

class Controller extends Module {
    val io = IO(new Bundle{
        val Inst = Input(UInt(32.W))
        val BrEq = Input(Bool())
        val BrLT = Input(Bool())

        val PCSel = Output(Bool())
        val ImmSel = Output(UInt(3.W))
        val RegWEn = Output(Bool())
        val BrUn = Output(Bool())
        val BSel = Output(Bool())
        val ASel = Output(Bool())
        val ALUSel = Output(UInt(4.W))
        val MemRW = Output(Bool())
        val WBSel = Output(UInt(2.W))

        //new
        val Lui = Output(Bool())
        val Hcf = Output(Bool())

        // Edit for Vector Extension
        val vector_ALUSel = Output(UInt(4.W))
        val vector_ASel = Output(Bool())
        val vector_BSel = Output(Bool())
        val vector_WBSel = Output(UInt(2.W))
        val vector_RegWEn = Output(Bool())
    })
    
    val opcode = Wire(UInt(7.W))
    opcode := io.Inst(6,0)

    val funct3 = Wire(UInt(3.W))
    funct3 := io.Inst(14,12)

    val funct7 = Wire(UInt(7.W))
    funct7 := io.Inst(31,25)

    // Edit for Vector Extension
    val funct6 = Wire(UInt(6.W))
    funct6 := io.Inst(31,26)

    //Control signal
    io.RegWEn := false.B
    io.ASel := false.B
    io.BSel := false.B
    io.BrUn := false.B
    io.MemRW := false.B
    io.ImmSel := 0.U
    io.ALUSel := 0.U
    io.PCSel := false.B
    io.WBSel := 0.U(2.W)
    io.Lui := false.B
    io.Hcf := opcode === HCF

    //**************************************************
    //**                                              **
    //**                                              **
    //**      Modification of Vector Extension        **
    //**                                              **
    //**                                              **
    //**************************************************
    io.vector_ALUSel := 0.U
    io.vector_ASel   := false.B
    io.vector_BSel   := false.B
    io.vector_RegWEn := true.B
    io.vector_WBSel  := 0.U(2.W)
}