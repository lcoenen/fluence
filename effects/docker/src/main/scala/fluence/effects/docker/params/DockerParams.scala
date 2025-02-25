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

package fluence.effects.docker.params

import fluence.effects.docker.DockerIO

import scala.collection.immutable.Queue
import scala.sys.process._

/**
 * Builder for basic `docker run` command parameters.
 *
 * @param params Current command' params
 */
case class DockerParams private (params: Queue[String], name: Option[String] = None) {

  /**
   * Adds a single param to command.
   *
   * @param param option
   */
  def add(param: String): DockerParams =
    copy(params.enqueue(param))

  /**
   * Adds a named option to command.
   *
   * @param optionName option name
   * @param optionValue option value
   */
  def option(optionName: String, optionValue: String): DockerParams =
    add(optionName).add(optionValue)

  def option(optionName: String, optionValue: Option[String]): DockerParams =
    optionValue.fold(this)(v => add(optionName).add(v))

  /**
   * Set the container name
   *
   * @param containerName Container name
   */
  def name(containerName: String): DockerParams =
    option("--name", containerName).copy(name = Some(containerName))

  /**
   * Adds a port mapping.
   *
   * @param hostPort port number on host
   * @param containerPort mapped port number in container
   */
  def port(hostPort: Short, containerPort: Short): DockerParams =
    option("-p", s"$hostPort:$containerPort")

  /**
   * Adds a volume mapping.
   *
   * @param hostVolume volume directory on host
   * @param containerVolume mounted volume location in container
   */
  def volume(hostVolume: String, containerVolume: String): DockerParams =
    option("-v", s"$hostVolume:$containerVolume")

  /**
   * Specifies a user on whose behalf commands will be run
   *
   * @param user user login or uid
   */
  def user(user: String): DockerParams =
    option("--user", user)

  /**
   * Specifies a hard limit on maximum amount of cpu that can be utilized by a container
   *
   * @param limit Fraction specifying number of cores. E.g., 0.5 to limit usage to a half of a core.
   */
  def cpus(limit: Double): DockerParams =
    option("--cpus", limit.toString)

  /**
   * Specifies a hard limit on maximum amount of memory available to a container
   *
   * @param limitMb Amount of memory in megabytes
   */
  def memory(limitMb: Int): DockerParams =
    option("--memory", s"${limitMb}M")

  /**
   * Guarantees to allocate at lest this much memory to a container
   *
   * @param megabytes Amount of memory in megabytes
   * @return
   */
  def memoryReservation(megabytes: Int): DockerParams =
    option("--memory-reservation", s"${megabytes}M")

  /**
   * Sets CPU and memory limits on a docker container
   */
  def limits(limits: DockerLimits): DockerParams = {
    // TODO: rewrite this with State monad
    type Mut = DockerParams => DockerParams
    type MutOpt = Option[Mut]

    val withCpus: MutOpt = limits.cpus.map(limit => _.cpus(limit))
    val withMemory: MutOpt = limits.memoryMb.map(limit => _.memory(limit))
    val withMemoryReservation: MutOpt = limits.memoryMb.map(limit => _.memoryReservation(limit))
    Seq(withCpus, withMemory, withMemoryReservation).flatten.foldLeft(this) { case (dp, f) => f(dp) }
  }

  /**
   * Builds the current command to a representation ready to pass in [[scala.sys.process.Process]].
   *
   * @param image Container image
   */
  def prepared(image: DockerImage): DockerParams.Prepared =
    DockerParams.Prepared(params, image, name)
}

object DockerParams {
  // Represents finalized docker command that's ready to be ran and can't be changed anymore
  sealed trait SealedParams {
    def command: Seq[String]
    def process: ProcessBuilder = Process(command)
  }

  // Represents a command for daemonized container run, i.e., anything with "docker run -d"
  case class DaemonParams(command: Seq[String], name: Option[String]) extends SealedParams

  // Represents a command for a single command execution, presumably with `--rm` flag
  case class ExecParams(command: Seq[String]) extends SealedParams

  private val daemonParams = Seq("docker", "run", "-d")
  private val runParams = Seq("docker", "run", "--user", "", "--rm", "-i")

  // Represents a docker run command with specified image name, ready to be specialized to Daemon or Exec params
  case class Prepared(params: Seq[String], image: DockerImage, name: Option[String]) {

    /**
     * Builds a command starting with `docker run -d` wrapped in DaemonParams, so
     * container will be deleted automatically by [[DockerIO.run]]
     *
     * @param cmd Command to run inside the container. If not present, the default command will be executed
     */
    def daemonRun(cmd: String = null): DaemonParams =
      DaemonParams(Option(cmd).foldLeft(daemonParams ++ params :+ image.imageName)(_ :+ _), name)

    /**
     * Builds a `docker run` command running custom executable.
     *
     * `--rm` flag is specified, so container will be removed automatically after executable exit
     * Resulting command will be like the following
     * `docker run --user "" --rm -i --entrypoint executable imageName execParams`
     *
     * @param entrypoint An executable to be run in container, must be callable by container (i.e. be in $PATH)
     * @param execParams Parameters passed to `executable`
     */
    def run(entrypoint: String, execParams: String*): ExecParams =
      ExecParams(
        (runParams :+ "--entrypoint" :+ entrypoint) ++ params ++ (image.imageName +: execParams)
      )

    /**
     * Builds a `docker run` command running custom executable.
     *
     * `--rm` flag is specified, so container will be removed automatically after executable exit
     * Resulting command will be like the following
     * `docker run --user "" --rm -i imageName execParams`
     *
     * @param execParams Parameters passed to `executable`
     */
    def runExec(execParams: String*): ExecParams =
      ExecParams(
        runParams ++ params ++ (image.imageName +: execParams)
      )
  }

  // Builds an empty docker command, ready for adding options
  def build(): DockerParams = DockerParams(Queue())
}
