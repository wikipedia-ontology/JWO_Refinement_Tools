package data

class JWNSynset(val id: String, val synsetList: List[String], val enDescription: String, val jaDescription: String) {
  override def toString: String = {
    var msg = id + ": "
    for (s <- synsetList.take(1)) {
      msg += s + " "
    }
    return msg
  }
}