/*
 * Copyright 2018 Fluence Labs Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fluence

import java.nio.ByteBuffer

import cats.arrow.FunctionK
import cats.{~>, Monad}
import cats.data.EitherT
import cats.effect.ConcurrentEffect
import cats.implicits._
import com.softwaremill.sttp.{MonadError => ME, _}

import scala.language.{higherKinds, implicitConversions}
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import com.softwaremill.sttp.impl.cats.implicits._

/**
 * Async sttp backend that will return EitherT.
 */
object EitherTSttpBackend {

  def apply[F[_]: ConcurrentEffect](): SttpBackend[EitherT[F, Throwable, ?], fs2.Stream[F, ByteBuffer]] = {
    val sttp: SttpBackend[F, fs2.Stream[F, ByteBuffer]] = AsyncHttpClientFs2Backend[F]()

    val eitherTArrow: F ~> EitherT[F, Throwable, ?] = new FunctionK[F, EitherT[F, Throwable, ?]] {
      override def apply[A](fa: F[A]): EitherT[F, Throwable, A] = {
        EitherT(fa.attempt)
      }
    }

    val eitherTSttp: SttpBackend[EitherT[F, Throwable, ?], fs2.Stream[F, ByteBuffer]] =
      sttp.mapK(eitherTArrow)

    eitherTSttp
  }
}

/**
 * sttp MonadError for EitherT
 */
class EitherTMonad[F[_]](implicit F: Monad[F]) extends ME[EitherT[F, Throwable, ?]] {
  type R[T] = EitherT[F, Throwable, T]

  override def unit[T](t: T): R[T] =
    EitherT.right[Throwable](F.pure(t))

  override def map[T, T2](fa: R[T])(f: T => T2): R[T2] =
    fa.map(f)

  override def flatMap[T, T2](fa: R[T])(f: T => R[T2]): R[T2] =
    fa.flatMap(f)

  override def error[T](t: Throwable): R[T] =
    EitherT.left[T](F.pure(t))

  override protected def handleWrappedError[T](rt: R[T])(h: PartialFunction[Throwable, R[T]]): R[T] =
    rt.handleErrorWith(h)
}
