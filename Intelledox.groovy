package portal.Utils

import grails.util.Environment
import groovy.json.JsonSlurper
import groovy.sql.Sql
import sun.misc.BASE64Decoder
import groovy.xml.*
import wslite.soap.*
import wslite.http.auth.*
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTP;
import sun.misc.BASE64Decoder;
import portal.Utils.FileTransferHelper;

class Intelledox {
    def createIndicationSpecialEventsPDF(dataMap, uwQuestionsMap, uwQuestionsOrder, dataSource_aim) {
        try {
            def indicationDateFormat = 'MM/dd/yyyy'
            def now = new Date()
            def timeZone = TimeZone.getTimeZone('PST')
            def timestamp = now.format(indicationDateFormat, timeZone)

            FileTransferHelper fileHelper = new FileTransferHelper();

            def totalPolicyFee = 0;
            def coverages = "";
            if (dataMap.getAt("cglLOB").length() > 1) {
                coverages = coverages + "CGL "
            }
            if (dataMap.getAt("alcoholLOB").length() > 1) {
                coverages = coverages + "ALCOHOL "
            }

            //BROKER HEADER STUFF
            def brokerCompanyName = XmlUtil.escapeXml(dataMap.getAt('brokerCompanyName'));
            def brokerCompanyAddress = XmlUtil.escapeXml(dataMap.getAt('brokerCompanyAddress'));
            def brokerCompanyAddressCity = XmlUtil.escapeXml(dataMap.getAt('brokerCompanyCity'));
            def brokerCompanyState = XmlUtil.escapeXml(dataMap.getAt('brokerCompanyState'));
            def brokerCompanyZip = XmlUtil.escapeXml(dataMap.getAt('brokerCompanyZip'));
            def brokerCompanyPhone = XmlUtil.escapeXml(dataMap.getAt('brokerCompanyPhone'));
            def brokerCompanyLicense = XmlUtil.escapeXml(dataMap.getAt('brokerCompanyLicense'));

            def environment = ""
            if (Environment.current == Environment.DEVELOPMENT) {
                environment = "dev";
            }
            else if (Environment.current == Environment.PRODUCTION) {
                environment = "prod";
            }

            def soapXML = """<x:Envelope xmlns:x="http://schemas.xmlsoap.org/soap/envelope/" xmlns:int="http://services.dpm.com.au/intelledox/">
    <x:Header/>
    <x:Body>
        <int:GenerateWithData>
            <int:userName>xxxxxx</int:userName>
            <int:password>xxxxxxx</int:password>
            <int:projectGroupGuid>xxxxxxxx-xxxxx-xxxxx-xxxxx-xxxxxxxx</int:projectGroupGuid>
            <int:providedData>
                <int:ProvidedData>
                    <int:DataServiceGuid>xxxxxxxx-xxxxx-xxxxx-xxxxx-xxxxxxxx</int:DataServiceGuid>
                    <int:Data><![CDATA[<?xml version="1.0" encoding="utf-8"?>
<application>
\t<basicInfo>
\t\t<environment>${environment}</environment>
\t\t<logo>c:\\IntelledoxLogo\\${XmlUtil.escapeXml(dataMap.getAt('logoFile'))}</logo>
\t\t<nameOfInsured>${XmlUtil.escapeXml(dataMap.getAt('namedInsured'))}</nameOfInsured>
\t\t<brokerCompanyName>${XmlUtil.escapeXml(dataMap.getAt('brokerCompanyName'))}</brokerCompanyName>"""

            if (brokerCompanyAddress != null) {
                soapXML = soapXML + """
\\t\\t<brokerCompanyAddress>${brokerCompanyAddress}</brokerCompanyAddress>"""
            }
            if (brokerCompanyAddressCity != null) {
                soapXML = soapXML + """
\\t\\t<brokerCompanyAddressCity>${brokerCompanyAddressCity}</brokerCompanyAddressCity>"""
            }
            if (brokerCompanyState != null) {
                soapXML = soapXML + """
\\t\\t<brokerCompanyAddressState>,${brokerCompanyState}</brokerCompanyAddressState>"""
            }
            if (brokerCompanyZip != null) {
                soapXML = soapXML + """
\\t\\t<brokerCompanyAddressZip>${brokerCompanyZip}</brokerCompanyAddressZip>"""
            }
            if (brokerCompanyPhone != null) {
                soapXML = soapXML + """
\t\t<brokerCompanyPhone>${brokerCompanyPhone}</brokerCompanyPhone>"""
            }
            if (brokerCompanyLicense != null) {
                soapXML = soapXML + """
\\t\\t<brokerCompanyLicenseNumber> CALicNo:${brokerCompanyLicense}</brokerCompanyLicenseNumber>"""
            }

            soapXML = soapXML + """
\t\t<agentName>${XmlUtil.escapeXml(dataMap.getAt('attention'))}</agentName>
\t\t<agentLicenseNumber>${
                XmlUtil.escapeXml(dataMap.getAt('brokerCompanyLicense')) ? XmlUtil.escapeXml(dataMap.getAt('brokerCompanyLicense')) : ""
            }</agentLicenseNumber>
\t\t<agentEmail>${XmlUtil.escapeXml(dataMap.getAt('brokerEmail'))}</agentEmail>
\t\t<agentPhone>${XmlUtil.escapeXml(dataMap.getAt('brokerPhone'))}</agentPhone>
\t\t<date>${timestamp}</date>
\t\t<dateStart>${XmlUtil.escapeXml(dataMap.getAt('proposedEffective'))}</dateStart>
\t\t<submission>${XmlUtil.escapeXml(dataMap.getAt('allQuoteIDs').split(';')[0])}</submission>
\t\t<underwriter>${XmlUtil.escapeXml(dataMap.getAt('accountExecName'))}</underwriter>
\t\t<underwriterPhone>${XmlUtil.escapeXml(dataMap.getAt('underwriterPhone'))}</underwriterPhone>
\t\t<underwriterFax>${XmlUtil.escapeXml(dataMap.getAt('underwriterFax'))}</underwriterFax>
\t\t<underwriterEmail>${XmlUtil.escapeXml(dataMap.getAt('accountExecEmail'))}</underwriterEmail>
\t\t<total>Total:</total>
\t\t<totalCost>${XmlUtil.escapeXml(dataMap.getAt('premiumAllLOBTotal'))}</totalCost>
\t\t<addressOfInsured>${XmlUtil.escapeXml(dataMap.getAt('streetNameMailing'))}</addressOfInsured>
\t\t<addressCityOfInsured>${XmlUtil.escapeXml(dataMap.getAt('cityMailing'))}</addressCityOfInsured>
\t\t<addressZipOfInsured>${XmlUtil.escapeXml(dataMap.getAt('zipCodeMailing'))}</addressZipOfInsured>
\t\t<riskDescription>${XmlUtil.escapeXml(dataMap.getAt("riskCategory"))}, ${
                XmlUtil.escapeXml(dataMap.getAt("riskChosen"))
            }</riskDescription>
\t\t<locationOfRiskAddress>${XmlUtil.escapeXml(dataMap.getAt("filmingLocation"))}</locationOfRiskAddress>
\t\t<insuranceCoverage>${coverages}</insuranceCoverage>
\t\t<insuranceCompany>${XmlUtil.escapeXml(dataMap.getAt("insuranceCompany"))}</insuranceCompany>
\t</basicInfo>
\t
\t<namedInsuredTable>
\t\t<namedInsuredHeader>Named Insured</namedInsuredHeader>
\t\t<namedInsuredRow>
\t\t\t<nameInsured nameInsuredColOne="${XmlUtil.escapeXml(dataMap.getAt("namedInsured"))}"></nameInsured>
\t\t\t<nameInsuredColTwo></nameInsuredColTwo>
\t\t</namedInsuredRow>
\t\t<namedInsuredRow>
\t\t\t<nameInsured nameInsuredColOne="${XmlUtil.escapeXml(dataMap.getAt('streetNameMailing'))}"></nameInsured>
\t\t\t<nameInsuredColTwo></nameInsuredColTwo>
\t\t</namedInsuredRow>
\t\t<namedInsuredRow>
\t\t\t<nameInsured nameInsuredColOne="${XmlUtil.escapeXml(dataMap.getAt('cityMailing'))}, ${
                XmlUtil.escapeXml(dataMap.getAt('stateMailing'))
            } ${XmlUtil.escapeXml(dataMap.getAt('zipCodeMailing'))}"></nameInsured>
\t\t\t<nameInsuredColTwo></nameInsuredColTwo>
\t\t</namedInsuredRow>
\t</namedInsuredTable>
\t
\t<insuranceCompanyTable>
\t\t<insuranceCompanyHeader>Insurance Company</insuranceCompanyHeader>
\t\t<insuranceCompanyRow>
\t\t\t<insuranceCompany insuranceCompanyColOne="${XmlUtil.escapeXml(dataMap.getAt("insuranceCompany"))}"></insuranceCompany>
\t\t</insuranceCompanyRow>
\t</insuranceCompanyTable>
\t
\t<policyTermTable>
\t\t<policyTermHeader>Policy Term</policyTermHeader>
\t\t<policyTermRow>
\t\t\t<policyTerm policyTermColOne="Policy Term: ${XmlUtil.escapeXml(dataMap.getAt("proposedEffectiveDate"))}"></policyTerm>
\t\t</policyTermRow>
\t\t<policyTermRow>
\t\t\t<policyTerm policyTermColOne="Proposed Effective: ${
                XmlUtil.escapeXml(dataMap.getAt("proposedEffectiveDate"))
            } - ${XmlUtil.escapeXml(dataMap.getAt("proposedExpirationDate"))}"></policyTerm>
\t\t</policyTermRow>
\t</policyTermTable>
\t""";
            soapXML = soapXML + """
\t<premiumSummaryTable>
\t\t<premiumSummaryHeader>Premium Summary</premiumSummaryHeader>
\t\t<premiumSummaryRow>""";
            if (dataMap.getAt("premSummary").split("\n").size() > 0) {
                dataMap.getAt("premSummary").split("\n").each {
                    if (it.length() > 0) {
                        if (it.split("\\t")[0] == "Premium Distribution") {

                        } else if (it.split("\\t")[0] == "Taxes and Fees") {
                            soapXML = soapXML + """
\t\t<premiumSummary premiumSummaryPackage="${it.split("\\t")[0]}">
\t\t\t<premiumSummaryCost>  </premiumSummaryCost>
\t\t</premiumSummary>"""
                        } else if (it.split("\\t")[0] == "Policy Fee") {
                            soapXML = soapXML + """
\t\t<premiumSummary premiumSummaryPackage="   ${it.split("\\t")[0]}">
\t\t\t<premiumSummaryCost>${it.split("\t")[1]} </premiumSummaryCost>
\t\t</premiumSummary>"""
                        } else {
                            soapXML = soapXML + """
\t\t<premiumSummary premiumSummaryPackage="   ${it.split("\\t")[0]}">
\t\t\t<premiumSummaryCost>${it.split("\t")[1]} </premiumSummaryCost>
\t\t</premiumSummary>"""
                        }

                    } else {

                    }
                }
            } else {
                log.info dataMap.getAt("premiumAllLOBTotal")
                soapXML = soapXML + """
\\t\\t<premiumSummary package="Total">
\\t\\t\\t<cost>  </cost>
\\t\\t</premiumSummary>"""
            }
            soapXML = soapXML + """
\t\t</premiumSummaryRow>
\t</premiumSummaryTable>""";

            ///////////////////Product descriptions and Limit/Deduct Breakdowns

            if (dataMap.getAt("cglLOB").length() > 1) {
                soapXML = soapXML + """
\t<coverageTable>
\t\t<coverageHeader>Commercial Package Policy - Limits/Deductibles</coverageHeader>
\t\t<coverageRow>""";
                dataMap.getAt("cglLOB").split("\n").each {
                    if (it.length() > 0) {
                        log.info("CGLLOB TESTING ===== " + it)
                        soapXML = soapXML + """
\t\t<coverage coveragePackage="${it.split("\t")[0]}">
\t\t\t<coverageLimit> ${it.split("\t")[1]} </coverageLimit>
\t\t\t<coverageDeductible> ${it.split("\t").size() >= 3 ? it.split("\t")[2] : ""} </coverageDeductible>
\t\t</coverage>
\t"""
                    }
                }
                soapXML = soapXML + """
\t\t</coverageRow>
\t</coverageTable>"""
            }
            if (dataMap.getAt("alcoholLOB").length() > 1) {
                soapXML = soapXML + """
\t<coverageTable>
\t\t<coverageHeader>Non-Owned and Hired Automobile- Limits/Deductibles</coverageHeader>
\t\t<coverageRow>""";
                dataMap.getAt("alcoholLOB").split("\n").each {
                    if (it.length() > 0) {
                        soapXML = soapXML + """
\t\t<coverage coveragePackage="${it.split("\t")[0]}">
\t\t\t<coverageLimit> ${it.split("\t")[1]} </coverageLimit>
\t\t\t<coverageDeductible> ${it.split("\t").size() >= 3 ? it.split("\t")[2] : ""} </coverageDeductible>
\t\t</coverage>
\t"""
                    }
                }
                soapXML = soapXML + """
\t\t</coverageRow>
\t</coverageTable>"""
            }



            soapXML = soapXML + """
\t<termsTable>
\t\t<termHeader>Terms</termHeader>
\t\t<term>
\t\t\t<terms>${XmlUtil.escapeXml(dataMap.getAt("termsInsert"))} </terms>
\t\t</term>
\t</termsTable>"""

            soapXML = soapXML + """
\t<policyFormEndorsementTable>
\t\t<policyFormEndorsementTitle>Policy Form / Endorsement</policyFormEndorsementTitle>
\t</policyFormEndorsementTable>
"""
            log.info(dataMap.getAt("endorseInsert"))
            if (dataMap.getAt("cglLOB").length() > 1) {
                dataMap.getAt("endorseInsert").split("\n").eachWithIndex { row, index ->
                    if (row.length() > 0) {
                        if (index == 0) {
                            soapXML = soapXML + """
\t<policyFormEndorsementHeaders>
\t\t<policyFormEndorsementHeader>${XmlUtil.escapeXml(row)}</policyFormEndorsementHeader>
\t\t\t<policyFormEndorsementRow>
"""
                        } else {
                            log.info row
                            log.info row.split(" - ")[0]
                            log.info row.split(" - ")[1]

                            soapXML = soapXML + """
\t\t\t\t<policyFormEndorsement policyFormEndorsementCode="${XmlUtil.escapeXml(row.split(" - ")[0])}">
\t\t\t\t\t\t<policyFormEndorsementName>${XmlUtil.escapeXml(row.split(" - ")[1])}</policyFormEndorsementName>
\t\t\t\t</policyFormEndorsement>
"""
                        }
                    }
                }
                soapXML = soapXML + """
\t\t\t</policyFormEndorsementRow>
\t</policyFormEndorsementHeaders>
"""

            }
            soapXML = soapXML + """
\t<notesTable>
\t\t<notesHeader>Underwriting Questions</notesHeader>
\t\t<notesRow>"""
            uwQuestionsOrder.each {

                soapXML = soapXML + """

\t\t\t<notes notesQuestion="${XmlUtil.escapeXml("${it}")}">
\t\t\t\t<notesAnswer>${XmlUtil.escapeXml(uwQuestionsMap["${it}"])}</notesAnswer>
\t\t\t</notes>"""
            }
            soapXML = soapXML + """
\t\t</notesRow>
\t</notesTable>"""

            soapXML = soapXML + """
\t
\t<applicantInformationTable>
\t\t<applicantInformationHeader>Application Information</applicantInformationHeader>
\t\t<applicantInformationRow>
\t\t\t<applicantInformation applicantInformationColOne="Name of Insured">
\t\t\t\t<applicantInformationColTwo>${XmlUtil.escapeXml(dataMap.getAt("namedInsured"))}</applicantInformationColTwo>
\t\t\t</applicantInformation>
\t\t\t<applicantInformation applicantInformationColOne="Website">
\t\t\t\t<applicantInformationColTwo>${XmlUtil.escapeXml(dataMap.getAt("website"))}</applicantInformationColTwo>
\t\t\t</applicantInformation>
\t\t\t<applicantInformation applicantInformationColOne="Mailing Address">
\t\t\t\t<applicantInformationColTwo>${XmlUtil.escapeXml(dataMap.getAt("streetNameMailing"))} ${
                XmlUtil.escapeXml(dataMap.getAt("cityMailing"))
            }, ${XmlUtil.escapeXml(dataMap.getAt("stateMailing"))} ${
                XmlUtil.escapeXml(dataMap.getAt("zipCodeMailing"))
            } </applicantInformationColTwo>
\t\t\t</applicantInformation>
\t\t\t<applicantInformation applicantInformationColOne="Primary Contact Name">
\t\t\t\t<applicantInformationColTwo>${XmlUtil.escapeXml(dataMap.getAt("namedInsured"))} </applicantInformationColTwo>
\t\t\t</applicantInformation>
\t\t\t<applicantInformation applicantInformationColOne="Tel No">
\t\t\t\t<applicantInformationColTwo>${XmlUtil.escapeXml(dataMap.getAt("phoneNumber"))} </applicantInformationColTwo>
\t\t\t</applicantInformation>
\t\t\t<applicantInformation applicantInformationColOne="Email">
\t\t\t\t<applicantInformationColTwo>${XmlUtil.escapeXml(dataMap.getAt("namedInsuredEmail"))} </applicantInformationColTwo>
\t\t\t</applicantInformation>
\t\t</applicantInformationRow>
\t</applicantInformationTable>"""

            soapXML = soapXML + """
\t<ratingTable>
\t\t<ratingHeader>Rating</ratingHeader>
\t"""
            if (dataMap.getAt("CGLIndicationRateInfo") != null) {
                soapXML = soapXML + """
\t\t<ratingRow>
\t\t\t<rating ratingName="Commercial General Liability">
\t\t\t\t<ratingPrice>  </ratingPrice>
\t\t\t</rating>"""

                dataMap.getAt("CGLIndicationRateInfo").split("\n").eachWithIndex { row, index ->
                    log.info("ROW: " + row)
                    if (row.split("\t").size() > 1) {
                        soapXML = soapXML + """
\t\t\t<rating ratingName="${row.split("\t")[0]}">
\t\t\t\t<ratingPrice> ${XmlUtil.escapeXml(row.split("\t")[1])}</ratingPrice>
\t\t\t</rating>"""
                    } else {
                        soapXML = soapXML + """
\t\t\t<rating ratingName="${row.split("\t")[0]}">
\t\t\t\t<ratingPrice></ratingPrice>
\t\t\t</rating>"""
                    }
                }
                soapXML = soapXML + """
\t\t</ratingRow>"""
            }
            soapXML = soapXML + """
\t</ratingTable>
\t



</application>]]></int:Data>
                </int:ProvidedData>
            </int:providedData>
            <int:options>
                <int:ReturnDocuments>true</int:ReturnDocuments>
                <int:RunProviders>1</int:RunProviders>
                <int:LogGeneration>true</int:LogGeneration>
            </int:options>
        </int:GenerateWithData>
    </x:Body>
</x:Envelope>"""

            log.info soapXML

            def client = new SOAPClient('http://xxx.xx.xxx.xx/Produce/Service/GenerateDoc.asmx?WSDL')
            client.authorization = new HTTPBasicAuthorization("xxxx", "xxxxxxxx")
            def response = client.send(SOAPAction: 'http://services.dpm.com.au/intelledox/GenerateWithData', soapXML)

            if (response.text.length() > 1500) {
                log.info response.text.substring(0, 1500);
            } else {
                log.info response.text
            }

            def fileName = "Indication A.pdf"

            def a = new XmlSlurper().parseText(response.text)
            def nodeToSerialize = a."**".find { it.name() == 'BinaryFile' }
            def pdfBinaryFile = nodeToSerialize.text();

            log.info("NEW FOLDER QUOTE = " + dataMap.getAt("allQuoteIDs"))

            dataMap.getAt("allQuoteIDs").split(",").each {
                def quoteID = it.split(";")[0]
                def folderPath = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getRealPath("/attachments/${quoteID}/")
                log.info folderPath

                fileHelper.saveBinaryFileToLocalPath(pdfBinaryFile, folderPath, fileName);

                fileHelper.ftpFileToAIM(fileName, folderPath, quoteID, dataSource_aim);
            }

            return "good"
        }
        catch (Exception e) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            printWriter.flush();
            String stackTrace = writer.toString();
            log.info("Error Details - " + stackTrace)

            return "Indication Error"
        }


    }

    def createCertPDF(params, dataSource_aim){
        FileTransferHelper fileHelper = new FileTransferHelper();
        def projectGUID = "";
        if (params.ai == "true") {
            //WITH AI FORM
            projectGUID = "xxxxxxxx-xxxxx-xxxx-xxxx-xxxxxxxx"
        } else {
            //WITHOUT AI FORM
            projectGUID = "xxxxxxxx-xxxxx-xxxx-xxxx-xxxxxxxx"
        }

        def soapXML = """<x:Envelope xmlns:x="http://schemas.xmlsoap.org/soap/envelope/" xmlns:int="http://services.dpm.com.au/intelledox/">
    <x:Header/>
    <x:Body>
        <int:GenerateWithData>
            <int:userName>xxxx</int:userName>
            <int:password>xxxxxxxx</int:password>
            <int:projectGroupGuid>${projectGUID}</int:projectGroupGuid>
            <int:providedData>
                <int:ProvidedData>
                    <int:DataServiceGuid>xxxxxxxx-xxxxx-xxxx-xxxx-xxxxxxxx</int:DataServiceGuid>
                    <int:Data><![CDATA[<?xml version="1.0" encoding="utf-8"?>
<application>
\t<certificate>
\t\t
\t\t<date>${params.date}</date>
\t\t<brokerCompanyName>${params.brokerCompanyName}</brokerCompanyName>
\t\t<brokerCompanyAddress>${params.brokerCompanyAddress + "\n" + params.brokerCompanyCity + "," + params.brokerCompanyState + " " + params.brokerCompanyZip}</brokerCompanyAddress>
\t\t<insured>${params.insured}</insured>
\t\t<insuredAddress>${params.insuredAddress}</insuredAddress>
\t\t<agentName>${params.contactName}</agentName>
\t\t<agentPhone>${params.contactPhone}</agentPhone>
\t\t<agentFax>${params.contactFax}</agentFax>
\t\t<agentEmail>${params.contactEmail}</agentEmail>
\t\t<insuranceCompanyName>${XmlUtil.escapeXml(params.insurer)}</insuranceCompanyName>
\t\t<NAIC>${params.NAIC}</NAIC>
\t\t<certificateNumber>${params.certificateNumber}</certificateNumber>
\t\t<revisionNumber>${params.revisionNumber}</revisionNumber>
\t\t<submissionID>${params.otherPolicyNumber + "\n" + params.generalPolicyNumber + "\n" + params.autoPolicyNumber}</submissionID>
\t\t<broker>${params['contactName']}</broker>

\t\t<insrltrGen>${params.insrltrGen}</insrltrGen>
\t\t<cbGenCommercialGeneralLiability>${params.cbGenCommercialGeneralLiability}</cbGenCommercialGeneralLiability>
\t\t<cbGenClaimsMade>${params.cbGenClaimsMade}</cbGenClaimsMade>
\t\t<cbGenOccur>${params.cbGenOccur}</cbGenOccur>
\t\t<cbGenPolicy>${params.cbGenPolicy}</cbGenPolicy>
\t\t<cbGenProject>${params.cbGenProject}</cbGenProject>
\t\t<cbGenLoc>${params.cbGenLoc}</cbGenLoc>
\t\t<genAddl>${params.genAddl}</genAddl>
\t\t<genSubr>${params.genSubr}</genSubr>
\t\t<generalPolicyNumber>${params.generalPolicyNumber}</generalPolicyNumber>
\t\t<genStart>${params.genStart}</genStart>
\t\t<genEnd>${params.genEnd}</genEnd>
\t\t<genEachLimit>${params.genEachLimit}</genEachLimit>
\t\t<genFireLimit>${params.genFireLimit}</genFireLimit>
\t\t<genMedLimit>${params.genMedLimit}</genMedLimit>
\t\t<genPersonalLimit>${params.genPersonalLimit}</genPersonalLimit>
\t\t<genAggregateLimit>${params.genAggregateLimit}</genAggregateLimit>
\t\t<genProductsLimit>${params.genProductsLimit}</genProductsLimit>

\t\t<insrltrAuto>${params.insrltrAuto}</insrltrAuto>
\t\t<cbAutoAny>${params.cbAutoAny}</cbAutoAny>
\t\t<cbAutoAllOwned>${params.cbAutoAllOwned}</cbAutoAllOwned>
\t\t<cbAutoHiredAuto>${params.cbAutoHiredAuto}</cbAutoHiredAuto>
\t\t<cbAutoPhysicalDamages>${params.cbAutoPhysicalDamages}</cbAutoPhysicalDamages>
\t\t<cbAutoScheduledAuto>${params.cbAutoScheduledAuto}</cbAutoScheduledAuto>
\t\t<cbAutoNonOwnedAuto>${params.cbAutoNonOwnedAuto}</cbAutoNonOwnedAuto>
\t\t<autoAddl>${params.autoAddl}</autoAddl>
\t\t<autoSubr>${params.autoSubr}</autoSubr>
\t\t<autoPolicyNumber>${params.autoPolicyNumber}</autoPolicyNumber>
\t\t<autoStart>${params.autoStart}</autoStart>
\t\t<autoEnd>${params.autoEnd}</autoEnd>
\t\t<autoCombinedSingleLimit>${params.autoCombinedSingleLimit}</autoCombinedSingleLimit>
\t\t<autoBodilyInjuryPersonLimit>${params.autoBodilyInjuryPersonLimit}</autoBodilyInjuryPersonLimit>
\t\t<autoBodilyInjuryAccidentLimit>${params.autoBodilyInjuryAccidentLimit}</autoBodilyInjuryAccidentLimit>
\t\t<autoPropertyDamageLimit>${params.autoPropertyDamageLimit}</autoPropertyDamageLimit>

\t\t<insrltrUmbrella></insrltrUmbrella>
\t\t<cbUmbrellaLiab></cbUmbrellaLiab>
\t\t<cbUmbrellaExcessLiab></cbUmbrellaExcessLiab>
\t\t<cbUmbrellaDeductible></cbUmbrellaDeductible>
\t\t<cbUmbrellaRetention></cbUmbrellaRetention>
\t\t<cbUmbrellaOccur></cbUmbrellaOccur>
\t\t<cbUmbrellaClaimsMade></cbUmbrellaClaimsMade>
\t\t<umbrellaRetentionLimit></umbrellaRetentionLimit>
\t\t<umbrellaAddl></umbrellaAddl>
\t\t<umbrellaSubr></umbrellaSubr>
\t\t<umbrellaPolicyNumber></umbrellaPolicyNumber>
\t\t<umbrellaStart></umbrellaStart>
\t\t<umbrellaEnd></umbrellaEnd>
\t\t<umbrellaEachOccurrenceLimit></umbrellaEachOccurrenceLimit>
\t\t<umbrellaAggregateLimit></umbrellaAggregateLimit>

\t\t<insrltrWorkersComp></insrltrWorkersComp>
\t\t<cbWorkerCompMemberExcluded></cbWorkerCompMemberExcluded>
\t\t<workersCompDescriptionNH></workersCompDescriptionNH>
\t\t<workersCompSubr></workersCompSubr>
\t\t<workersCompPolicyNumber></workersCompPolicyNumber>
\t\t<workersCompStart></workersCompStart>
\t\t<workersCompEnd></workersCompEnd>
\t\t<cbWorkersCompStatutoryLimits></cbWorkersCompStatutoryLimits>
\t\t<cbWorkersCompOther></cbWorkersCompOther>
\t\t<workersCompEachAccidentLimit></workersCompEachAccidentLimit>
\t\t<workersCompDiseaseEmployeeLimit></workersCompDiseaseEmployeeLimit>
\t\t<workersCompDiseasePolicyLimit></workersCompDiseasePolicyLimit>

\t\t<insrltrOther>${params.insrltrOther}</insrltrOther>
\t\t<riskType>${params.riskType}</riskType>
\t\t<otherAddl>${params.otherAddl}</otherAddl>
\t\t<otherSubr>${params.otherSubr}</otherSubr>
\t\t<otherPolicyNumber>${params.otherPolicyNumber}</otherPolicyNumber>
\t\t<otherStart>${params.otherStart}</otherStart>
\t\t<otherEnd>${params.otherEnd}</otherEnd>
\t\t<otherLimit>${params.otherLimit}</otherLimit>

\t\t<additionalRemarks>${params.additionalRemarks}</additionalRemarks>
\t\t<certificateHolder>${params.certificateHolder}</certificateHolder>
\t\t
\t\t<nameOfOrganization>${params.certificateHolder}</nameOfOrganization>
\t\t<nameOfOrganizationInformation>${params.additionalRemarks}</nameOfOrganizationInformation>
\t</certificate>"""

        if (params.ai == "true") {
            soapXML = soapXML + """    
\t<aOneForm>
\t\t<policyNumber>${params.submissionPolicyID}</policyNumber>
\t</aOneForm>"""
        }


        if (params.getAt("epkgLOB").length() > 1) {

            ////CPK GENERAL LIABILITY TABLE
            if (params.getAt("cpkLOB").length() > 1) {
                soapXML = soapXML + """
\t<coverageTable>
\t\t<coverageHeader>Commercial Package - Limits/Deductibles</coverageHeader>
\t\t<coverageRow>""";
                params.getAt("cpkLOB").split(";&&;").each {
                    if (it.length() > 0) {
                        soapXML = soapXML + """
\t\t\t<coverage coveragePackage="${XmlUtil.escapeXml(it.split(";&;")[0])}">
\t\t\t\t<coverageLimit> ${it.split(";&;")[1]} </coverageLimit>
\t\t\t\t<coverageDeductible> ${it.split(";&;").size() >= 3 ? it.split(";&;")[2] : ""} </coverageDeductible>
\t\t\t</coverage>"""
                    }
                }
                soapXML = soapXML + """
\t\t</coverageRow>
\t</coverageTable>"""
            }
            ////CGL GENERAL LIABILITY TABLE
            if (params.getAt("cglLOB").length() > 1) {
                soapXML = soapXML + """
\t<coverageTable>
\t\t<coverageHeader>Commercial General Liability - Limits/Deductibles</coverageHeader>
\t\t<coverageRow>""";
                params.getAt("cglLOB").split(";&&;").each {
                    if (it.length() > 0) {
                        soapXML = soapXML + """
\t\t\t<coverage coveragePackage="${XmlUtil.escapeXml(it.split(";&;")[0])}">
\t\t\t\t<coverageLimit> ${it.split(";&;")[1]} </coverageLimit>
\t\t\t\t<coverageDeductible> ${it.split(";&;").size() >= 3 ? it.split(";&;")[2] : ""} </coverageDeductible>
\t\t\t</coverage>"""
                    }
                }
                soapXML = soapXML + """
\t\t</coverageRow>
\t</coverageTable>"""
            }
            ////EPKG TABLE
            if (params.getAt("epkgLOB").length() > 1) {
                soapXML = soapXML + """
\t<coverageTable>
\t\t<coverageHeader>Entertainment Package - Limits/Deductibles</coverageHeader>
\t\t<coverageRow>""";
                params.getAt("epkgLOB").split(";&&;").each {
                    if (it.length() > 0) {
                        soapXML = soapXML + """
\t\t\t<coverage coveragePackage="${XmlUtil.escapeXml(it.split(";&;")[0])}">
\t\t\t\t<coverageLimit> ${it.split(";&;")[1]} </coverageLimit>
\t\t\t\t<coverageDeductible> ${it.split(";&;").size() >= 3 ? it.split(";&;")[2] : ""} </coverageDeductible>
\t\t\t</coverage>"""
                    }
                }
                soapXML = soapXML + """
\t\t</coverageRow>
\t</coverageTable>"""
            }
        }

        soapXML = soapXML + """

</application>]]></int:Data>
\t\t\t\t</int:ProvidedData>
\t\t\t</int:providedData>
\t\t\t<int:options>
\t\t\t\t<int:ReturnDocuments>true</int:ReturnDocuments>
\t\t\t\t<int:RunProviders>1</int:RunProviders>
\t\t\t\t<int:LogGeneration>true</int:LogGeneration>
\t\t\t</int:options>
\t\t</int:GenerateWithData>
    </x:Body>
</x:Envelope>"""

        def client = new SOAPClient('http://xxx.xx.xxx.xx/Produce/Service/GenerateDoc.asmx?WSDL')
        client.authorization = new HTTPBasicAuthorization("xxxx", "xxxxxxx")
        def response = client.send(SOAPAction:'http://services.dpm.com.au/intelledox/GenerateWithData', soapXML)

        def fileName = "Certificate-" + params.insured + ".pdf"

        //PARSE RESPONSE FROM INTELLEDOX SERVER FOR BINARY FILE
        def a = new XmlSlurper().parseText(response.text)
        def nodeToSerialize = a."**".find {it.name() == 'BinaryFile'}
        def pdfBinaryFile = nodeToSerialize.text();


        //GET THE SUBMISSION/QUOTE ID
        def quoteID = dataMap.getAt("allQuoteIDs").split(",")[0].split(";")[0]


        //GET REAL PATH OF FOLDER ON DIGITAL OCEAN
        def folderPath = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getRealPath("/attachments/${quoteID}/")

        //SAVE PDF TO BOTH DIGITAL OCEAN AND AIM SERVER
        fileHelper.saveBinaryFileToLocalPath(pdfBinaryFile, folderPath, fileName);
        fileHelper.ftpFileToAIM(fileName, folderPath, quoteID, dataSource_aim);

        return "SUCCESS"
    }

    def createSL2FormPDF(dataMap, uwQuestionsMap, uwQuestionsOrder, dataSource_aim) {
        try {
            FileTransferHelper fileHelper = new FileTransferHelper();

            def indicationDateFormat = 'MM/dd/yyyy'
            def now = new Date()
            def timeZone = TimeZone.getTimeZone('PST')
            def timestamp = now.format(indicationDateFormat, timeZone)

            def soapXML = """<x:Envelope xmlns:x="http://schemas.xmlsoap.org/soap/envelope/" xmlns:int="http://services.dpm.com.au/intelledox/">
    <x:Header/>
    <x:Body>
        <int:GenerateWithData>
            <int:userName>xxxxx</int:userName>
            <int:password>xxxxx</int:password>
            <int:projectGroupGuid>xxxxxxx-xxxx-xxxxx-xxxxx-xxxxxxxx</int:projectGroupGuid>
            <int:providedData>
                <int:ProvidedData>
                    <int:DataServiceGuid>xxxxxxx-xxxx-xxxxx-xxxxx-xxxxxxxx</int:DataServiceGuid>
                    <int:Data><![CDATA[<?xml version="1.0" encoding="utf-8"?>
<application>
\t<basicInfo>
\t\t<nameOfInsured>${XmlUtil.escapeXml(dataMap.getAt("nameOfProductionCompany"))}</nameOfInsured>
\t\t<addressOfInsured>${XmlUtil.escapeXml(dataMap.getAt('streetNameMailing'))}</addressOfInsured>
\t\t<addressCityOfInsured>${XmlUtil.escapeXml(dataMap.getAt('cityMailing'))}</addressCityOfInsured>
\t\t<addressZipOfInsured>${XmlUtil.escapeXml(dataMap.getAt('zipCodeMailing'))}</addressZipOfInsured>
\t\t<riskDescription>${XmlUtil.escapeXml(dataMap.getAt("riskCategory"))}, ${
                XmlUtil.escapeXml(dataMap.getAt("riskChosen"))
            }</riskDescription>
\t\t<locationOfRiskAddress>${XmlUtil.escapeXml(dataMap.getAt("filmingLocation"))}</locationOfRiskAddress>
\t\t<insuranceCoverage>${dataMap.getAt("nameOfProductionCompany")}</insuranceCoverage>
\t\t<RiskPurchasingGroupName></RiskPurchasingGroupName>
\t\t<RiskPurchasingGroupAddress></RiskPurchasingGroupAddress>
\t\t<nameOtherAgent></nameOtherAgent>
\t\t<date>${timestamp}</date>
\t\t<dateStart>${XmlUtil.escapeXml(dataMap.getAt('proposedEffectiveDate'))}</dateStart>
\t</basicInfo>
</application>]]></int:Data>
                </int:ProvidedData>
            </int:providedData>
            <int:options>
                <int:ReturnDocuments>true</int:ReturnDocuments>
                <int:RunProviders>1</int:RunProviders>
                <int:LogGeneration>true</int:LogGeneration>
            </int:options>
        </int:GenerateWithData>
    </x:Body>
</x:Envelope>"""

            def client = new SOAPClient('http://xxx.xx.xxx.xx/Produce/Service/GenerateDoc.asmx?WSDL')
            client.authorization = new HTTPBasicAuthorization("xxxx", "xxxxxxx")
            def response = client.send(SOAPAction:'http://services.dpm.com.au/intelledox/GenerateWithData', soapXML)

            def fileName = "SL2 A.pdf"

            //PARSE RESPONSE FROM INTELLEDOX SERVER FOR BINARY FILE
            def a = new XmlSlurper().parseText(response.text)
            def nodeToSerialize = a."**".find {it.name() == 'BinaryFile'}
            def pdfBinaryFile = nodeToSerialize.text();


            //GET THE SUBMISSION/QUOTE ID
            def quoteID = dataMap.getAt("allQuoteIDs").split(",")[0].split(";")[0]


            //GET REAL PATH OF FOLDER ON DIGITAL OCEAN
            def folderPath = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getRealPath("/attachments/${quoteID}/")

            //SAVE PDF TO BOTH DIGITAL OCEAN AND AIM SERVER
            fileHelper.saveBinaryFileToLocalPath(pdfBinaryFile, folderPath, fileName);
            fileHelper.ftpFileToAIM(fileName, folderPath, quoteID, dataSource_aim);

            return "SUCCESS"
        }
        catch (Exception e) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            printWriter.flush();
            String stackTrace = writer.toString();
            log.info("Error Details - " + stackTrace)

            return "SL2 Error"
        }
    }

    def createBindingPDF(dataMap, uwQuestionsMap, uwQuestionsOrder, dataSource_aim){
        FileTransferHelper fileHelper = new FileTransferHelper();

        def soapXML = """<x:Envelope xmlns:x="http://schemas.xmlsoap.org/soap/envelope/" xmlns:int="http://services.dpm.com.au/intelledox/">
    <x:Header/>
    <x:Body>
        <int:GenerateWithData>
            <int:userName>xxxx</int:userName>
            <int:password>xxxxxxx</int:password>
            <int:projectGroupGuid>xxxxxx-xxxxx-xxxx-xxxxx-xxxxxxxxx</int:projectGroupGuid>
            <int:providedData>
                <int:ProvidedData>
                    <int:DataServiceGuid>xxxxxx-xxxxx-xxxx-xxxxx-xxxxxxxxx</int:DataServiceGuid>
                    <int:Data><![CDATA[<?xml version="1.0" encoding="utf-8"?>
<application>
\t<binding>
\t\t<date>${dataMap.getAt("dateAdded").substring(1, dataMap.getAt("dateAdded").length() - 1).split(" ")[0]}</date>
\t\t<startDate>${dataMap.getAt("proposedEffectiveDate")} </startDate>
\t\t<effectiveDate>${dataMap.getAt("proposedEffectiveDate")} - ${dataMap.getAt("proposedExpirationDate")} </effectiveDate>
\t\t<endDate>${dataMap.getAt("proposedExpirationDate")}</endDate>
\t\t<broker>${dataMap.getAt("brokerFirstName")} ${dataMap.getAt("brokerLastName")}</broker>
\t\t<insured>${XmlUtil.escapeXml(dataMap.getAt('namedInsured'))}</insured>
\t\t<insuredStreet>${dataMap.getAt("streetNameMailing")}</insuredStreet>
\t\t<insuredCity>${dataMap.getAt("cityMailing")}</insuredCity>
\t\t<insuredState>${dataMap.getAt("stateMailing")}</insuredState>
\t\t<insuredZip>${dataMap.getAt("zipCodeMailing")}</insuredZip>
\t\t<coverageType>${coverages}</coverageType>
\t\t<emailBody>We are pleased to enclose the attached Insurance Binder as per your Binding Instructions. We will forward the insurance policy as soon as received from the carrier. 
 
Please note that premium is due within 20 days from the effective date of this binder regardless of when the policy is issued. 
 
Thank you for your order.  We appreciate your business. 
 
Should you have any questions, please do not hesitate to call. 
 
</emailBody>
\t\t<insurer>${dataMap.getAt("insuranceCompany")}</insurer>
\t\t<fees></fees>
\t\t<policyNumber></policyNumber>
\t\t<premium></premium>
\t\t<producer>${brokerCompanyName}</producer>
\t\t<referenceNumber></referenceNumber>
\t\t<term></term>
\t\t<total></total>
\t\t<triaPremium></triaPremium>
\t\t<underwriter>xxxxxxxx</underwriter>
\t\t<underwriterEmail>xxxx@company.com</underwriterEmail>
\t\t<underwriterTitle>Vice-President, Underwriting Manager </underwriterTitle>
\t\t<underwriterPhoneNumber>xxxxxxxxx</underwriterPhoneNumber>
\t\t<coverage>${coverages}</coverage>
\t</binding>
</application>]]></int:Data>
                </int:ProvidedData>
            </int:providedData>
            <int:options>
                <int:ReturnDocuments>true</int:ReturnDocuments>
                <int:RunProviders>1</int:RunProviders>
                <int:LogGeneration>true</int:LogGeneration>
            </int:options>
        </int:GenerateWithData>
    </x:Body>
</x:Envelope>"""

        
        def client = new SOAPClient('http://xxx.xx.xxx.xx/Produce/Service/GenerateDoc.asmx?WSDL')
        client.authorization = new HTTPBasicAuthorization("xxxx", "xxxxxxx")
        def response = client.send(SOAPAction:'http://services.dpm.com.au/intelledox/GenerateWithData', soapXML)

        def fileName = "Indication A.pdf"
        
        //PARSE RESPONSE FROM INTELLEDOX SERVER FOR BINARY FILE
        def a = new XmlSlurper().parseText(response.text)
        def nodeToSerialize = a."**".find {it.name() == 'BinaryFile'}
        def pdfBinaryFile = nodeToSerialize.text();
        
        
        //GET THE SUBMISSION/QUOTE ID
        def quoteID = dataMap.getAt("allQuoteIDs").split(",")[0].split(";")[0]
        
        
        //GET REAL PATH OF FOLDER ON DIGITAL OCEAN
        def folderPath = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getRealPath("/attachments/${quoteID}/")

        //SAVE PDF TO BOTH DIGITAL OCEAN AND AIM SERVER
        fileHelper.saveBinaryFileToLocalPath(pdfBinaryFile, folderPath, fileName);
        fileHelper.ftpFileToAIM(fileName, folderPath, quoteID, dataSource_aim);

        return "SUCCESS"
    }
}

//<underwriter>${XmlUtil.escapeXml(dataMap.getAt('accountExecName'))}</underwriter>
//\t\t<underwriterPhone>${XmlUtil.escapeXml(dataMap.getAt('underwriterPhone'))}</underwriterPhone>
//\t\t<underwriterFax>${XmlUtil.escapeXml(dataMap.getAt('underwriterFax'))}</underwriterFax>
//\t\t<underwriterEmail>${XmlUtil.escapeXml(dataMap.getAt('accountExecEmail'))}</underwriterEmail>