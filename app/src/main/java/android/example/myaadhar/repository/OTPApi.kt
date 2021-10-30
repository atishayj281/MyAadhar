package android.example.myaadhar.repository


import android.example.myaadhar.Constants
import android.example.myaadhar.models.Opts
import android.example.myaadhar.models.Otp
import android.example.myaadhar.models.OtpRes
import android.example.myaadhar.models.Type
import android.example.myaadhar.util.NamespaceFilter
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.WebResource
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import org.xml.sax.helpers.XMLReaderFactory
import java.io.FileInputStream
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.lang.Exception
import java.net.InetAddress
import java.net.URI
import java.security.KeyStore
import java.security.Security
import java.security.cert.X509Certificate
import java.util.*
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.crypto.dsig.CanonicalizationMethod
import javax.xml.crypto.dsig.DigestMethod
import javax.xml.crypto.dsig.SignatureMethod
import javax.xml.crypto.dsig.XMLSignatureFactory
import javax.xml.crypto.dsig.dom.DOMSignContext
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec
import javax.xml.crypto.dsig.spec.TransformParameterSpec
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult


class OTPApi {
    var configProp = Properties()
    @Throws(IOException::class)
    fun readProperties() {
        val `in` = this.javaClass.classLoader.getResourceAsStream("application.properties")
        configProp.load(`in`)
    }

    @Throws(Exception::class)
    fun getOtpRes(uid: String, txnId: String): OtpRes {
        val type: Type
        type = if (uid.length > 12) {
            Type.V
        } else {
            Type.A
        }
        val otp: Otp = createOtpRequest(configProp.getProperty(Constants.AUTH_REQUEST_AUA),
            configProp.getProperty(Constants.AUTH_REQUEST_ASA),
            configProp.getProperty(Constants.AUTH_REQUEST_AUA_LK),
            uid,
            txnId,
            type)
        return getParsedResponseFromOtpServer(otp)
    }

    private fun createOtpRequest(
        aua: String, sa: String, licenseKey: String,
        uid: String, txn: String, type: Type,
    ): Otp {
        val otpReq = Otp()
        otpReq.uid = uid
        otpReq.ver = "2.5"
        otpReq.ac = aua
        otpReq.sa = sa
        otpReq.type = type
        otpReq.txn = txn
        otpReq.lk = licenseKey
        // Using India TZ
        val calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        otpReq.ts = XMLGregorianCalendarImpl.createDateTime(
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH] + 1,
            calendar[Calendar.DAY_OF_MONTH],
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            calendar[Calendar.SECOND])
        val c = Opts()
        c.ch = "01"
        otpReq.opts = c
        return otpReq
    }

    @Throws(Exception::class)
    private fun generateSignedOtpXML(otp: Otp): String {
        val otpXML = StringWriter()
        val element = JAXBElement(QName(
            "http://www.uidai.gov.in/authentication/otp/1.0", "Otp"),
            Otp::class.java, otp)
        JAXBContext.newInstance(Otp::class.java).createMarshaller()
            .marshal(element, otpXML)
        val includeKeyInfo = true
        return signXML(otpXML.toString(), includeKeyInfo)
    }

    @Throws(JAXBException::class, SAXException::class)
    private fun parseOtpResponseXML(xmlToParse: String): OtpRes {
        val jc: JAXBContext = JAXBContext.newInstance(OtpRes::class.java)
        val u: Unmarshaller = jc.createUnmarshaller()
        val reader: XMLReader = XMLReaderFactory.createXMLReader()

        // Create the filter (to add namespace) and set the xmlReader as its
        // parent.
        val inFilter = NamespaceFilter(
            "http://www.uidai.gov.in/authentication/otp/1.0", true)
        inFilter.parent = reader

        // Prepare the input, in this case a java.io.File (output)
        val `is` =
            InputSource(StringReader(xmlToParse))

        // Create a SAXSource specifying the filter
        val source =
            SAXSource(inFilter, `is`)

        // Do unmarshalling
        return u.unmarshal(source, OtpRes::class.java).value
    }

    @Throws(Exception::class)
    private fun getParsedResponseFromOtpServer(otp: Otp): OtpRes {
        val signedXML = generateSignedOtpXML(otp)
        var uriString: String = if (otp.uid.length < 16) {
            (configProp.getProperty(Constants.URL)
                    + (if (configProp.getProperty(Constants.URL).endsWith("/")) "" else "/")
                    + otp.ac + "/" + otp.uid[0] + "/"
                    + otp.uid[1])
        } else {
            (configProp.getProperty(Constants.URL)
                    + (if (configProp.getProperty(Constants.URL).endsWith("/")) "" else "/")
                    + otp.ac + "/" + "0" + "/"
                    + "0")
        }
        uriString = uriString + "/" + configProp.getProperty(Constants.AUTH_REQUEST_ASA_LK)
        val otpURI = URI(uriString)
        val webResource: WebResource = Client.create().resource(otpURI)
        val responseXML: String = webResource.header("REMOTE_ADDR",
            InetAddress.getLocalHost().hostAddress).post(
            String::class.java, signedXML)
        return parseOtpResponseXML(responseXML)
    }

    @Throws(Exception::class)
    private fun signXML(xmlDocument: String, includeKeyInfo: Boolean): String {
        Security.addProvider(BouncyCastleProvider())
        // Parse the input XML
        val dbf = DocumentBuilderFactory.newInstance()
        dbf.isNamespaceAware = true
        val inputDocument = dbf.newDocumentBuilder().parse(InputSource(StringReader(xmlDocument)))

        // Sign the input XML's DOM document
        val signedDocument = sign(inputDocument, includeKeyInfo)

        // Convert the signedDocument to XML String
        val stringWriter = StringWriter()
        val tf = TransformerFactory.newInstance()
        val trans = tf.newTransformer()
        trans.transform(DOMSource(signedDocument), StreamResult(stringWriter))
        return stringWriter.buffer.toString()
    }

    @Throws(Exception::class)
    private fun sign(xmlDoc: Document, includeKeyInfo: Boolean): Document {

        // Creating the XMLSignature factory.
        val fac: XMLSignatureFactory = XMLSignatureFactory.getInstance("DOM")
        // Creating the reference object, reading the whole document for
        // signing.
        val ref: javax.xml.crypto.dsig.Reference = fac.newReference("",
            fac.newDigestMethod(DigestMethod.SHA1, null),
            listOf<javax.xml.crypto.dsig.Transform>(fac.newTransform(javax.xml.crypto.dsig.Transform.ENVELOPED,
                null as TransformParameterSpec?)),
            null,
            null)

        // Create the SignedInfo.
        val sInfo: javax.xml.crypto.dsig.SignedInfo = fac.newSignedInfo(
            fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                null as C14NMethodParameterSpec?),
            fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
            listOf<javax.xml.crypto.dsig.Reference>(ref))
        val x509Cert = authReqKeyFromKeyStore.certificate as X509Certificate
        val kInfo: javax.xml.crypto.dsig.keyinfo.KeyInfo = getKeyInfo(x509Cert, fac)
        val dsc = DOMSignContext(authReqKeyFromKeyStore.privateKey, xmlDoc.documentElement)
        val signature: javax.xml.crypto.dsig.XMLSignature =
            fac.newXMLSignature(sInfo, if (includeKeyInfo) kInfo else null)
        signature.sign(dsc)
        val node: Node = dsc.parent
        return node.ownerDocument
    }

    @get:Throws(Exception::class)
    private val authReqKeyFromKeyStore: KeyStore.PrivateKeyEntry
        private get() {
            FileInputStream(configProp.getProperty(Constants.SIGNATURE_FILE)).use { fileInputStream ->
                val keyStore =
                    KeyStore.getInstance("PKCS12")
                keyStore.load(fileInputStream,
                    configProp.getProperty(Constants.SIGNATURE_PASSWORD).toCharArray())
                return keyStore.getEntry(configProp.getProperty(Constants.SIGNATURE_ALIAS),
                    KeyStore.PasswordProtection(configProp.getProperty(Constants.SIGNATURE_PASSWORD)
                        .toCharArray())) as KeyStore.PrivateKeyEntry
            }
        }

    private fun getKeyInfo(
        cert: X509Certificate,
        fac: XMLSignatureFactory,
    ): javax.xml.crypto.dsig.keyinfo.KeyInfo {
        // Create the KeyInfo containing the X509Data.
        val kif: KeyInfoFactory = fac.keyInfoFactory
        val xd: javax.xml.crypto.dsig.keyinfo.X509Data =
            kif.newX509Data(listOf(cert.subjectX500Principal.name, cert))
        return kif.newKeyInfo(listOf<javax.xml.crypto.dsig.keyinfo.X509Data>(xd))
    }
}
