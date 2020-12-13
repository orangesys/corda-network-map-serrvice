/**
 *   Copyright 2018, Cordite Foundation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.cordite.networkmap.service

import io.cordite.networkmap.changeset.Change
import io.cordite.networkmap.changeset.changeSet
import io.cordite.networkmap.storage.file.NetworkParameterInputsStorage
import io.cordite.networkmap.utils.*
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import net.corda.core.crypto.Crypto
import net.corda.core.crypto.sign
import net.corda.core.identity.PartyAndCertificate
import net.corda.core.internal.sign
import net.corda.core.internal.signWithCert
import net.corda.core.node.NodeInfo
import net.corda.core.serialization.serialize
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.days
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.seconds
import net.corda.node.services.network.NetworkMapClient
import net.corda.nodeapi.internal.NodeInfoAndSigned
import net.corda.nodeapi.internal.crypto.CertificateType
import net.corda.nodeapi.internal.crypto.X509Utilities
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.node.internal.MOCK_VERSION_INFO
import org.junit.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.math.max
import kotlin.test.*

@RunWith(VertxUnitRunner::class)
class NetworkMapServiceTest {
	companion object {
		init {
			SerializationTestEnvironment.init()
		}
		
		@JvmField
		@ClassRule
		val mdcClassRule = JunitMDCRule()
		
		val NETWORK_PARAM_UPDATE_DELAY = 5.seconds
		val NETWORK_MAP_QUEUE_DELAY = 1.seconds
		
		const val TEST_CERT = "-----BEGIN CERTIFICATE-----\n" +
			"MIIDSzCCAjOgAwIBAgIEIGkklDANBgkqhkiG9w0BAQsFADBVMQswCQYDVQQGEwJH\n" +
			"QjELMAkGA1UECBMCVUsxDzANBgNVBAcTBkxvbmRvbjEMMAoGA1UEChMDbm1zMQww\n" +
			"CgYDVQQLEwNubXMxDDAKBgNVBAMTA25tczAgFw0xOTA4MTMxMTU4MDZaGA8yMjkz\n" +
			"MDUyNzExNTgwNlowVTELMAkGA1UEBhMCR0IxCzAJBgNVBAgTAlVLMQ8wDQYDVQQH\n" +
			"EwZMb25kb24xDDAKBgNVBAoTA25tczEMMAoGA1UECxMDbm1zMQwwCgYDVQQDEwNu\n" +
			"bXMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCXTJeZ6C1+tzDokDgj\n" +
			"1U79r2AikiEnwJBSL3pA3tDHxZUJtyhbIdsKp13Oznqe5w2F+ww0dh+WT1vmSI0v\n" +
			"/mc/Xn6/JCey1vet4k1AlLI5FE2PeN5Qkwh94IMs80WGXB55TzfS+xDCSmoz3SZG\n" +
			"MGTZHyF1qcf7FvR10mcKGKYrA6Z73F0LqWPgDOeo1E7ydQM4bznWYz5AiX1w6Awk\n" +
			"vhURBOE+QOwljUtOsKISTAeTCYfkD2aFyx17rHtQ2Wast/pDRZPywBrjQ6U7MY4H\n" +
			"00LlnQDJH2grV8wyg05z688x+qmhpKOmezM2HVVLY6wTo09hjP7u2pMCYL59b7B7\n" +
			"XKWHAgMBAAGjITAfMB0GA1UdDgQWBBQj3fpcHS7hXllaF9VNIhDvOxPWXjANBgkq\n" +
			"hkiG9w0BAQsFAAOCAQEAYz7bijvxAZrZtdAcPGGCGQVOMwYyKp0aiWsGx889qBZB\n" +
			"UCWBU4gBhq7dL58I9nBmy8LVwSezQTv2/uy5npp8JoIsCoE7xOrrHQPmAeILwd4L\n" +
			"Z9b48DDqKpMjHcGBTRMZZOd9zxV6rQuhs8NqcDCUc17Ws1xKDqy/FFKGbD+9JLwY\n" +
			"SYDCenKO8DxglCgJeT1BltbNF8pUpDvv63CP2k5XFTbaskAv3fN8kI9jJKJeVPPd\n" +
			"mZ3fMH/0pv5o8ks1F9mtfERHfvjStZ0CZKd1U0VFe7nh+hZa1/HogD5YIPzZ4pMY\n" +
			"V1B8clL+gEhT2YT3np7r2JB/UMwmiBzptaPTzYLQ7Q==\n" +
			"-----END CERTIFICATE-----\n"
		const val TEST_PRIV_KEY = "-----BEGIN PRIVATE KEY-----\n" +
			"MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCXTJeZ6C1+tzDo\n" +
			"kDgj1U79r2AikiEnwJBSL3pA3tDHxZUJtyhbIdsKp13Oznqe5w2F+ww0dh+WT1vm\n" +
			"SI0v/mc/Xn6/JCey1vet4k1AlLI5FE2PeN5Qkwh94IMs80WGXB55TzfS+xDCSmoz\n" +
			"3SZGMGTZHyF1qcf7FvR10mcKGKYrA6Z73F0LqWPgDOeo1E7ydQM4bznWYz5AiX1w\n" +
			"6AwkvhURBOE+QOwljUtOsKISTAeTCYfkD2aFyx17rHtQ2Wast/pDRZPywBrjQ6U7\n" +
			"MY4H00LlnQDJH2grV8wyg05z688x+qmhpKOmezM2HVVLY6wTo09hjP7u2pMCYL59\n" +
			"b7B7XKWHAgMBAAECggEAF533FCkv4NXLpqHMgZtsZyEXCo5w6nmejZWnqbSeNnDb\n" +
			"INccT41rWbBibkPA1EkQzVcwwoelypq8p6evkkxN5ZBkjIWiRMJViykGEgyj/VD0\n" +
			"Am9FlKI+8xc2oq+erKh4gUsiVaanKQzgoxPkQ5SOSW4YlPtZohIlOrcLKoJKffAn\n" +
			"lnHydl4ANMPJrfGJe4Qt/f2NaXU0lUmZIcds4Vy31UjsFv3S9JYiw1461P7AjwyW\n" +
			"xky9g3TqXaUIAb5qRkXlsmcsSHDpUMDIdEVOfRT5Wp5YozFccHheqoicLqyzPGJ8\n" +
			"YoUus+6jaevsrT4sJgah2zXVOpmNxWznsrNfroTJsQKBgQDpj9vd7sbBHalf2lUn\n" +
			"xRcbHwRMU1BwHp+Bta53hO6OpLQRQdB3HyYUSw1ZNuHGn5AbY9m6fFG17nDsdSac\n" +
			"DdFhrZ/T4HeeouOLyuI4vSyqyr/lDjifg18W/6Ef4eRjtmCFG7Bf/GhEYPIlE03U\n" +
			"eVY14gzCdyF0OPQSXekY/Xt0NQKBgQCl1Zd4pg7DuO4BLo9iG8IRFcaIPOO1S8AU\n" +
			"+LiWjnQn7kYRTVIHW48ewL9oNQNQeKAqg6AH0lwUjn+7u450npie4hmyRICYozgu\n" +
			"IfB4t2ugLEnIddWGsilG3lGsQtRfSzY50aOODyOdlt/ZjiXsUWkUcnIpVpI5Fohr\n" +
			"3sklrupySwKBgAprrLeeK6f6FtB8AUig9oLzQ1HUdcZK13sGDB6xA6PBnXcXpKFr\n" +
			"9lr5bkMIu9IM1HUkY1Z6rXqchNVH9M0JoMnm4Tam0S92vQhDqQw3BIMqQJ2LoOMd\n" +
			"DWUNSrcNcQ8x1+SYc6oUrOJAIQ2eat6L+1i0GTWj8w4obFtVbUz8IkHxAoGATQKN\n" +
			"Xseun5FnZUEb8LF3Q7vbADVWgUB2KMb/4Vqx8kEiZLKFX0lTgzJ7MIc9zMbXiiap\n" +
			"0DgN7rXl92Y310w3D8FrsW9CUJ+rXTEBnO2Am1c1xFOEHxLpPDHNt7MpMD+bJOqH\n" +
			"i1bYcTw5I6xxS/baV1Z2UWAUc1JVc9J3knpSAVUCgYEAtxZZCnld4L0VUGGg/v1t\n" +
			"PkE4760u5y7su133YjSp1rfEJ3MHBs2MiN8WASK7VqV2mE9HBy6+cRsxlpXHMePP\n" +
			"6AzrYrzpG5Iq+c0510nMTM8WAZaYEmKVs5Vy0KZ3O9j4zXE1dKcKGHuLHc5nXMss\n" +
			"9/wwFw2whBqt8hqRSpfYAME=\n" +
			"-----END PRIVATE KEY-----\n"
	}

	val log = loggerFor<NetworkMapServiceTest>()
	
	@JvmField
	@Rule
	val mdcRule = JunitMDCRule()
	
	private val dbDirectory = createTempDir()
	private val PORT = getFreePort()
	val WEB_ROOT = "/root"
	
	private lateinit var vertx: Vertx
	private lateinit var service: NetworkMapService
	private lateinit var client: HttpClient

	@Before
	fun before(context: TestContext) {
		vertx = Vertx.vertx()
		val nmsOptions = NMSOptions(
			dbDirectory = dbDirectory,
			user = InMemoryUser("", "sa", ""),
			port = PORT,
			cacheTimeout = CACHE_TIMEOUT,
			tls = false,
			webRoot = WEB_ROOT,
			paramUpdateDelay = NETWORK_PARAM_UPDATE_DELAY,
			enableDoorman = false,
			enableCertman = true,
			pkix = false,
			truststore = null,
			trustStorePassword = null,
			strictEV = false,
			storageType = StorageType.FILE
		)
		
		this.service = NetworkMapService(nmsOptions, Vertx.vertx(VertxOptions().setEventLoopPoolSize(6)))
		
		val completed = Future.future<Unit>()
		service.startup().setHandler(completed)
		completed
			.compose { service.processor.initialiseWithTestData(vertx = vertx, includeNodes = false) }
			.setHandler(context.asyncAssertSuccess())

		client = vertx.createHttpClient(HttpClientOptions()
			.setDefaultHost(DEFAULT_HOST)
			.setDefaultPort(PORT)
			.setSsl(false)
			.setTrustAll(true)
			.setVerifyHost(false)
		)
	}
	
	@After
	fun after(context: TestContext) {
		service.shutdown()
		val async = context.async()
		vertx.close {
			context.assertTrue(it.succeeded())
			async.complete()
		}
	}
	
	@Test
	fun `that we can configure network map with default network map parameters`() {
		val nmc = createNetworkMapClient()
		val nmp = nmc.getNetworkParameters(nmc.getNetworkMap().payload.networkParameterHash).verified()
		assertEquals(nmp.minimumPlatformVersion, 4)
		assertEquals(nmp.maxMessageSize, 10485760)
		assertEquals(nmp.maxTransactionSize, Int.MAX_VALUE)
		assertEquals(nmp.epoch, 2)
		assertEquals(nmp.eventHorizon, 30.days)
		assertEquals(nmp.packageOwnership, emptyMap())
	}
	
	@Test
	fun `that we can retrieve network map and parameters and they are correct`(context: TestContext) {
		val nmc = createNetworkMapClient()
		val nmp = nmc.getNetworkParameters(nmc.getNetworkMap().payload.networkParameterHash)
		val notaries = nmp.verified().notaries
		
		context.assertEquals(2, notaries.size)
		context.assertEquals(1, notaries.filter { it.validating }.count())
		context.assertEquals(1, notaries.filter { !it.validating }.count())
		
		val nis = getNetworkParties(nmc)
		context.assertEquals(0, nis.size)
	}
	
	@Test
	fun `that "my-host" is localhost`(context: TestContext) {
		val nmc = createNetworkMapClient()
		val hostname = nmc.myPublicHostname()
		context.assertEquals("localhost", hostname)
	}
	
	@Test
	fun `that we can add a new node`(context: TestContext) {
		val nmc = createNetworkMapClient()
		val sni = createAliceSignedNodeInfo()
		nmc.publish(sni.signed)
		//Thread.sleep(NETWORK_MAP_QUEUE_DELAY.toMillis() * 2)
		NMSUtil.waitForNMSUpdate(vertx)
		val nm = nmc.getNetworkMap().payload
		val nhs = nm.nodeInfoHashes
		context.assertEquals(1, nhs.size)
		assertEquals(sni.signed.raw.hash, nhs[0])
	}
	
	@Test
	fun `that we cannot register the same node name with a different key`(context: TestContext) {
		val nmc = createNetworkMapClient()
		
		val sni1 = createAliceSignedNodeInfo()
		nmc.publish(sni1.signed)
		//Thread.sleep(NETWORK_MAP_QUEUE_DELAY.toMillis() * 2)
		NMSUtil.waitForNMSUpdate(vertx)
		val nm = nmc.getNetworkMap().payload
		val nhs = nm.nodeInfoHashes
		context.assertEquals(1, nhs.size)
		assertEquals(sni1.signed.raw.hash, nhs[0])
		
		val sni2 = createAliceSignedNodeInfo()
		
		val pk1 = sni1.nodeInfo.legalIdentities.first().owningKey
		val pk2 = sni2.nodeInfo.legalIdentities.first().owningKey
		assertNotEquals(pk1, pk2)
		try {
			nmc.publish(sni2.signed)
			throw RuntimeException("should have throw IOException complaining that the node has been registered before")
		} catch (err: Throwable) {
			if (err !is IOException) {
				throw err
			}
			assertEquals("Response Code 500: node failed to register because the following names have already been registered with different public keys O=Alice Corp, L=Madrid, C=ES", err.message)
		}
	}
	
	@Test
	fun `that we can modify and acknowledge the network parameters`(testContext: TestContext) {
		val async = testContext.async()
		val nmc = createNetworkMapClient()
		deleteValidatingNotaries(nmc)
			// we wait for the NMS to process the request
			.compose { vertx.sleep(max(1, NetworkParameterInputsStorage.DEFAULT_WATCH_DELAY)) }
			.compose { vertx.sleep(max(1, NETWORK_MAP_QUEUE_DELAY.toMillis() * 2)) } // will need to check these - it seems excessive
			// at this point the NMS should have created a ParametersUpdate - retrieve the new network map that contains this update
			.map {
				val nm = nmc.getNetworkMap().payload
				// check the update time is right
				assertNotNull(nm.parametersUpdate, "expecting parameter update plan")
				val deadLine = nm.parametersUpdate!!.updateDeadline
				val delay = Duration.between(Instant.now(), deadLine)
				assert(delay > Duration.ZERO && delay <= NETWORK_PARAM_UPDATE_DELAY)
				delay // returns the delay
			}
			// wait for the update to be applied as per the NetworkMap ParameterUpdate plan
			.compose { delay -> vertx.sleep(max(1, delay.toMillis() * 2)) }
			// the network map should be updated with the planned update - the validating notaries should've been deleted
			.compose {
				val nm = nmc.getNetworkMap().payload
				assertNull(nm.parametersUpdate)
				val nmp = nmc.getNetworkParameters(nm.networkParameterHash).verified()
				assertEquals(1, nmp.notaries.size)
				assertTrue(nmp.notaries.all { !it.validating })
				val keyPair = Crypto.generateKeyPair()
				val signedHash = nmp.serialize().hash.serialize().sign(keyPair)
				nmc.ackNetworkParametersUpdate(signedHash)
				service.latestParametersAccepted(keyPair.public)
			}.onSuccess {
				val nm = nmc.getNetworkMap().payload  // this dead locks - it's on the vertx main event loop thread - presume the http server is too.  wonder why the previous ones work
				assertEquals(it, nm.networkParameterHash)
				async.complete()
			}
			.catch(testContext::fail)
	}
	
	@Test
	fun `that we can submit a certificate and signature to certman`(context: TestContext) {
		// prepare the payload
		val privKey = CryptoUtils.decodePEMPrivateKey(TEST_PRIV_KEY)
		val cert = CertificateFactory.getInstance("X.509").generateCertificate(ByteArrayInputStream(TEST_CERT.toByteArray())) as X509Certificate
		val sign = Base64.getEncoder().encodeToString(TEST_CERT.signWithCert(privKey, cert).raw.bytes)
		val payload = TEST_CERT + sign
		
		val client = vertx.createHttpClient(HttpClientOptions().setDefaultHost("localhost").setDefaultPort(PORT))
		val async = context.async()
		
		@Suppress("DEPRECATION")
		client.post("$WEB_ROOT${NetworkMapService.CERTMAN_REST_ROOT}/generate") { it ->
			if (!it.isOkay()) {
				context.fail("failed with ${it.statusMessage()}")
			}

			it.bodyHandler { body ->
				ZipInputStream(ByteArrayInputStream(body.bytes)).use {
					var entry = it.nextEntry
					while (entry != null) {
						
						entry = it.nextEntry
					}
					async.complete()
				}
			}
		}
			.putHeader(HttpHeaders.CONTENT_LENGTH, payload.length.toString())
			.exceptionHandler {
				context.fail(it)
			}
			.end(payload)
	}
	
	private fun getNetworkParties(nmc: NetworkMapClient) =
		nmc.getNetworkMap().payload.nodeInfoHashes.map { nmc.getNodeInfo(it) }
	
	
	private fun createNetworkMapClient(): NetworkMapClient {
		return NetworkMapClient(URL("http://localhost:$PORT$WEB_ROOT"), MOCK_VERSION_INFO).apply {
			start(service.certificateManager.rootCertificateAndKeyPair.certificate)
		}
	}
	
	private fun createTempDir(): File {
		return Files.createTempDirectory("nms-test-").toFile()
			.apply {
				mkdirs()
				deleteOnExit()
			}
	}
	
	private fun deleteValidatingNotaries(nmc: NetworkMapClient): Future<Unit> {
		val parameters = nmc.getNetworkParameters(nmc.getNetworkMap().payload.networkParameterHash).verified()
		val notaries = parameters.notaries.filter { it.validating }.map { it.identity.name.serialize().hash }.map { Change.RemoveNotary(it) }
		return service.processor.updateNetworkParameters(changeSet(notaries), "removing all validating notaries")
	}
	
	private fun createAliceSignedNodeInfo(): NodeInfoAndSigned {
		val cm = service.certificateManager
		// create the certificate chain from the doorman to node CA to legal identity
		val nodeCA = cm.createCertificateAndKeyPair(cm.doormanCertAndKeyPair, ALICE_NAME, CertificateType.NODE_CA)
		val legalIdentity = cm.createCertificateAndKeyPair(nodeCA, ALICE_NAME, CertificateType.LEGAL_IDENTITY)
		val certPath = X509Utilities.buildCertPath(
			legalIdentity.certificate,
			nodeCA.certificate,
			cm.doormanCertAndKeyPair.certificate,
			cm.rootCertificateAndKeyPair.certificate
		)
		val alicePartyAndCertificate = PartyAndCertificate(certPath)
		val ni = NodeInfo(listOf(NetworkHostAndPort("localhost", 10001)), listOf(alicePartyAndCertificate), 1, 1)
		return NodeInfoAndSigned(ni) { _, serialised ->
			legalIdentity.keyPair.private.sign(serialised.bytes)
		}
	}
}

private fun HttpClientResponse.isOkay(): Boolean {
	return ((this.statusCode() / 100) == 2)
}
