package kr.kro.prjectwwtp.service;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import kr.kro.prjectwwtp.domain.FlowPredict;
import kr.kro.prjectwwtp.domain.Member;
import kr.kro.prjectwwtp.domain.TmsPredict;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {
	private final SendGrid sendGrid;
	private final LogService logService;
	@Value("${spring.sendgrid.api-key}")
    private String apiKey;

    @Value("${spring.sendgrid.from-email}")
    private String fromEmail;	
	
	@Value("${spring.EmailAPI.URI}")
	private String emailAPIDomain;
	
	@Value("${chart.png.save-path}")
	private String chartSavePath;
	
	@Value("${chart.png.server-url:https://www.projectwwtp.kro.kr/api/charts}")
	private String chartServerUrl;
    
    public void sendValidateEmail(Member member) {
    	String type = "send One";
		String errorMsg = null;
    	try {
	    	String userId = member.getUserId();
			String email = member.getUserEmail();
			String subject = "Email 인증 From FlowWater"; 
			String validateLink = emailAPIDomain + "/api/member/validateKey?keyValue="+member.getValidateKey();
			String deleteLink = emailAPIDomain + "/api/member/deleteEmail?userId="+userId+"&email="+email;
			String body = "<div style=\"font-family: 'Apple SD Gothic Neo', 'sans-serif' !important; width: 540px; height: 600px; border-top: 4px solid #3498db; margin: 100px auto; padding: 30px 0; box-sizing: border-box;\">" +
		              "    <h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
		              "        <span style=\"color: #3498db;\">" + subject + "</span> 안내" +
		              "    </h1>" +
		              "    <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
		              "        아래 버튼을 클릭하여 인증을 완료해 주세요.<br>" +
		              "        본 메일은 <b>FlowWater</b> 서비스 이용을 위해 발송되었습니다.<br>" +
		              "        본 메일의 인증은 10분 간만 유효합니다." +
		              "    </p>" +
		              "<table cellspacing='0' cellpadding='0' border='0' style='margin: 30px 5px 40px'> " +
		              "  <tr> " +
		              "    <td align='center' bgcolor='#3498db' width='210' height='45' style='border-radius: 5px; color: #ffffff;'> " +
		              "      <a href='" + validateLink + "' target='_blank' style='display: block; width: 210px; height: 45px; font-family: sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; line-height: 45px; text-align: center; font-weight: bold;'> " +
		              "        인증 완료하기 " +
		              "      </a> " +
		              "    </td> " +
		              "  </tr> " +
		              "</table>" +	 		              
		              "    <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
		              "        더이상 이 보고서를 받지 않으시려면<br>" +
		              "        아래 버튼을 눌러 이메일 정보를 삭제하십시오..<br>" +
		              "    </p>" +
		              "<table cellspacing='0' cellpadding='0' border='0' style='margin: 30px 5px 40px'> " +
		              "  <tr> " +
		              "    <td align='center' bgcolor='#ff4444' width='210' height='45' style='border-radius: 5px; color: #ffffff;'> " +
		              "      <a href='" + deleteLink + "' target='_blank' style='display: block; width: 210px; height: 45px; font-family: sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; line-height: 45px; text-align: center; font-weight: bold;'> " +
		              "        수신거부 " +
		              "      </a> " +
		              "    </td> " +
		              "  </tr> " +
		              "</table>" +	 		              
		              "    <div style=\"border-top: 1px solid #DDD; padding: 5px;\">" +
					  "        <p style=\"font-size: 12px; line-height: 21px; color: #777; margin: 0;\">" +
					  "            도움이 필요하시면 <a href=\"https://www.projectwwtp.kro.kr/support\" style=\"color: #3498db; text-decoration: none;\">고객지원</a>으로 문의 바랍니다." +
					  "        </p>" +
		              "    </div>" +
		              "</div>";
			Response response = sendEmail(member.getUserEmail(), subject, body);

			System.out.println("sendEmail response : " + response.getStatusCode());
			if(response.getStatusCode() != 202)
				errorMsg = response.getBody();
    	}catch(Exception e) {
			errorMsg = e.getMessage();
			logService.addErrorLog("MailService.java", "sendEmail()", e.getMessage());
		} finally {
			logService.addMailLog(member, type, errorMsg);	
		}
    }
    
	public String failMessage(String type, String errorMsg) {
		String titleText = "FlowWater 인증 실패 안내";
		String body = "<!DOCTYPE html>" +
			    "<html>" +
			    "<head>" +
			    "    <meta charset=\"UTF-8\">" +
			    "    <title>" + titleText + "</title>" + // 브라우저 탭 타이틀
			    "</head>" +
			    "<body style=\"margin: 0; padding: 0;\">" +
			    "    <div style=\"font-family: 'Apple SD Gothic Neo', 'sans-serif' !important; width: 540px; border-top: 4px solid #e74c3c; margin: 50px auto; padding: 30px 0; box-sizing: border-box;\">" +
			    "        <h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
			    "            <span style=\"color: #e74c3c;\">인증 실패</span> 안내" +
			    "        </h1>" +
			    "        <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
			    "            안녕하세요, <b>FlowWater</b>입니다.<br>" +
			    "            요청하신 <b>"+ type + "</b>이 아래와 같은 사유로 완료되지 않았습니다." +
			    "        </p>" +
			    "        <div style=\"background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 5px;\">" +
			    "            <p style=\"margin: 0; font-size: 15px; color: #333;\">" +
			    "                <b>실패 사유:</b> <span style=\"color: #e74c3c;\">" + errorMsg + "</span>" +
			    "            </p>" +
			    "        </div>" +
			    "        <div style=\"border-top: 1px solid #DDD; padding: 15px 5px;\">" +
			    "            <p style=\"font-size: 12px; line-height: 21px; color: #777; margin: 0;\">" +
			    "                도움이 필요하시면 <a href=\"https://www.projectwwtp.kro.kr/support\" style=\"color: #3498db; text-decoration: none;\">고객지원</a>으로 문의 바랍니다." +
			    "            </p>" +
			    "        </div>" +
			    "    </div>" +
			    "</body>" +
			    "</html>";
		return body;
	}
	
	public String successMessage(String type, Member member) {
		String titleText = "FlowWater 가입을 환영합니다!";
		String mainLink = "https://www.projectwwtp.kro.kr";

		String body = 
		    "<!DOCTYPE html>" +
		    "<html>" +
		    "<head>" +
		    "    <meta charset=\"UTF-8\">" +
		    "    <title>" + titleText + "</title>" +
		    "</head>" +
		    "<body style=\"margin: 0; padding: 0;\">" +
		    "    <div style=\"font-family: 'Apple SD Gothic Neo', 'sans-serif' !important; width: 540px; border-top: 4px solid #3498db; margin: 50px auto; padding: 30px 0; box-sizing: border-box;\">" +
		    "        <h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
		    "            <span style=\"color: #3498db;\">인증 성공!</span> 환영합니다" +
		    "        </h1>" +
		    "        <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
		    "            안녕하세요, <b>" + member.getUserName() + "</b>님!<br>" +
		    "            <b>"+ type + "</b>이 성공적으로 완료되었습니다." +
		    "        </p>";
	    	if(type.equals("이메일 인증")) {		    
	    		body += "        <div style=\"background-color: #f0f8ff; padding: 20px; border-radius: 5px; margin: 20px 5px; border: 1px dashed #3498db;\">" +
			    "            <p style=\"margin: 0; font-size: 15px; color: #333; text-align: center;\">" +
			    "                <b>\"FlowWater와 함께 깨끗하고 스마트한 시작을 함께하세요!\"</b>" +
			    "            </p>" +
			    "        </div>" +
			    "        <p style=\"font-size: 16px; line-height: 26px; padding: 0 5px;\">" +
			    "            아래 버튼을 눌러 메인 화면으로 이동해 보세요." +
			    "        </p>" +
	            "<table cellspacing='0' cellpadding='0' border='0' style='margin: 30px 5px 40px'> " +
	            "  <tr> " +
	            "    <td align='center' bgcolor='#3498db' width='210' height='45' style='border-radius: 5px; color: #ffffff'> " +
	            "      <a href='" + mainLink + "' target='_blank' style='display: block; width: 210px; height: 45px; font-family: sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; line-height: 45px; text-align: center; font-weight: bold;'> " +
	            "        FlowWater 시작하기 " +
	            "      </a> " +
	            "    </td> " +
	            "  </tr> " +
	            "</table>";
	    	}
		    body += "        <div style=\"border-top: 1px solid #DDD; padding: 15px 5px;\">" +
		    "            <p style=\"font-size: 12px; line-height: 21px; color: #777; margin: 0;\">" +
		    "                도움이 필요하시면 <a href=\"http://wwws.projectwwtp.kro.kr/support\" style=\"color: #3498db; text-decoration: none;\">고객지원</a>으로 문의 바랍니다." +
		    "            </p>" +
		    "        </div>" +
		    "    </div>" +
		    "</body>" +
		    "</html>";
		return body;
	}
    
	/**
	 * 통합된 보고서 메일 발송 함수
	 * @param member 수신자
	 * @param tmsList TMS 데이터
	 * @param flowList 유입유량 데이터
	 * @param timeStamp 차트 파일명용 타임스탬프
	 * @param fileName 첨부파일명
	 */
	public void sendReportMail(Member member, List<TmsPredict> tmsList, List<FlowPredict> flowList, String timeStamp, String fileName) {
	    String type = "sendReport";
	    String errorMsg = null;
	    try {
	        // 첨부용 차트 파일을 서버에서 읽어 첨부
	        String chartFileName = fileName != null ? fileName : ("chart_report_" + timeStamp + ".html");
	        File chartFile = new File(chartSavePath, chartFileName);
	        byte[] fileBytes = null;
	        if (chartFile.exists()) {
	            fileBytes = java.nio.file.Files.readAllBytes(chartFile.toPath());
	        }
	        // 본문 구성 (차트 이미지 포함)
	        String subject = "Report From FlowWater";
	        String userId = member.getUserId();
	        String email = member.getUserEmail();
	        String deleteLink = emailAPIDomain + "/api/member/deleteEmail?userId="+userId+"&email="+email;
	        StringBuilder bodyHtml = new StringBuilder();
	        bodyHtml.append("<div style=\"font-family: 'Apple SD Gothic Neo', 'sans-serif' !important; width: 540px; border-top: 4px solid #3498db; margin: 100px auto; padding: 30px 0; box-sizing: border-box;\">");
	        bodyHtml.append("<h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">");
	        bodyHtml.append("<span style=\"color: #3498db;\">" + subject + "</span> 안내</h1>");
	        bodyHtml.append("<div style=\"margin-top:30px;\">");
	        String chartFileAttachName = chartFileName;
	        String chartHtmlUrl = chartServerUrl + "/" + chartFileName;
	        // 상단 타이틀 바로 아래에 버튼 배치
	        bodyHtml.append("<p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">아래 버튼을 클릭해 12시간 동안의 예측차트를 확인해보세요.<br>");
	        bodyHtml.append("<table cellspacing='0' cellpadding='0' border='0' style='margin: 30px 5px 40px'> <tr> <td align='center' bgcolor='#3498db' width='210' height='45' style='border-radius: 5px; color: #ffffff'> <a href='" + chartHtmlUrl + "' target='_blank' style='display: block; width: 210px; height: 45px; font-family: sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; line-height: 45px; text-align: center; font-weight: bold;'>예측차트 바로가기</a> </td> </tr> </table>");
	        // 안내 및 차트 이미지
	        bodyHtml.append("<p style=\"font-size: 16px; line-height: 26px; margin-top: 30px; padding: 0 5px;\">아래 차트 이미지를 확인해보세요.<br></p>");
	        // 차트 이미지 삽입
	        String flowPng = "chart_flow_" + timeStamp + ".png";
	        if (new java.io.File(chartSavePath, flowPng).exists()) {
	            bodyHtml.append("<div style=\"margin-bottom: 30px;\"><b>유입유량 예측</b><br><img src=\"")
	                    .append(chartServerUrl).append("/").append(flowPng)
	                    .append("\" style=\"width:100%;max-width:500px;border:1px solid #ddd;border-radius:4px;\"></div>");
	        }
	        String[] tmsKeys = {"TOC", "PH", "SS", "FLUX", "TN", "TP"};
	        for (String key : tmsKeys) {
	            String tmsPng = "chart_" + key.toLowerCase() + "_" + timeStamp + ".png";
	            if (new java.io.File(chartSavePath, tmsPng).exists()) {
	                bodyHtml.append("<div style=\"margin-bottom: 30px;\"><b>")
	                        .append(key).append(" 예측</b><br><img src=\"")
	                        .append(chartServerUrl).append("/").append(tmsPng)
	                        .append("\" style=\"width:100%;max-width:500px;border:1px solid #ddd;border-radius:4px;\"></div>");
	            }
	        }
	        bodyHtml.append("</div>");
	        
	        bodyHtml.append("<p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">더이상 이 보고서를 받지 않으시려면<br>아래 버튼을 눌러 이메일 정보를 삭제하십시오..<br></p>");
	        bodyHtml.append("<table cellspacing='0' cellpadding='0' border='0' style='margin: 30px 5px 40px'> <tr> <td align='center' bgcolor='#ff4444' width='210' height='45' style='border-radius: 5px; color: #ffffff'> <a href='" + deleteLink + "' target='_blank' style='display: block; width: 210px; height: 45px; font-family: sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; line-height: 45px; text-align: center; font-weight: bold;'>수신거부</a> </td> </tr> </table>");
	        bodyHtml.append("<div style=\"border-top: 1px solid #DDD; padding: 5px;\"><p style=\"font-size: 12px; line-height: 21px; color: #777; margin: 0;\">도움이 필요하시면 <a href=\"https://www.projectwwtp.kro.kr/support\" style=\"color: #3498db; text-decoration: none;\">고객지원</a>으로 문의 바랍니다.</p></div>");
	        bodyHtml.append("</div>");
	        Email from = new Email(fromEmail);
	        Email to = new Email(member.getUserEmail());
	        Content content = new Content("text/html", bodyHtml.toString());
	        Mail mail = new Mail(from, subject, to, content);
	        // 첨부파일 제거 (html 첨부 X)
	        Request request = new Request();
	        request.setMethod(Method.POST);
	        request.setEndpoint("mail/send");
	        request.setBody(mail.build());
	        Response response = sendGrid.api(request);
	        if(response.getStatusCode() != 202)
	            errorMsg = response.getBody();
	    } catch (Exception e) {
	        errorMsg = e.getMessage();
	        logService.addErrorLog("MailService.java", "sendReportMail()", e.getMessage());
	    } finally {
	        logService.addMailLog(member, type, errorMsg);
	    }
	}
	
	/**
	 * 차트 HTML 파일을 생성하여 서버에 저장하고 파일명을 반환
	 * @param tmsList TMS 데이터
	 * @param flowList 유입유량 데이터
	 * @param timeStamp 차트 파일명용 타임스탬프
	 * @return 저장된 파일명 (예: chart_report_20260318.html)
	 */
	public String makeChartFile(List<TmsPredict> tmsList, List<FlowPredict> flowList, String timeStamp) {
        try {
            HashMap<String, String> tmsSvgs = new HashMap<>();
            String flowSvg = generateSvgChartWithTooltip(flowList, java.util.Arrays.asList("flow"), true, "유입유량 예측");
            String[] tmsKeys = {"TOC", "PH", "SS", "FLUX", "TN", "TP"};
            for (String key : tmsKeys) {
                String tmsSvg = generateSvgChartWithTooltip(tmsList, java.util.Arrays.asList(key), false, key + " 예측");
                tmsSvgs.put(key, tmsSvg);
            }
            StringBuilder chartHtml = new StringBuilder();
            chartHtml.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>FlowWater Report Chart</title>");
            chartHtml.append("<style>.tooltip {position:absolute;display:none;padding:6px 12px;background:#222;color:#fff;border-radius:4px;font-size:14px;z-index:1000;pointer-events:none;}</style>");
            chartHtml.append("</head><body style='position:relative;'>");
            chartHtml.append("<div class='tooltip' id='chartTooltip'></div>");
            if (flowSvg != null) {
                chartHtml.append("<h2>유입유량 예측</h2>").append(flowSvg);
            }
            for (String key : tmsKeys) {
                if (tmsSvgs.containsKey(key)) {
                    chartHtml.append("<h2>").append(key).append(" 예측</h2>").append(tmsSvgs.get(key));
                }
            }
            chartHtml.append("<script>\n");
            chartHtml.append("document.querySelectorAll('svg').forEach(function(svg) {\n");
            chartHtml.append("  svg.addEventListener('mouseover', function(e) {\n");
            chartHtml.append("    if(e.target.classList.contains('chart-point')) {\n");
            chartHtml.append("      var tooltip = document.getElementById('chartTooltip');\n");
            chartHtml.append("      var value = e.target.getAttribute('data-value');\n");
            chartHtml.append("      var x = parseInt(e.target.getAttribute('data-x')) || e.target.cx.baseVal.value;\n");
            chartHtml.append("      var y = parseInt(e.target.getAttribute('data-y')) || e.target.cy.baseVal.value;\n");
            chartHtml.append("      tooltip.style.display = 'block';\n");
            chartHtml.append("      tooltip.textContent = value;\n");
            chartHtml.append("      var rect = svg.getBoundingClientRect();\n");
            chartHtml.append("      tooltip.style.left = (rect.left + x + window.scrollX + 10) + 'px';\n");
            chartHtml.append("      tooltip.style.top = (rect.top + y + window.scrollY - 30) + 'px';\n");
            chartHtml.append("    }\n");
            chartHtml.append("  });\n");
            chartHtml.append("  svg.addEventListener('mouseout', function(e) {\n");
            chartHtml.append("    if(e.target.classList.contains('chart-point')) {\n");
            chartHtml.append("      var tooltip = document.getElementById('chartTooltip');\n");
            chartHtml.append("      tooltip.style.display = 'none';\n");
            chartHtml.append("    }\n");
            chartHtml.append("  });\n");
            chartHtml.append("});\n");
            chartHtml.append("</script>");
            chartHtml.append("</body></html>");
            String chartFileName = "chart_report_" + timeStamp + ".html";
            File directory = new File(chartSavePath);
            if (!directory.exists()) directory.mkdirs();
            File file = new File(directory, chartFileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(chartHtml.toString().getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
            
            // PNG 파일 저장 (유입유량)
            if (flowSvg != null) {
                byte[] flowPng = convertSvgToPngBytes(flowSvg);
                if (flowPng != null && flowPng.length > 0) {
                    String flowPngName = "chart_flow_" + timeStamp + ".png";
                    savePngToLocalFile(flowPng, flowPngName);
                }
            }
            // PNG 파일 저장 (TMS별)
            for (String key : tmsKeys) {
                String tmsSvg = tmsSvgs.get(key);
                if (tmsSvg != null) {
                    byte[] tmsPng = convertSvgToPngBytes(tmsSvg);
                    if (tmsPng != null && tmsPng.length > 0) {
                        String tmsPngName = "chart_" + key.toLowerCase() + "_" + timeStamp + ".png";
                        savePngToLocalFile(tmsPng, tmsPngName);
                    }
                }
            }
            
            return chartFileName;
        } catch (Exception e) {
            logService.addErrorLog("MailService.java", "makeChartFile()", e.getMessage());
            return null;
        }
    }
	
	/**
	 * SVG 차트 이미지를 생성하는 정적 메서드
	 * @param data 데이터 포인트 리스트
	 * @param keys 데이터 키 (TOC, PH, SS 등)
	 * @param isSingle 단일 데이터인지 여부
	 * @param title 차트 제목
	 * @return SVG 문자열
	 */
/*	
	private String generateSvgChart(List<? extends Object> data, List<String> keys, boolean isSingle, String title) {
		int width = 1000;
		int height = 350;
		int padding = 60;
		int chartW = width - (padding * 2);
		int chartH = height - (padding * 2);
		
		if (data == null || data.isEmpty()) {
			return "<svg viewBox=\"0 0 " + width + " " + height + "\" xmlns=\"http://www.w3.org/2000/svg\" width=\"" + width + "\" height=\"" + height + "\">" +
				   "<rect width=\"" + width + "\" height=\"" + height + "\" fill=\"white\"/>" +
				   "<text x=\"500\" y=\"175\" text-anchor=\"middle\" fill=\"#999\">No data</text>" +
				   "</svg>";
		}
		
		// 메모리 효율을 위해 데이터 샘플링 (최대 200개 포인트)
		List<?> sampledData = data;
		int sampleRate = 1;
		int maxDataPoints = 200;
		
		if (data.size() > maxDataPoints) {
			sampleRate = data.size() / maxDataPoints;
			List<Object> temp = new ArrayList<>();
			for (int i = 0; i < data.size(); i += sampleRate) {
				temp.add(data.get(i));
			}
			sampledData = temp;
			System.out.println("[SVG] Data sampled: " + data.size() + " → " + sampledData.size() + " (rate: " + sampleRate + ")");
		}
		
		StringBuilder svg = new StringBuilder();
		// SVG 루트 요소
		svg.append("<svg viewBox=\"0 0 ").append(width).append(" ").append(height)
		   .append("\" xmlns=\"http://www.w3.org/2000/svg\" ")
		   .append("width=\"").append(width).append("\" height=\"").append(height)
		   .append("\" preserveAspectRatio=\"xMidYMid meet\">\n");
		
		// 배경 (흰색, 전체)
		svg.append("<rect x=\"0\" y=\"0\" width=\"").append(width).append("\" height=\"").append(height)
		   .append("\" fill=\"white\"/>\n");
		
		// 차트 배경
		svg.append("<rect x=\"").append(padding).append("\" y=\"").append(padding)
		   .append("\" width=\"").append(chartW).append("\" height=\"").append(chartH)
		   .append("\" fill=\"white\" stroke=\"#ddd\" stroke-width=\"1\"/>\n");
		
		// 그리드라인과 X축 레이블 (샘플링된 데이터 기반)
		int dataSize = sampledData.size();
		for (int i = 0; i < dataSize; i++) {
			int x = padding + (int)(i * (double)chartW / (dataSize - 1));
			svg.append("<line x1=\"").append(x).append("\" y1=\"").append(padding)
			   .append("\" x2=\"").append(x).append("\" y2=\"").append(height - padding)
			   .append("\" stroke=\"#f0f0f0\" stroke-width=\"1\"/>\n");
			
			// X축 레이블 (시간)
			String timeLabel = getTimeFromData(sampledData, i);
			svg.append("<text x=\"").append(x).append("\" y=\"").append(height - padding + 25)
			   .append("\" text-anchor=\"middle\" font-size=\"10\" fill=\"#888\">")
			   .append(timeLabel).append("</text>\n");
		}
		
		// Y축 값 범위 계산
		double minVal = Double.MAX_VALUE;
		double maxVal = Double.MIN_VALUE;
		
		for (Object dataPoint : sampledData) {
			for (String key : keys) {
				double val = getValueFromData(dataPoint, key, isSingle);
				minVal = Math.min(minVal, val);
				maxVal = Math.max(maxVal, val);
			}
		}
		
		// Y축 범위 조정
		double range = (maxVal - minVal == 0) ? 1 : (maxVal - minVal);
		double yAxisUnit = calculateYAxisUnit(range);
		double maxLabel = Math.ceil(maxVal / yAxisUnit) * yAxisUnit;
		double minLabel = Math.floor(minVal / yAxisUnit) * yAxisUnit;
		double labelRange = maxLabel - minLabel;
		if (labelRange == 0) labelRange = yAxisUnit;
		
		// Y축 레이블 (최대 10개로 제한)
		int labelCount = (int)Math.round(labelRange / yAxisUnit) + 1;
		int maxLabels = 10;
		int labelStep = Math.max(1, labelCount / maxLabels);
		
		for (int i = 0; i < labelCount; i += labelStep) {
			double val = maxLabel - (labelRange / (labelCount - 1) * i);
			int y = padding + (int)(i * chartH / (labelCount - 1));
			svg.append("<text x=\"").append(padding - 10).append("\" y=\"").append(y + 5)
			   .append("\" text-anchor=\"end\" font-size=\"10\" fill=\"#888\">")
			   .append(String.format("%.1f", val)).append("</text>\n");
			
			svg.append("<line x1=\"").append(padding).append("\" y1=\"").append(y)
			   .append("\" x2=\"").append(width - padding).append("\" y2=\"").append(y)
			   .append("\" stroke=\"#e0e0e0\" stroke-width=\"1\"/>\n");
		}
		
		// 색상 정의
		HashMap<String, String> colorMap = new HashMap<>();
		colorMap.put("TOC", "#e74c3c");
		colorMap.put("PH", "#2ecc71");
		colorMap.put("SS", "#f1c40f");
		colorMap.put("FLUX", "#9b59b6");
		colorMap.put("TN", "#34495e");
		colorMap.put("TP", "#e67e22");
		colorMap.put("flow", "#3498db");
		
		// 차트 라인 그리기
		for (int keyIdx = 0; keyIdx < keys.size(); keyIdx++) {
			String key = keys.get(keyIdx);
			String color = colorMap.get(key);
			
			// polyline의 points 속성은 "x1,y1 x2,y2 x3,y3" 형식이어야 함
			StringBuilder pointsData = new StringBuilder();
			
			for (int i = 0; i < dataSize; i++) {
				double val = getValueFromData(sampledData.get(i), key, isSingle);
				int x = padding + (int)(i * (double)chartW / (dataSize - 1));
				double scaledVal = (val - minLabel) / labelRange * chartH;
				int y = height - padding - (int)scaledVal;
				
				if (i > 0) {
					pointsData.append(" ");
				}
				pointsData.append(x).append(",").append(y);
			}
			
			svg.append("<polyline points=\"").append(pointsData.toString())
			   .append("\" fill=\"none\" stroke=\"").append(color)
			   .append("\" stroke-width=\"2.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>\n");
			
			// 데이터 포인트 (모든 포인트는 표시하지 않음, 샘플링된 포인트만)
			for (int i = 0; i < dataSize; i++) {
				double val = getValueFromData(sampledData.get(i), key, isSingle);
				int x = padding + (int)(i * (double)chartW / (dataSize - 1));
				double scaledVal = (val - minLabel) / labelRange * chartH;
				int y = height - padding - (int)scaledVal;
				
				svg.append("<circle cx=\"").append(x).append("\" cy=\"").append(y)
				   .append("\" r=\"3\" fill=\"white\" stroke=\"").append(color)
				   .append("\" stroke-width=\"1.5\"/>\n");
			}
		}
		
		// 범례 추가
		int legendY = padding - 35;
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String color = colorMap.get(key);
			int legendX = padding + (i * 150);
			
			svg.append("<rect x=\"").append(legendX).append("\" y=\"").append(legendY)
			   .append("\" width=\"12\" height=\"12\" fill=\"").append(color).append("\"/>\n");
			svg.append("<text x=\"").append(legendX + 18).append("\" y=\"").append(legendY + 12)
			   .append("\" font-size=\"11\" fill=\"#333\">").append(key).append("</text>\n");
		}
		
		svg.append("</svg>\n");
		return svg.toString();
	}
*/	
	private double calculateYAxisUnit(double range) {
		if (range <= 1) return 0.1;
		else if (range <= 5) return 0.5;
		else if (range <= 10) return 1;
		else if (range <= 50) return 5;
		else if (range <= 100) return 10;
		else if (range <= 500) return 50;
		else if (range <= 1000) return 100;
		else if (range <= 5000) return 500;
		else if (range <= 10000) return 1000;
		else if (range <= 50000) return 5000;
		else return 10000;
	}
	
	private String getTimeFromData(List<? extends Object> data, int index) {
		Object item = data.get(index);
		try {
			if (item instanceof FlowPredict) {
				return ((FlowPredict)item).getFlowTime().format(DateTimeFormatter.ofPattern("HH:mm"));
			} else if (item instanceof TmsPredict) {
				return ((TmsPredict)item).getTmsTime().format(DateTimeFormatter.ofPattern("HH:mm"));
			}
		} catch (Exception e) {
			logService.addErrorLog("MailService.java", "getTimeFromData()", e.getMessage());
		}
		return "";
	}
	
	private double getValueFromData(Object data, String key, boolean isSingle) {
		try {
			if (isSingle && data instanceof FlowPredict) {
				return ((FlowPredict)data).getFlowValue();
			} else if (data instanceof TmsPredict) {
				TmsPredict tms = (TmsPredict) data;
				switch(key) {
					case "TOC": return tms.getToc();
					case "PH": return tms.getPh();
					case "SS": return tms.getSs();
					case "FLUX": return tms.getFlux();
					case "TN": return tms.getTn();
					case "TP": return tms.getTp();
				}
			}
		} catch (Exception e) {
			logService.addErrorLog("MailService.java", "getValueFromData()", e.getMessage());
		}
		return 0;
	}
	
	/**
	 * SVG를 PNG 바이트 배열로 변환 (CID용)
	 * @param svgString SVG 문자열
	 * @return PNG 이미지의 바이트 배열
	 */
	private byte[] convertSvgToPngBytes(String svgString) {
		try {
			System.out.println("[SVG Conversion] Starting PNG conversion...");
			System.out.println("[SVG Conversion] Original SVG Size: " + svgString.length() + " bytes");
			System.out.println("[SVG Conversion] SVG Content (first 200 chars): " + svgString.substring(0, Math.min(200, svgString.length())));
			
			// SVG가 완전한 문서 구조를 가져야 함
			String completeSvg = svgString.trim();
			
			// XML 선언 추가
			if (!completeSvg.startsWith("<?xml")) {
				completeSvg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + completeSvg;
			}
			
			// DOCTYPE 추가
			if (!completeSvg.contains("<!DOCTYPE")) {
				completeSvg = completeSvg.replace("<svg", 
					"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n<svg");
			}
			
			System.out.println("[SVG Conversion] Complete SVG Size: " + completeSvg.length() + " bytes");
			
			// Batik 트랜스코더 설정
			PNGTranscoder transcoder = new PNGTranscoder();
			transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 1000f);
			transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 350f);
			
			// SVG 입력 생성
			ByteArrayInputStream svgInput = new ByteArrayInputStream(completeSvg.getBytes(StandardCharsets.UTF_8));
			
			// PNG 출력 생성
			ByteArrayOutputStream pngOutput = new ByteArrayOutputStream();
			
			// 트랜스코더 입출력 설정
			TranscoderInput input = new TranscoderInput(svgInput);
			TranscoderOutput output = new TranscoderOutput(pngOutput);
			
			System.out.println("[SVG Conversion] Executing transcode...");
			
			// 트랜스코딩 실행
			transcoder.transcode(input, output);
			
			byte[] pngBytes = pngOutput.toByteArray();
			System.out.println("[SVG Conversion] Success! PNG Size: " + pngBytes.length + " bytes");
			
			return pngBytes;
			
		} catch (org.apache.batik.transcoder.TranscoderException te) {
			System.err.println("[SVG Conversion] Batik TranscoderException:");
			System.err.println("  Message: " + te.getMessage());
			if (te.getCause() != null) {
				System.err.println("  Cause: " + te.getCause().getMessage());
				te.getCause().printStackTrace();
			}
			te.printStackTrace();
			
			String errorMsg = "Batik TranscoderException: " + te.getMessage();
			if (te.getCause() != null) {
				errorMsg += " (Cause: " + te.getCause().getMessage() + ")";
			}
			logService.addErrorLog("MailService.java", "convertSvgToPngBytes()", errorMsg);
			return new byte[0];
			
		} catch (Exception e) {
			System.err.println("[SVG Conversion] Error converting SVG to PNG:");
			System.err.println("  Exception Type: " + e.getClass().getName());
			System.err.println("  Message: " + e.getMessage());
			e.printStackTrace();
			
			String errorMsg = "SVG to PNG conversion failed: " + e.getClass().getSimpleName() + " - " + e.getMessage();
			logService.addErrorLog("MailService.java", "convertSvgToPngBytes()", errorMsg);
			return new byte[0];
		}
	}
	
	/**
	 * 로컬 저장 경로를 사용하여 차트 이미지를 메일에 포함시키는 메서드
	 * @param member 수신자
	 * @param subject 메일 제목
	 * @param bodyHtml 메일 본문
	 * @param tmsList TMS 데이터
	 * @param flowList 유입유량 데이터
	 */
/*	
	public void sendEmailWithChartAsUrl(Member member, String subject, String bodyHtml, 
										List<TmsPredict> tmsList, List<FlowPredict> flowList,
										String fileName, String chart, String timeStamp) {
		String type = "sendReport";
		String errorMsg = null;
        try {
        	// 유입유량 차트와 TMS 차트 파일 저장
        	HashMap<String, String> chartPaths = new HashMap<>();
        	
        	// 유입유량 차트
			String flowSvg = generateSvgChart(flowList, java.util.Arrays.asList("flow"), true, "");
			byte[] flowPng = convertSvgToPngBytes(flowSvg);
			String flowPath = null;
			if (flowPng.length > 0) {
				String flowFilename = "chart_flow_" + timeStamp + ".png";
				String flowFilePath = savePngToLocalFile(flowPng, flowFilename);
				if (flowFilePath != null) {
					flowPath = flowFilename;
					System.out.println("[PNG Charts] Flow chart saved: " + flowFilePath);
				}
			}
			
			// TMS 차트들
			String[] tmsKeys = {"TOC", "PH", "SS", "FLUX", "TN", "TP"};
			for (String key : tmsKeys) {
				String tmsSvg = generateSvgChart(tmsList, java.util.Arrays.asList(key), false, "");
				byte[] tmsPng = convertSvgToPngBytes(tmsSvg);
				if (tmsPng.length > 0) {
					String tmsFilename = "chart_" + key.toLowerCase() + "_" + timeStamp + ".png";
					String tmsFilePath = savePngToLocalFile(tmsPng, tmsFilename);
					if (tmsFilePath != null) {
						chartPaths.put(key, tmsFilename);
						System.out.println("[PNG Charts] " + key + " chart saved: " + tmsFilePath);
					}
				}
			}
        	
        	// 메일 본문 생성 (URL 방식 사용)
        	String bodyWithImages = generateChartBodyWithUrl(bodyHtml, flowPath, chartPaths);
        	
        	Email from = new Email(fromEmail);
			Email to = new Email(member.getUserEmail());
			Content content = new Content("text/html", bodyWithImages);
			Mail mail = new Mail(from, subject, to, content);
			
			Attachments attachment = new Attachments();
			attachment.setContent(Base64.getEncoder().encodeToString(chart.getBytes(StandardCharsets.UTF_8)));
			attachment.setFilename(fileName);
			attachment.setType("text/html");
			attachment.setDisposition("attachment");
			mail.addAttachments(attachment);
			
			Request request = new Request();
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			
			Response response = sendGrid.api(request);
			System.out.println("sendEmail response : " + response.getStatusCode());
			if(response.getStatusCode() != 202)
				errorMsg = response.getBody();
        } catch (Exception e) {
			errorMsg = e.getMessage();
			logService.addErrorLog("MailService.java", "sendEmailWithChartAsUrl()", e.getMessage());
		} finally {
			logService.addMailLog(member, type, errorMsg);	
		}
	}
*/	
	/**
	 * 로컬 파일 경로가 포함된 메일 본문 생성
	 * @param bodyHtml 기본 메일 본문
	 * @param flowPath 유입유량 차트 파일명
	 * @param tmsPaths TMS 차트 파일명 맵
	 * @return 서버 URL이 포함된 HTML
	 */
/*	
	private String generateChartBodyWithUrl(String bodyHtml, String flowPath, HashMap<String, String> tmsPaths) {
		StringBuilder html = new StringBuilder();
		
		// 기본 메일 본문에서 h1과 나머지 부분 분리
		String headerPart = extractHeaderBeforeCharts(bodyHtml);
		String footerPart = extractFooterAfterCharts(bodyHtml);
		
		// 헤더 부분 추가 (h1 태그)
		html.append(headerPart).append("\n");
		
		// CSS 스타일 추가
		html.append("<style>\n");
		html.append("  .chart-container { font-family: Arial, sans-serif; background: #f9f9f9; padding: 20px; }\n");
		html.append("  .chart-section { background: white; margin-bottom: 30px; padding: 20px; border-radius: 8px; }\n");
		html.append("  .chart-title { color: #2c3e50; border-left: 4px solid #3498db; padding-left: 10px; margin: 0 0 15px 0; font-size: 16px; font-weight: bold; }\n");
		html.append("  .chart-image { width: 100%; height: auto; border: 1px solid #ddd; border-radius: 4px; }\n");
		html.append("</style>\n");
		
		// 차트 컨테이너 시작
		html.append("<div class=\"chart-container\">\n");
		
		// 유입유량 차트
		if (flowPath != null && !flowPath.isEmpty()) {
			html.append("<div class=\"chart-section\">\n");
			html.append("  <div class=\"chart-title\">유입유량 예측</div>\n");
			html.append("  <img src=\"").append(chartServerUrl).append("/").append(flowPath)
			    .append("\" class=\"chart-image\" alt=\"유입유량 예측 차트\" />\n");
			html.append("</div>\n");
		}
		
		// TMS 차트들
		String[] tmsKeys = {"TOC", "PH", "SS", "FLUX", "TN", "TP"};
		for (String key : tmsKeys) {
			if (tmsPaths.containsKey(key)) {
				String tmsPath = tmsPaths.get(key);
				html.append("<div class=\"chart-section\">\n");
				html.append("  <div class=\"chart-title\">").append(key).append(" 예측</div>\n");
				html.append("  <img src=\"").append(chartServerUrl).append("/").append(tmsPath)
				    .append("\" class=\"chart-image\" alt=\"").append(key).append(" 예측 차트\" />\n");
				html.append("</div>\n");
			}
		}
		
		html.append("</div>\n");
		
		// 푸터 부분 추가 (수신거부 등 나머지 내용)
		html.append(footerPart);
		
		return html.toString();
	}
*/	
	/**
	 * 메일 본문에서 h1 태그까지의 헤더 부분 추출
	 * @param bodyHtml 전체 메일 본문
	 * @return h1 태그를 포함한 헤더 부분
	 */
/*	
	private String extractHeaderBeforeCharts(String bodyHtml) {
		int h1EndIndex = bodyHtml.indexOf("</h1>");
		if (h1EndIndex == -1) {
			return bodyHtml;
		}
		return bodyHtml.substring(0, h1EndIndex + 5); // </h1> 포함
	}
*/	
	/**
	 * 메일 본문에서 h1 태그 다음부터의 푸터 부분 추출
	 * @param bodyHtml 전체 메일 본문
	 * @return h1 태그 이후의 나머지 내용
	 */
/*	
	private String extractFooterAfterCharts(String bodyHtml) {
		int h1EndIndex = bodyHtml.indexOf("</h1>");
		if (h1EndIndex == -1) {
			return "";
		}
		return bodyHtml.substring(h1EndIndex + 5); // </h1> 이후부터
	}
*/	
	/**
	 * PNG 이미지를 로컬 파일로 저장
	 * @param pngBytes PNG 이미지 바이트 배열
	 * @param filename 저장할 파일명
	 * @return 저장된 파일의 절대 경로
	 */
	private String savePngToLocalFile(byte[] pngBytes, String filename) {
		if (pngBytes == null || pngBytes.length == 0) {
			System.err.println("[PNG Save] PNG bytes is empty or null for file: " + filename);
			return null;
		}
		
		try {
			// 저장 디렉토리 생성
			File directory = new File(chartSavePath);
			if (!directory.exists()) {
				boolean created = directory.mkdirs();
				if (!created) {
					System.err.println("[PNG Save] Failed to create directory: " + chartSavePath);
					return null;
				}
				System.out.println("[PNG Save] Created directory: " + directory.getAbsolutePath());
			}
			
			// 파일 저장
			File file = new File(directory, filename);
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(pngBytes);
				fos.flush();
			}
			
			String absolutePath = file.getAbsolutePath();
			System.out.println("[PNG Save] Successfully saved PNG file: " + absolutePath);
			System.out.println("[PNG Save] File size: " + file.length() + " bytes");
			
			return absolutePath;
			
		} catch (Exception e) {
			System.err.println("[PNG Save] Error saving PNG file: " + filename);
			System.err.println("[PNG Save] Error message: " + e.getMessage());
			e.printStackTrace();
			
			String errorMsg = "PNG file save failed: " + filename + " - " + e.getMessage();
			logService.addErrorLog("MailService.java", "savePngToLocalFile()", errorMsg);
			return null;
		}
	}
	
	public Response sendEmail(String email, String subject, String bodyHtml) throws Exception {
        Email from = new Email(fromEmail);
        Email to = new Email(email);
        Content content = new Content("text/html", bodyHtml);
        Mail mail = new Mail(from, subject, to, content);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        return sendGrid.api(request);
    }
    
    private String generateSvgChartWithTooltip(List<? extends Object> data, List<String> keys, boolean isSingle, String title) {
        int width = 1000;
        int height = 350;
        int padding = 60;
        int chartW = width - (padding * 2);
        int chartH = height - (padding * 2);
        if (data == null || data.isEmpty()) {
            return "<svg viewBox='0 0 " + width + " " + height + "' xmlns='http://www.w3.org/2000/svg' width='" + width + "' height='" + height + "'>" +
                   "<rect width='" + width + "' height='" + height + "' fill='white'/><text x='500' y='175' text-anchor='middle' fill='#999'>No data</text></svg>";
        }
        List<?> sampledData = data;
        int sampleRate = 1;
        int maxDataPoints = 200;
        if (data.size() > maxDataPoints) {
            sampleRate = data.size() / maxDataPoints;
            List<Object> temp = new ArrayList<>();
            for (int i = 0; i < data.size(); i += sampleRate) {
                temp.add(data.get(i));
            }
            sampledData = temp;
        }
        StringBuilder svg = new StringBuilder();
        svg.append("<svg viewBox='0 0 ").append(width).append(" ").append(height)
           .append("' xmlns='http://www.w3.org/2000/svg' width='").append(width).append("' height='").append(height)
           .append("' preserveAspectRatio='xMidYMid meet' style='position:relative;'>\n");
        svg.append("<rect x='0' y='0' width='").append(width).append("' height='").append(height).append("' fill='white'/>");
        // 제목 추가 (SVG 내부 제목 제거)
        // if (title != null && !title.isEmpty()) {
        //     svg.append("<text x='").append(padding).append("' y='25' font-size='16' font-weight='bold' fill='#2c3e50'>")
        //        .append(title).append("</text>\n");
        // }
        svg.append("<rect x='").append(padding).append("' y='").append(padding)
           .append("' width='").append(chartW).append("' height='").append(chartH)
           .append("' fill='white' stroke='#ddd' stroke-width='1'/>");
        int dataSize = sampledData.size();
        for (int i = 0; i < dataSize; i++) {
            int x = padding + (int)(i * (double)chartW / (dataSize - 1));
            String timeLabel = getTimeFromData(sampledData, i);
            svg.append("<text x='").append(x).append("' y='").append(height - padding + 25)
               .append("' text-anchor='middle' font-size='10' fill='#888'>")
               .append(timeLabel).append("</text>\n");
        }
        double minVal = Double.MAX_VALUE;
        double maxVal = Double.MIN_VALUE;
        for (Object dataPoint : sampledData) {
            for (String key : keys) {
                double val = getValueFromData(dataPoint, key, isSingle);
                minVal = Math.min(minVal, val);
                maxVal = Math.max(maxVal, val);
            }
        }
        double range = (maxVal - minVal == 0) ? 1 : (maxVal - minVal);
        double yAxisUnit = calculateYAxisUnit(range);
        double maxLabel = Math.ceil(maxVal / yAxisUnit) * yAxisUnit;
        double minLabel = Math.floor(minVal / yAxisUnit) * yAxisUnit;
        double labelRange = maxLabel - minLabel;
        if (labelRange == 0) labelRange = yAxisUnit;
        int labelCount = (int)Math.round(labelRange / yAxisUnit) + 1;
        int maxLabels = 10;
        int labelStep = Math.max(1, labelCount / maxLabels);
        for (int i = 0; i < labelCount; i += labelStep) {
            double val = maxLabel - (labelRange / (labelCount - 1) * i);
            int y = padding + (int)(i * chartH / (labelCount - 1));
            svg.append("<text x='").append(padding - 10).append("' y='").append(y + 5)
               .append("' text-anchor='end' font-size='10' fill='#888'>")
               .append(String.format("%.1f", val)).append("</text>\n");
            svg.append("<line x1='").append(padding).append("' y1='").append(y)
               .append("' x2='").append(width - padding).append("' y2='").append(y)
               .append("' stroke='#e0e0e0' stroke-width='1'/>");
        }
        HashMap<String, String> colorMap = new HashMap<>();
        colorMap.put("TOC", "#e74c3c");
        colorMap.put("PH", "#2ecc71");
        colorMap.put("SS", "#f1c40f");
        colorMap.put("FLUX", "#9b59b6");
        colorMap.put("TN", "#34495e");
        colorMap.put("TP", "#e67e22");
        colorMap.put("flow", "#3498db");
        for (int keyIdx = 0; keyIdx < keys.size(); keyIdx++) {
            String key = keys.get(keyIdx);
            String color = colorMap.get(key);
            StringBuilder pointsData = new StringBuilder();
            for (int i = 0; i < dataSize; i++) {
                double val = getValueFromData(sampledData.get(i), key, isSingle);
                int x = padding + (int)(i * (double)chartW / (dataSize - 1));
                double scaledVal = (val - minLabel) / labelRange * chartH;
                int y = height - padding - (int)scaledVal;
                if (i > 0) pointsData.append(" ");
                pointsData.append(x).append(",").append(y);
            }
            svg.append("<polyline points='").append(pointsData.toString())
               .append("' fill='none' stroke='").append(color)
               .append("' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'/>");
            for (int i = 0; i < dataSize; i++) {
                double val = getValueFromData(sampledData.get(i), key, isSingle);
                int x = padding + (int)(i * (double)chartW / (dataSize - 1));
                double scaledVal = (val - minLabel) / labelRange * chartH;
                int y = height - padding - (int)scaledVal;
                svg.append("<circle cx='").append(x).append("' cy='").append(y)
                   .append("' r='3' fill='white' stroke='").append(color)
                   .append("' stroke-width='1.5' class='chart-point' data-value='").append(val)
                   .append("' data-x='").append(x).append("' data-y='").append(y)
                   .append("'/>");
            }
        }
        int legendY = padding - 35;
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String color = colorMap.get(key);
            int legendX = padding + (i * 150);
            
            svg.append("<rect x='").append(legendX).append("' y='").append(legendY)
               .append("' width='12' height='12' fill='").append(color).append("'/>");
            svg.append("<text x='").append(legendX + 18).append("' y='").append(legendY + 12)
               .append("' font-size='11' fill='#333'>").append(key).append("</text>\n");
        }
        
        svg.append("</svg>\n");
        return svg.toString();
    }
}
