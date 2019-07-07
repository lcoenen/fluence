package fluence.kad.http

import fluence.codec.{CodecError, PureCodec}
import PureCodec.{liftFuncEither ⇒ liftFE}
import cats.syntax.compose._
import cats.syntax.arrow._
import cats.syntax.profunctor._
import cats.syntax.either._
import cats.syntax.functor._
import cats.instances.option._
import fluence.crypto.signature.PubKeyAndSignature
import fluence.kad.http.UriContact.{~~>, pkWithSignatureCodec}
import org.http4s.Uri

import scala.util.Try

object UriContactOps {

  /**
   * Read the contact, performing all the formal validations on the way. Note that signature is not checked
   */
  val readContact: String ~~> UriContact = {
    val readUri: String ~~> Uri =
      liftFE[String, Uri](Uri.fromString(_).leftMap(pf ⇒ CodecError("Cannot parse string as Uri", Some(pf))))

    val readHost: Uri ~~> String = (uri: Uri) ⇒
      Either.fromOption(uri.host, CodecError("Host not provided")).map(_.value)

    val readPort: Uri ~~> Short = (uri: Uri) ⇒
      Either
        .fromOption(uri.port, CodecError("Port not provided"))
        .flatMap(p ⇒ Try(p.toShort).toEither.left.map(t ⇒ CodecError(s"Port is not convertible to Short: $p", Some(t))))

    val checkScheme: Uri ~~> Unit =
      (uri: Uri) ⇒
        Either.fromOption(
          uri.scheme.filter(_.value.equalsIgnoreCase("fluence")).void,
          CodecError("Uri must start with fluence://")
      )

    // PubKey and Signature are encoded as base58 in userInfo part of URI
    val readPks: Uri ~~> PubKeyAndSignature = liftFE[Uri, (String, String)](
      uri ⇒
        Either.fromOption(uri.userInfo, CodecError("User info must be provided")).map(_.split(':')).flatMap {
          case Array(a, b) ⇒ Right((a, b))
          case _ ⇒ Left(CodecError("User info must be in pk:sign form"))
      }
    ) >>> pkWithSignatureCodec.direct

    // Finally, compose parsers and build the UriContact product
    readUri >>> (readHost &&& readPort &&& readPks &&& checkScheme).rmap {
      case (((host, port), pks), _) ⇒ UriContact(host, port, pks)
    }
  }
}
