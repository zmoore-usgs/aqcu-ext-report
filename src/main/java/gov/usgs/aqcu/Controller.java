package gov.usgs.aqcu;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.usgs.aqcu.builder.ReportBuilderService;
import gov.usgs.aqcu.client.JavaToRClient;
import gov.usgs.aqcu.model.ExtremesReport;
import gov.usgs.aqcu.parameter.ExtremesRequestParameters;

@RestController
@RequestMapping("/extremes")
public class Controller {
	public static final String UNKNOWN_USERNAME = "unknown";

	private Gson gson;
	private ReportBuilderService reportBuilderService;
	private JavaToRClient javaToRClient;

	@Autowired
	public Controller(
			ReportBuilderService reportBuilderService,
		JavaToRClient javaToRClient,
		Gson gson) {
		this.reportBuilderService = reportBuilderService;
		this.javaToRClient = javaToRClient;
		this.gson = gson;
	}

	@GetMapping(produces={MediaType.TEXT_HTML_VALUE})
	public ResponseEntity<?> getReport(@Validated ExtremesRequestParameters requestParameters) throws Exception {
		String requestingUser = getRequestingUser();
		ExtremesReport report = reportBuilderService.buildReport(requestParameters, requestingUser);
		byte[] reportHtml = javaToRClient.render(requestingUser, "extremes", gson.toJson(report, ExtremesReport.class));
		return new ResponseEntity<byte[]>(reportHtml, new HttpHeaders(), HttpStatus.OK);
	}
	
	@GetMapping(value="/rawData", produces={MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<ExtremesReport> getReportRawData(@Validated ExtremesRequestParameters requestParameters) throws Exception {
		ExtremesReport report = reportBuilderService.buildReport(requestParameters, getRequestingUser());
		return new ResponseEntity<ExtremesReport>(report, new HttpHeaders(), HttpStatus.OK);
	}

	String getRequestingUser() {
		String username = UNKNOWN_USERNAME;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (null != authentication && !(authentication instanceof AnonymousAuthenticationToken)) {
			username= authentication.getName();
		}
		return username;
	}
}
