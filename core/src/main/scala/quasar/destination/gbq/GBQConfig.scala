/*
 * Copyright 2020 Precog Data
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

package quasar.destination.gbq

import argonaut._, Argonaut._

import cats.implicits._

import java.net.{URI, URISyntaxException}

import scala.{
  StringContext,
  Either
}
import scala.Predef.String

final case class GBQConfig(authCfg: ServiceAccountConfig, datasetId: String)

final case class ServiceAccountConfig(
  tokenUri: URI,
  authProviderCertUrl: URI,
  privateKey: String,
  clientId: String,
  clientCertUrl: URI,
  authUri: URI,
  projectId: String,
  privateKeyId: String,
  clientEmail: String,
  accountType: String)

object GBQConfig {
  implicit val uriCodecJson: CodecJson[URI] =
    CodecJson(
      uri => Json.jString(uri.toString),
      c => for {
        uriStr <- c.jdecode[String]
        uri0 = Either.catchOnly[URISyntaxException](new URI(uriStr))
        uri <- uri0.fold(
          ex => DecodeResult.fail(s"Invalid URI: ${ex.getMessage}", c.history),
          DecodeResult.ok(_))
      } yield uri)

  implicit val serviceAccountConfigCodecJson: CodecJson[ServiceAccountConfig] = 
    casecodec10[URI,URI, String, String, URI, URI, String, String, String, String, ServiceAccountConfig](
      (tokenUri,
      authProviderCertUrl,
      privateKey,
      clientId,
      clientCertUrl,
      authUri,
      projectId,
      privateKeyId,
      clientEmail,
      accountType) => ServiceAccountConfig(
        tokenUri,
        authProviderCertUrl,
        privateKey,
        clientId,
        clientCertUrl,
        authUri,
        projectId,
        privateKeyId,
        clientEmail,
        accountType),
      sac => 
        (sac.tokenUri, 
        sac.authProviderCertUrl,
        sac.privateKey,
        sac.clientId,
        sac.clientCertUrl,
        sac.authUri,
        sac.projectId,
        sac.privateKeyId,
        sac.clientEmail,
        sac.accountType).some)(
          "token_uri",
          "auth_provider_x509_cert_url",
          "private_key",
          "client_id",
          "client_x509_cert_url",
          "auth_uri",
          "project_id",
          "private_key_id",
          "client_email",
          "type")

  implicit val gbqConfigCodecJson: CodecJson[GBQConfig] =
    CodecJson(
      (g: GBQConfig) =>
        ("authCfg" := g.authCfg) ->:
        ("datasetId" := g.datasetId) ->:
        jEmptyObject,
      c => for {
        authCfg <- (c --\ "authCfg").as[ServiceAccountConfig]
        datasetId <- (c --\ "datasetId").as[String]
      } yield GBQConfig(authCfg, datasetId))
}