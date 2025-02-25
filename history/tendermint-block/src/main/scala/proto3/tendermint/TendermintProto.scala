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

// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package proto3.tendermint

object TendermintProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    com.google.protobuf.timestamp.TimestampProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq(
    proto3.tendermint.PartSetHeader,
    proto3.tendermint.BlockID,
    proto3.tendermint.Vote,
    proto3.tendermint.Version,
    proto3.tendermint.Header,
    proto3.tendermint.Data,
    proto3.tendermint.Block,
    proto3.tendermint.Commit,
    proto3.tendermint.EvidenceData,
    proto3.tendermint.Evidence,
    proto3.tendermint.Validator,
    proto3.tendermint.CanonicalVote,
    proto3.tendermint.CanonicalBlockID,
    proto3.tendermint.CanonicalPartSetHeader
  )
  private lazy val ProtoBytes: Array[Byte] =
    scalapb.Encoding.fromBase64(
      scala.collection
        .Seq(
          """ChB0ZW5kZXJtaW50LnByb3RvEgZwcm90bzMaH2dvb2dsZS9wcm90b2J1Zi90aW1lc3RhbXAucHJvdG8iOQoNUGFydFNldEhlY
  WRlchIUCgV0b3RhbBgBIAEoBVIFdG90YWwSEgoEaGFzaBgCIAEoDFIEaGFzaCJKCgdCbG9ja0lEEhIKBGhhc2gYASABKAxSBGhhc
  2gSKwoFcGFydHMYAiABKAsyFS5wcm90bzMuUGFydFNldEhlYWRlclIFcGFydHMipQIKBFZvdGUSEgoEdHlwZRgBIAEoBVIEdHlwZ
  RIWCgZoZWlnaHQYAiABKANSBmhlaWdodBIUCgVyb3VuZBgDIAEoBVIFcm91bmQSKwoIYmxvY2tfaWQYBCABKAsyDy5wcm90bzMuQ
  mxvY2tJRFIIYmxvY2tfaWQSOAoJdGltZXN0YW1wGAUgASgLMhouZ29vZ2xlLnByb3RvYnVmLlRpbWVzdGFtcFIJdGltZXN0YW1wE
  iwKEXZhbGlkYXRvcl9hZGRyZXNzGAYgASgMUhF2YWxpZGF0b3JfYWRkcmVzcxIoCg92YWxpZGF0b3JfaW5kZXgYByABKAVSD3Zhb
  GlkYXRvcl9pbmRleBIcCglzaWduYXR1cmUYCCABKAxSCXNpZ25hdHVyZSIxCgdWZXJzaW9uEhQKBWJsb2NrGAEgASgEUgVibG9ja
  xIQCgNhcHAYAiABKARSA2FwcCLhBAoGSGVhZGVyEikKB3ZlcnNpb24YASABKAsyDy5wcm90bzMuVmVyc2lvblIHdmVyc2lvbhIZC
  ghjaGFpbl9pZBgCIAEoCVIHY2hhaW5JZBIWCgZoZWlnaHQYAyABKANSBmhlaWdodBIuCgR0aW1lGAQgASgLMhouZ29vZ2xlLnByb
  3RvYnVmLlRpbWVzdGFtcFIEdGltZRIXCgdudW1fdHhzGAUgASgDUgZudW1UeHMSGwoJdG90YWxfdHhzGAYgASgDUgh0b3RhbFR4c
  xIzCg1sYXN0X2Jsb2NrX2lkGAcgASgLMg8ucHJvdG8zLkJsb2NrSURSC2xhc3RCbG9ja0lkEigKEGxhc3RfY29tbWl0X2hhc2gYC
  CABKAxSDmxhc3RDb21taXRIYXNoEhsKCWRhdGFfaGFzaBgJIAEoDFIIZGF0YUhhc2gSJwoPdmFsaWRhdG9yc19oYXNoGAogASgMU
  g52YWxpZGF0b3JzSGFzaBIwChRuZXh0X3ZhbGlkYXRvcnNfaGFzaBgLIAEoDFISbmV4dFZhbGlkYXRvcnNIYXNoEiUKDmNvbnNlb
  nN1c19oYXNoGAwgASgMUg1jb25zZW5zdXNIYXNoEhkKCGFwcF9oYXNoGA0gASgMUgdhcHBIYXNoEioKEWxhc3RfcmVzdWx0c19oY
  XNoGA4gASgMUg9sYXN0UmVzdWx0c0hhc2gSIwoNZXZpZGVuY2VfaGFzaBgPIAEoDFIMZXZpZGVuY2VIYXNoEikKEHByb3Bvc2VyX
  2FkZHJlc3MYECABKAxSD3Byb3Bvc2VyQWRkcmVzcyIYCgREYXRhEhAKA3R4cxgBIAMoDFIDdHhzIrQBCgVCbG9jaxImCgZoZWFkZ
  XIYASABKAsyDi5wcm90bzMuSGVhZGVyUgZoZWFkZXISIAoEZGF0YRgCIAEoCzIMLnByb3RvMy5EYXRhUgRkYXRhEjAKCGV2aWRlb
  mNlGAMgASgLMhQucHJvdG8zLkV2aWRlbmNlRGF0YVIIZXZpZGVuY2USLwoLbGFzdF9jb21taXQYBCABKAsyDi5wcm90bzMuQ29tb
  Wl0UgpsYXN0Q29tbWl0IlQKBkNvbW1pdBIqCghibG9ja19pZBgBIAEoCzIPLnByb3RvMy5CbG9ja0lEUgdibG9ja0lkEh4KCnByZ
  WNvbW1pdHMYAiADKAxSCnByZWNvbW1pdHMiUAoMRXZpZGVuY2VEYXRhEiwKCGV2aWRlbmNlGAEgAygLMhAucHJvdG8zLkV2aWRlb
  mNlUghldmlkZW5jZRISCgRoYXNoGAIgASgMUgRoYXNoIsUBCghFdmlkZW5jZRISCgR0eXBlGAEgASgJUgR0eXBlEi8KCXZhbGlkY
  XRvchgCIAEoCzIRLnByb3RvMy5WYWxpZGF0b3JSCXZhbGlkYXRvchIWCgZoZWlnaHQYAyABKANSBmhlaWdodBIuCgR0aW1lGAQgA
  SgLMhouZ29vZ2xlLnByb3RvYnVmLlRpbWVzdGFtcFIEdGltZRIsChJ0b3RhbF92b3RpbmdfcG93ZXIYBSABKANSEHRvdGFsVm90a
  W5nUG93ZXIiOwoJVmFsaWRhdG9yEhgKB2FkZHJlc3MYASABKAxSB2FkZHJlc3MSFAoFcG93ZXIYAyABKANSBXBvd2VyItEBCg1DY
  W5vbmljYWxWb3RlEhIKBHR5cGUYASABKAVSBHR5cGUSFgoGaGVpZ2h0GAIgASgGUgZoZWlnaHQSFAoFcm91bmQYAyABKAZSBXJvd
  W5kEjMKCGJsb2NrX2lkGAQgASgLMhgucHJvdG8zLkNhbm9uaWNhbEJsb2NrSURSB2Jsb2NrSWQSLgoEdGltZRgFIAEoCzIaLmdvb
  2dsZS5wcm90b2J1Zi5UaW1lc3RhbXBSBHRpbWUSGQoIY2hhaW5faWQYBiABKAlSB2NoYWluSWQiaQoQQ2Fub25pY2FsQmxvY2tJR
  BISCgRoYXNoGAEgASgMUgRoYXNoEkEKDHBhcnRzX2hlYWRlchgCIAEoCzIeLnByb3RvMy5DYW5vbmljYWxQYXJ0U2V0SGVhZGVyU
  gtwYXJ0c0hlYWRlciJCChZDYW5vbmljYWxQYXJ0U2V0SGVhZGVyEhIKBGhhc2gYASABKAxSBGhhc2gSFAoFdG90YWwYAiABKAVSB
  XRvdGFsYgZwcm90bzM="""
        )
        .mkString
    )
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(
      javaProto,
      Array(
        com.google.protobuf.timestamp.TimestampProto.javaDescriptor
      )
    )
  }

  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}
