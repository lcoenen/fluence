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

package fluence.statemachine.control

import cats.effect.Concurrent
import cats.{Applicative, CommutativeApplicative, Monad, Order, Parallel, Traverse, UnorderedTraverse}
import cats.effect.concurrent.{Deferred, Ref, Semaphore}
import cats.syntax.functor._
import cats.instances.list._
import cats.syntax.flatMap._

import scala.language.higherKinds
import scala.collection.immutable
import scala.collection.immutable.TreeMap

class DeferredMap[F[_]: Concurrent, K, A: HasKey[?, K]](
  ref: Ref[F, immutable.Map[K, Deferred[F, Option[A]]]]
)(
  implicit F: Monad[F]
) {
  import HasKey.syntax._

  def put(value: A): F[Unit] = getOrPutPromise(value.key).map(_.complete(Some(value)))

  def get(key: K): F[Option[A]] = getOrPutPromise(key).flatMap(_.get)

  def delete(p: K => Boolean) =
    ref
      .modify(_.partition(t => !p(t._1)))
      .flatMap(m => Traverse[List].traverse(m.values.toList)(_.complete(None)))

  private def getOrPutPromise(key: K, dd: Deferred[F, Option[A]] = null): F[Deferred[F, Option[A]]] = {
    ref.access.flatMap {
      case (map, setter) =>
        if (map.contains(key)) F.pure(map(key))
        else
          for {
            promise <- if (dd == null) Deferred[F, Option[A]] else F.pure(dd)
            success <- setter(map.updated(key, promise))
            // ref was modified, try again
            _ <- ifF(!success) { getOrPutPromise(key, promise) }
          } yield promise
    }
  }

  private def ifF(cond: Boolean)(thunk: => F[_]) =
    if (cond) thunk.void
    else F.unit
}

trait HasKey[A, K] {
  def key(a: A): K
}

object HasKey {
val o: Order[Any] = _
  object syntax {
    implicit class HasKeyOps[K, A](a: A)(implicit HK: HasKey[A, K]) {
      def key = HK.key(a)
    }
  }

  def apply[K, A: HasKey[?, K]] = implicitly[HasKey[A, K]]

  implicit def hasHeight[A: HasHeight] = new HasKey[A, Long] {
    override def key(a: A): Long = HasHeight[A].height(a)
  }
}

class SomeMap[F[_], K: Order, V](
  askSlot: Semaphore[F],
  map: Ref[F, TreeMap[K, V]]
                                ){
  // k >= min current key
  // there should be no more asks
  // once returned, all k < this are removed
  // otherwise should be idempotent
  def ask(k: K): F[V] = ???

  // k > max current key,
  // if there's pending ask, k <= min ask k
  // idempotent -- can put the same value twice
  def put(k: K, v: V): F[Unit] = ???
}