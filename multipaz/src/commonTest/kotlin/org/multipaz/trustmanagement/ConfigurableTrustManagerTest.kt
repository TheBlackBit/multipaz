package org.multipaz.trustmanagement

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import org.multipaz.asn1.ASN1Integer
import org.multipaz.crypto.AsymmetricKey
import org.multipaz.crypto.Crypto
import org.multipaz.crypto.EcCurve
import org.multipaz.crypto.X500Name
import org.multipaz.crypto.X509Cert
import org.multipaz.crypto.X509CertChain
import org.multipaz.crypto.X509KeyUsage
import org.multipaz.crypto.buildX509Cert
import org.multipaz.mdoc.rical.Rical
import org.multipaz.mdoc.rical.RicalCertificateInfo
import org.multipaz.mdoc.rical.SignedRical
import org.multipaz.mdoc.util.MdocUtil
import org.multipaz.mdoc.vical.SignedVical
import org.multipaz.mdoc.vical.Vical
import org.multipaz.mdoc.vical.VicalCertificateInfo
import org.multipaz.util.truncateToWholeSeconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ConfigurableTrustManagerTest {

    private lateinit var caCertificate: X509Cert
    private lateinit var intermediateCertificate: X509Cert
    private lateinit var dsCertificate: X509Cert

    private fun runTestWithSetup(block: suspend TestScope.() -> Unit) = runTest { setup(); block() }

    private suspend fun setup() {
        val now = Clock.System.now().truncateToWholeSeconds()

        val caKey = Crypto.createEcPrivateKey(EcCurve.P384)
        caCertificate = buildX509Cert(
            publicKey = caKey.publicKey,
            signingKey = AsymmetricKey.anonymous(caKey, caKey.curve.defaultSigningAlgorithm),
            serialNumber = ASN1Integer(1L),
            subject = X500Name.fromName("CN=Test TrustManager CA"),
            issuer = X500Name.fromName("CN=Test TrustManager CA"),
            validFrom = now - 1.hours,
            validUntil = now + 1.hours
        ) {
            includeSubjectKeyIdentifier()
            setKeyUsage(setOf(X509KeyUsage.KEY_CERT_SIGN))
            setBasicConstraints(true, null)
        }

        val intermediateKey = Crypto.createEcPrivateKey(EcCurve.P384)
        intermediateCertificate = buildX509Cert(
            publicKey = intermediateKey.publicKey,
            signingKey = AsymmetricKey.anonymous(caKey, caKey.curve.defaultSigningAlgorithm),
            serialNumber = ASN1Integer(1L),
            subject = X500Name.fromName("CN=Test TrustManager Intermediate CA"),
            issuer = caCertificate.subject,
            validFrom = now - 1.hours,
            validUntil = now + 1.hours
        ) {
            includeSubjectKeyIdentifier()
            setAuthorityKeyIdentifierToCertificate(caCertificate)
            setKeyUsage(setOf(X509KeyUsage.KEY_CERT_SIGN))
            setBasicConstraints(true, null)
        }

        val dsKey = Crypto.createEcPrivateKey(EcCurve.P384)
        dsCertificate = buildX509Cert(
            publicKey = dsKey.publicKey,
            signingKey = AsymmetricKey.anonymous(intermediateKey, intermediateKey.curve.defaultSigningAlgorithm),
            serialNumber = ASN1Integer(1L),
            subject = X500Name.fromName("CN=Test TrustManager DS"),
            issuer = intermediateCertificate.subject,
            validFrom = now - 1.hours,
            validUntil = now + 1.hours
        ) {
            includeSubjectKeyIdentifier()
            setAuthorityKeyIdentifierToCertificate(intermediateCertificate)
            setKeyUsage(setOf(X509KeyUsage.DIGITAL_SIGNATURE))
        }
    }

    private data class TestIaca(
        val elboniaIaca: X509Cert,
        val atlantisIaca: X509Cert,
        val encodedSignedVical: ByteString,
        val elboniaDs: X509Cert,
    )

    private data class TestRical(
        val breweryReaderRootCert: X509Cert,
        val breweryReaderCert: X509Cert,
        val encodedSignedRical: ByteString,
    )

    private suspend fun createTestIaca(): TestIaca {
        val now = Clock.System.now().truncateToWholeSeconds()
        val validFrom = now - 10.minutes
        val validUntil = now + 10.minutes

        val elboniaIacaKey = Crypto.createEcPrivateKey(EcCurve.P256)
        val elboniaIaca = MdocUtil.generateIacaCertificate(
            iacaKey = AsymmetricKey.anonymous(elboniaIacaKey),
            subject = X500Name.fromName("CN=Elbonia TrustManager CA"),
            serial = ASN1Integer.fromRandom(numBits = 128),
            validFrom = validFrom,
            validUntil = validUntil,
            issuerAltNameUrl = "https://example.com/elbonia/altname",
            crlUrl = "https://example.com/elbonia/crl"
        )
        val elboniaDsKey = Crypto.createEcPrivateKey(EcCurve.P256)
        val elboniaDs = MdocUtil.generateDsCertificate(
            iacaKey = AsymmetricKey.X509CertifiedExplicit(
                privateKey = elboniaIacaKey,
                certChain = X509CertChain(listOf(elboniaIaca))
            ),
            dsKey = elboniaDsKey.publicKey,
            subject = X500Name.fromName("CN=Elbonia DS"),
            serial = ASN1Integer.fromRandom(numBits = 128),
            validFrom = validFrom,
            validUntil = validUntil
        )

        val atlantisIacaKey = Crypto.createEcPrivateKey(EcCurve.P256)
        val atlantisIaca = MdocUtil.generateIacaCertificate(
            iacaKey = AsymmetricKey.anonymous(atlantisIacaKey),
            subject = X500Name.fromName("CN=Atlantis TrustManager CA"),
            serial = ASN1Integer.fromRandom(numBits = 128),
            validFrom = validFrom,
            validUntil = validUntil,
            issuerAltNameUrl = "https://example.com/atlantis/altname",
            crlUrl = "https://example.com/atlantis/crl"
        )

        val vical = Vical(
            version = "1",
            vicalProvider = "Test VICAL provider",
            date = now,
            nextUpdate = null,
            vicalIssueID = null,
            certificateInfos = listOf(
                VicalCertificateInfo(
                    certificate = elboniaIaca,
                    docTypes = listOf("org.iso.18013.5.1.mDL")
                ),
                VicalCertificateInfo(
                    certificate = atlantisIaca,
                    docTypes = listOf("org.iso.18013.5.1.mDL")
                )
            ),
            notAfter = null,
            vicalUrl = null,
            extensions = emptyMap(),
        )

        val vicalKey = Crypto.createEcPrivateKey(EcCurve.P256)
        val vicalCert = buildX509Cert(
            publicKey = vicalKey.publicKey,
            signingKey = AsymmetricKey.anonymous(vicalKey, vicalKey.curve.defaultSigningAlgorithm),
            serialNumber = ASN1Integer(1),
            subject = X500Name.fromName("CN=Test VICAL provider"),
            issuer = X500Name.fromName("CN=Test VICAL provider"),
            validFrom = validFrom,
            validUntil = validUntil
        ) {
            includeSubjectKeyIdentifier()
        }

        val signedVical = SignedVical(vical, X509CertChain(listOf(vicalCert)))
        return TestIaca(
            elboniaIaca = elboniaIaca,
            atlantisIaca = atlantisIaca,
            encodedSignedVical = ByteString(
                signedVical.generate(AsymmetricKey.anonymous(vicalKey))
            ),
            elboniaDs = elboniaDs,
        )
    }

    private suspend fun createTestRical(): TestRical {
        val now = Clock.System.now().truncateToWholeSeconds()
        val validFrom = now - 10.minutes
        val validUntil = now + 10.minutes

        val breweryReaderRootKey = Crypto.createEcPrivateKey(EcCurve.P256)
        val breweryReaderRootCert = MdocUtil.generateReaderRootCertificate(
            readerRootKey = AsymmetricKey.anonymous(breweryReaderRootKey),
            subject = X500Name.fromName("CN=Brewery TrustManager CA"),
            serial = ASN1Integer.fromRandom(numBits = 128),
            validFrom = validFrom,
            validUntil = validUntil,
            crlUrl = "https://example.com/brewery/crl"
        )
        val breweryReaderKey = Crypto.createEcPrivateKey(EcCurve.P256)
        val breweryReaderCert = MdocUtil.generateReaderCertificate(
            readerRootKey = AsymmetricKey.X509CertifiedExplicit(
                privateKey = breweryReaderRootKey,
                certChain = X509CertChain(listOf(breweryReaderRootCert))
            ),
            readerKey = breweryReaderKey.publicKey,
            dnsName = "brewery.multipaz.org",
            subject = X500Name.fromName("CN=Brewery"),
            serial = ASN1Integer.fromRandom(numBits = 128),
            validFrom = validFrom,
            validUntil = validUntil
        )

        val rical = Rical(
            type = Rical.RICAL_TYPE_READER_AUTHENTICATION,
            version = "1",
            provider = "Test RICAL provider",
            date = now,
            nextUpdate = null,
            notAfter = null,
            certificateInfos = listOf(
                RicalCertificateInfo(
                    certificate = breweryReaderRootCert,
                )
            ),
            id = null,
            latestRicalUrl = null,
            extensions = emptyMap(),
        )

        val ricalKey = Crypto.createEcPrivateKey(EcCurve.P256)
        val ricalCert = buildX509Cert(
            publicKey = ricalKey.publicKey,
            signingKey = AsymmetricKey.anonymous(ricalKey, ricalKey.curve.defaultSigningAlgorithm),
            serialNumber = ASN1Integer(1),
            subject = X500Name.fromName("CN=Test RICAL provider"),
            issuer = X500Name.fromName("CN=Test RICAL provider"),
            validFrom = validFrom,
            validUntil = validUntil
        ) {
            includeSubjectKeyIdentifier()
        }

        val signedRical = SignedRical(rical, X509CertChain(listOf(ricalCert)))
        return TestRical(
            breweryReaderRootCert = breweryReaderRootCert,
            breweryReaderCert = breweryReaderCert,
            encodedSignedRical = ByteString(
                signedRical.generate(AsymmetricKey.anonymous(ricalKey))
            ),
        )
    }

    @Test
    fun verifyX509Entries() = runTestWithSetup {
        val entryCa = TrustEntryX509Cert(
            identifier = "ca",
            metadata = TrustMetadata(),
            certificate = caCertificate
        )
        val entryIntermediate = TrustEntryX509Cert(
            identifier = "intermediate",
            metadata = TrustMetadata(),
            certificate = intermediateCertificate
        )
        val trustManager = ConfigurableTrustManager(
            identifier = "configurable_test",
            entries = listOf(entryCa, entryIntermediate)
        )

        trustManager.verify(listOf(dsCertificate)).let {
            assertEquals(null, it.error)
            assertTrue(it.isTrusted)
            assertEquals(3, it.trustChain!!.certificates.size)
            assertEquals(caCertificate, it.trustChain.certificates.last())
        }
    }

    @Test
    fun verifyVicalEntries() = runTestWithSetup {
        val testIaca = createTestIaca()
        val entryVical = TrustEntryVical(
            identifier = "vical",
            metadata = TrustMetadata(),
            encodedSignedVical = testIaca.encodedSignedVical
        )
        val trustManager = ConfigurableTrustManager(
            identifier = "configurable_test",
            entries = listOf(entryVical)
        )

        trustManager.verify(listOf(testIaca.elboniaDs)).let {
            assertEquals(null, it.error)
            assertTrue(it.isTrusted)
            assertEquals(2, it.trustChain!!.certificates.size)
            assertEquals(testIaca.elboniaIaca, it.trustChain.certificates.last())
        }
    }

    @Test
    fun verifyRicalEntries() = runTestWithSetup {
        val testRical = createTestRical()
        val entryRical = TrustEntryRical(
            identifier = "rical",
            metadata = TrustMetadata(),
            encodedSignedRical = testRical.encodedSignedRical
        )
        val trustManager = ConfigurableTrustManager(
            identifier = "configurable_test",
            entries = listOf(entryRical)
        )

        trustManager.verify(listOf(testRical.breweryReaderCert)).let {
            assertEquals(null, it.error)
            assertTrue(it.isTrusted)
            assertEquals(2, it.trustChain!!.certificates.size)
            assertEquals(testRical.breweryReaderRootCert, it.trustChain.certificates.last())
        }
    }

    @Test
    fun emptyEntriesFailVerification() = runTestWithSetup {
        val trustManager = ConfigurableTrustManager(
            identifier = "configurable_test",
            entries = emptyList()
        )

        trustManager.verify(listOf(dsCertificate)).let {
            assertFalse(it.isTrusted)
            assertEquals("No trusted root certificate could not be found", it.error?.message)
            assertNull(it.trustChain)
        }
    }

    @Test
    fun updateEntriesDynamically() = runTestWithSetup {
        val trustManager = ConfigurableTrustManager(
            identifier = "configurable_test",
            entries = emptyList()
        )

        // Starts with empty trust cache
        trustManager.verify(listOf(dsCertificate)).let {
            assertFalse(it.isTrusted)
        }

        val entryIntermediate = TrustEntryX509Cert(
            identifier = "intermediate",
            metadata = TrustMetadata(),
            certificate = intermediateCertificate
        )

        // Replace state with a new set of entries
        trustManager.setEntries(listOf(entryIntermediate))

        // State is updated correctly
        val currentEntries = trustManager.getEntries()
        assertEquals(1, currentEntries.size)
        assertEquals(entryIntermediate, currentEntries[0])

        // Verify it now resolves
        trustManager.verify(listOf(dsCertificate)).let {
            assertEquals(null, it.error)
            assertTrue(it.isTrusted)
            assertEquals(2, it.trustChain!!.certificates.size)
            assertEquals(intermediateCertificate, it.trustChain.certificates.last())
        }
    }

    @Test
    fun returnsAllTrustPointsCorrectly() = runTestWithSetup {
        val testIaca = createTestIaca()
        val entryX509 = TrustEntryX509Cert(
            identifier = "x509",
            metadata = TrustMetadata(),
            certificate = caCertificate
        )
        val entryVical = TrustEntryVical(
            identifier = "vical",
            metadata = TrustMetadata(),
            encodedSignedVical = testIaca.encodedSignedVical
        )

        val trustManager = ConfigurableTrustManager(
            identifier = "configurable_test",
            entries = listOf(entryX509, entryVical)
        )

        val points = trustManager.getTrustPoints()

        // Should contain the X509 cert plus the 2 CA certs embedded in the VICAL payload
        assertEquals(3, points.size)

        val certs = points.map { it.certificate }
        assertTrue(certs.contains(caCertificate))
        assertTrue(certs.contains(testIaca.elboniaIaca))
        assertTrue(certs.contains(testIaca.atlantisIaca))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun eventFlowEmissions() = runTestWithSetup {
        val trustManager = ConfigurableTrustManager(
            identifier = "configurable_test",
            entries = emptyList()
        )
        var numEvents = 0

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            trustManager.eventFlow.collect {
                numEvents += 1
            }
        }

        // 1. Initial setEntries emission
        val entryCa = TrustEntryX509Cert(
            identifier = "ca",
            metadata = TrustMetadata(),
            certificate = caCertificate
        )
        trustManager.setEntries(listOf(entryCa))
        assertEquals(1, numEvents)

        // 2. Subsequent setEntries emission
        val entryIntermediate = TrustEntryX509Cert(
            identifier = "intermediate",
            metadata = TrustMetadata(),
            certificate = intermediateCertificate
        )
        trustManager.setEntries(listOf(entryCa, entryIntermediate))
        assertEquals(2, numEvents)

        job.cancel()
    }
}