package ru.runa.wfe.datafile.builder;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.report.logic.ReportLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;

@Component
public class ReportDataFileBuilder implements DataFileBuilder {

    @Autowired
    private ReportLogic reportLogic;

    @Override
    public void build(ZipOutputStream zos, Document script, ru.runa.wfe.user.User user) throws IOException {
        BatchPresentation batchPresentation = BatchPresentationFactory.REPORTS.createNonPaged();
        List<WfReport> reports = reportLogic.getReportDefinitions(user, batchPresentation, false);
        for (WfReport report : reports) {
            String fileName = report.getName() + "." + FileDataProvider.REPORT_FILE;
            byte[] reportLogicFile = reportLogic.getFile(user, report.getId(), ru.runa.wfe.definition.FileDataProvider.REPORT_FILE);
            ZipEntry zipEntry = new ZipEntry(PATH_TO_REPORTS + fileName);
            zos.putNextEntry(zipEntry);
            zos.write(reportLogicFile, 0, reportLogicFile.length);
            zos.closeEntry();
            Element element = script.getRootElement().addElement("deployReport", ru.runa.wfe.commons.xml.XmlUtils.RUNA_NAMESPACE);
            element.addAttribute("file", PATH_TO_REPORTS + fileName);
            element.addAttribute("name", report.getName());
            element.addAttribute("description", report.getDescription());
            element.addAttribute("type", report.getCategories()[0]);
            for (WfReportParameter parameter : report.getParameters()) {
                Element parameterElement = element.addElement("parameter");
                parameterElement.addAttribute("name", parameter.getUserName());
                parameterElement.addAttribute("innerName", parameter.getInternalName());
                parameterElement.addAttribute("type", parameter.getType().getDescription());
                //parameterElement.addAttribute("required", String.valueOf(parameter.isRequired()));
            }
        }
    }

    @Override
    public int getOrder() {
        return 7;
    }
}
