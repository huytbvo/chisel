package Chisel

import Component._

class TransactionalBundle extends Bundle {
  
  val req = new PipeIO()(Bits()).flip()
  val resp = Bits(OUTPUT)
  
}

abstract class TransactionalComponent extends Component {
  val io: TransactionalBundle
  tcomponents += this

  var acceptBackPressure = true
  val req_ready: Bool = Bool(false)
  val resp_ready: Bool = Bool(false)
  val resp_valid: Bool = Bool(false)
  

}
