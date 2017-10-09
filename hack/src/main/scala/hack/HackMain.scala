package hack

import net.sf.ntru.encrypt.{EncryptionKeyPair, EncryptionParameters, EncryptionPrivateKey, EncryptionPublicKey}
import net.sf.ntru.polynomial.IntegerPolynomial
import nics.crypto.ntrureencrypt.NTRUReEncrypt
import java.util.Base64

import net.sf.ntru.util.ArrayEncoder


object HackMain extends App {

  val ep = EncryptionParameters.EES1171EP1_FAST

  val ntruReEnc = new NTRUReEncrypt(ep)

  def b64enc(bytes: Array[Byte]) = Base64.getEncoder.encodeToString(bytes)

  def b64dec(str: String): Array[Byte] = Base64.getDecoder.decode(str)

  def encrypt(pub: EncryptionPublicKey, message: String): String = {
    polyString(encryptPoly(pub, message))
  }

  def encryptPoly(pub: EncryptionPublicKey, message: String): IntegerPolynomial = {
    val bytes = message.getBytes("UTF-8")
    val msg = bytes.length % 3 match {
      case 0 => bytes
      case 1 => Array.concat(bytes, Array(' '.toByte))
      case 2 => Array.concat(bytes, Array(' '.toByte, ' '.toByte))
    }
    val m = IntegerPolynomial.fromBinary3Sves(msg, ep.N, true)

    ntruReEnc.encrypt(pub, m)
  }

  def polyString(poly: IntegerPolynomial): String = {
    poly.coeffs.length + ":" + b64enc(poly.toBinary(4096))
  }

  def decrypt(priv: EncryptionPrivateKey, message: String): String = {
    val d = decryptPoly(priv, message)

    new String(ArrayEncoder.encodeMod3Sves(d.coeffs, true), "UTF-8").filterNot(_ == ' ')
  }

  def decryptPoly(priv: EncryptionPrivateKey, message: String): IntegerPolynomial = {
    val (coeffs, data) = message.splitAt(message.indexOf(':'))

    ntruReEnc.decrypt(priv, IntegerPolynomial.fromBinary(b64dec(data.drop(1)), coeffs.toInt, 4096))
  }

  val kpA = new EncryptionKeyPair(
    new EncryptionPrivateKey(
      b64dec("BJMIAAcACAAIxgnQjA61qnV1UZAkcIM+whQ0t7HOjwAHAAcdIAiHuIQv6/kPNbDFaApEIX9RDQALAAtKsAmFZtQnYEHLa2eDHxYBCLCCHb4RnBI9SWhJTOMeAQ==")
    ),
    new EncryptionPublicKey(
      b64dec("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")
    ))

  val kpB = new EncryptionKeyPair(
    new EncryptionPrivateKey(
      b64dec("BJMIAAcACAAIOxAVtlbnuvtFMI51kARRFuOqWmUrdwAHAAcnmMnKqua40hkRDeDBIuJBJ+bFDwALAAsZYYlVMLMeKx1qW8sTIRUBeQjFNyZCEvMA6VS+wtnvAA==")
    ),
    new EncryptionPublicKey(
      b64dec("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")
    ))

  def test2(): Unit = {

    val message = "some crazy message"

    val enc = encrypt(kpA.getPublic, message)
    val dec = decrypt(kpA.getPrivate, enc)

    assert(dec == message, "Messages should match after enc/dec")

    val rk = ntruReEnc.generateReEncryptionKey(kpA.getPrivate, kpB.getPrivate)
    val cB = ntruReEnc.reEncrypt(rk, encryptPoly(kpA.getPublic, message))

    val dec2 = decrypt(kpB.getPrivate, polyString(cB))

    assert(dec2 == message, "Reencrypted messages should match")

    println("dec2 = " + dec2)
  }

  test2()
}
